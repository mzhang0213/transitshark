'use client';

import {useEffect, useState, useCallback, useRef} from 'react';
import Map from "@/components/Map";
import FareCalculator from "@/components/FareCalculator";
import type { TransitLine, ZoneInfo, ZoneScore } from '@/types/transit';
import "./page.css"

function formatTime(minutes: number) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  const period = h >= 12 ? 'PM' : 'AM';
  const display = h === 0 ? 12 : h > 12 ? h - 12 : h;
  return `${display}:${m.toString().padStart(2, '0')} ${period}`;
}

const MODE_TO_ROUTE_TYPE: Record<string, number> = {
  light_rail: 0,
  heavy_rail: 1,
  commuter_rail: 2,
  bus: 3,
};

/** Extract all stops from current lines state into the format the backend expects */
function extractStopsForScoring(lines: TransitLine[]): { lat: number; lng: number; routeType: number }[] {
  const seen = new Set<string>();
  const stops: { lat: number; lng: number; routeType: number }[] = [];
  for (const line of lines) {
    const routeType = MODE_TO_ROUTE_TYPE[line.mode] ?? 3;
    for (const stop of line.stops) {
      const key = stop.mbtaStopId;
      if (seen.has(key)) continue;
      seen.add(key);
      stops.push({ lat: stop.lat, lng: stop.lng, routeType });
    }
  }
  return stops;
}

export default function Home() {
  const [lines, setLines] = useState<TransitLine[]>([]);
  const [zones, setZones] = useState<ZoneInfo[]>([]);
  const [zoneScores, setZoneScores] = useState<ZoneScore[]>([]);
  const [editMode, setEditMode] = useState(false);
  const [movedStops, setMovedStops] = useState<Set<string>>(new Set());
  const [timeOfDay, setTimeOfDay] = useState(() => {
    const now = new Date();
    return now.getHours() * 60 + now.getMinutes();
  });

  // Keep a ref to lines so async callbacks always see the latest state
  const linesRef = useRef(lines);
  linesRef.current = lines;

  const totalScore = zoneScores.reduce((sum, z) => sum + z.score, 0);

  /** Send current frontend stop state to backend for score computation */
  const recomputeScores = useCallback(async (currentLines: TransitLine[]) => {
    try {
      const res = await fetch('/api/zones/compute-scores', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ stops: extractStopsForScoring(currentLines) }),
      });
      const data = await res.json();
      const scores = Array.isArray(data) ? data : data.zoneScores;
      if (Array.isArray(scores)) {
        setZoneScores(scores);
      }
    } catch (error) {
      console.error('Error computing scores:', error);
    }
  }, []);

  // Fetch network + zones + initial scores in parallel
  useEffect(() => {
    Promise.all([
      fetch('/api/network?type=0,1,3').then(r => r.json()),
      fetch('/api/zones').then(r => r.json()),
      fetch('/api/zones/scores').then(r => r.json()),
    ]).then(([netData, zonesData, scoresData]) => {
      const lines = Array.isArray(netData) ? netData : netData.lines;
      setLines(lines ?? []);
      setZones(Array.isArray(zonesData) ? zonesData : []);
      setZoneScores(Array.isArray(scoresData) ? scoresData : []);
    })
    .catch(error => console.error('Error fetching data:', error));
  }, []);

  // Called after a stop is dragged
  const onStopMoved = useCallback(async (_stopId: number, newLat: number, newLng: number, mbtaStopId: string) => {
    setMovedStops(prev => new Set(prev).add(mbtaStopId));

    // Optimistic update — edges redraw immediately
    const updatedLines = linesRef.current.map(line => ({
      ...line,
      stops: line.stops.map(stop =>
        stop.mbtaStopId === mbtaStopId ? { ...stop, lat: newLat, lng: newLng } : stop
      ),
    }));
    setLines(updatedLines);

    // Recompute scores from the updated frontend state
    await recomputeScores(updatedLines);
  }, [recomputeScores]);

  // Called when service level is changed on a line (no backend mutation needed)
  const onServiceChanged = useCallback(async () => {
    // Service changes affect the heatmap — recompute from current state
    await recomputeScores(linesRef.current);
  }, [recomputeScores]);

  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-black relative">
      {/* Simulation Time Display - Top Right */}
      <div id="simTimeDisplay">{formatTime(timeOfDay)}</div>

      {/* Edit Mode Indicator */}
      {editMode && (
        <div id="editModeIndicator">EDIT MODE — Drag stops to reposition</div>
      )}

      {/* Main Content - Full screen map */}
      <main className="h-screen">
        <div className="w-full h-full">
          <Map
            lines={lines}
            zones={zones}
            zoneScores={zoneScores}
            editMode={editMode}
            movedStops={movedStops}
            onStopMoved={onStopMoved}
            onServiceChanged={onServiceChanged}
          />
        </div>
      </main>

      {/* Score Display - Bottom Right */}
      <div id="scoreDisplay">
        <span className="score-label">Score</span>
        <span className="score-value">{totalScore.toFixed(1)}</span>
      </div>

      <div id="bottomNavBar">
        <div className="settings-wrapper">
          <button>Settings</button>
          <div className="settings-popup">
            <div className="settings-popup-title">Settings</div>
            <div className="settings-section">
              <label className="settings-label">
                Time of Day
              </label>
              <input
                type="range"
                min={0}
                max={1439}
                value={timeOfDay}
                onChange={(e) => setTimeOfDay(Number(e.target.value))}
                className="settings-slider"
              />
              <div className="settings-time-display">{formatTime(timeOfDay)}</div>
            </div>
          </div>
        </div>
        <button>Calculate</button>
        <button
          className={editMode ? 'active-button' : ''}
          onClick={() => setEditMode(!editMode)}
        >
          {editMode ? 'Done' : 'Edit Map'}
        </button>
        <div className="fare-wrapper">
          <button>Fare Estimate</button>
          <div className="fare-popup">
            <FareCalculator />
          </div>
        </div>
        <button>More Analysis</button>
      </div>
    </div>
  );
}
