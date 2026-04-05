'use client';

import {useEffect, useState, useCallback} from 'react';
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

  const totalScore = zoneScores.reduce((sum, z) => sum + z.score, 0);
  const [scoreVersion, setScoreVersion] = useState(0);

  // Reset lines to original MBTA positions
  const resetLines = useCallback(async () => {
    try {
      // Tell backend to refresh from MBTA API
      await fetch('/api/state/refresh', { method: 'POST' });
      // Re-fetch everything
      const [netData, zonesData, scoresData] = await Promise.all([
        fetch('/api/network?type=0,1,3').then(r => r.json()),
        fetch('/api/zones').then(r => r.json()),
        fetch('/api/zones/scores').then(r => r.json()),
      ]);
      const lines = Array.isArray(netData) ? netData : netData.lines;
      setLines(lines ?? []);
      setZones(Array.isArray(zonesData) ? zonesData : []);
      setZoneScores(Array.isArray(scoresData) ? scoresData : []);
      setMovedStops(new Set());
      setScoreVersion(v => v + 1);
    } catch (error) {
      console.error('Error resetting lines:', error);
    }
  }, []);

  // Fetch network + zones + scores in parallel
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

    // Optimistic update for instant edge redraw
    setLines(prev => prev.map(line => ({
      ...line,
      stops: line.stops.map(stop =>
        stop.mbtaStopId === mbtaStopId ? { ...stop, lat: newLat, lng: newLng } : stop
      ),
    })));

    try {
      // Tell backend to update its in-memory state and recompute scores
      const res = await fetch('/api/modify-stop', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          mbtaStopId,
          modificationType: 'MOVE',
          modification: { newLat, newLng },
        }),
      });
      const scores = await res.json();
      if (Array.isArray(scores)) {
        setZoneScores(scores);
        setScoreVersion(v => v + 1);
      }
    } catch (error) {
      console.error('Error moving stop:', error);
    }
  }, []);

  // Called when service level is changed on a line
  const onServiceChanged = useCallback(async (lineId: number, serviceLevel: number) => {
    const modificationType = serviceLevel >= 0 ? 'INCR_SERVICE' : 'DECR_SERVICE';
    try {
      const res = await fetch('/api/modify-line', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          line: lineId,
          modificationType,
          modification: { frequencyChangeMinutes: Math.abs(serviceLevel) },
          time: Math.floor(timeOfDay / 60),
        }),
      });
      const data = await res.json();
      const scores = data.zoneScores;
      if (Array.isArray(scores)) {
        setZoneScores(scores);
        setScoreVersion(v => v + 1);
      }
    } catch (error) {
      console.error('Error changing service:', error);
    }
  }, [timeOfDay]);

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
            scoreVersion={scoreVersion}
            onStopMoved={onStopMoved}
            onServiceChanged={onServiceChanged}
          />
        </div>
      </main>

      {/* Score Display - Bottom Right */}
      <div id="scoreDisplay">
        <span className="score-label">Score</span>
        <span className="score-value">{Math.abs(totalScore).toFixed(1)}</span>
        {/*<span className="score-hint">{totalScore > 0 ? 'oversupplied' : totalScore < 0 ? 'underserved' : 'balanced'}</span>*/}
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
            <div className="settings-section" style={{ marginTop: 12 }}>
              <button
                onClick={resetLines}
                className="settings-reset-btn"
              >
                Reset to Default MBTA Lines
              </button>
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
