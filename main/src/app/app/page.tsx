'use client';

import {useEffect, useState} from 'react';
import Map from "@/components/Map";
import type { TransitLine } from '@/types/transit';
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
  const [timeOfDay, setTimeOfDay] = useState(() => {
    const now = new Date();
    return now.getHours() * 60 + now.getMinutes();
  });
  const [score, setScore] = useState(0);

  useEffect(() => {
    fetch('/api/network?type=0,1,3')
        .then(response => response.json())
        .then((data) => {
          const lines = Array.isArray(data) ? data : data.lines;
          setLines(lines ?? []);
          console.log(lines)
        })
        .catch(error => console.error('Error fetching network:', error));
  }, []);

  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-black relative">
      {/* Simulation Time Display - Top Right */}
      <div id="simTimeDisplay">{formatTime(timeOfDay)}</div>

      {/* Main Content - Full screen map */}
      <main className="h-screen">
        <div className="w-full h-full">
          <Map lines={lines} />
        </div>
      </main>

      {/* Score Display - Bottom Right */}
      <div id="scoreDisplay">
        <span className="score-label">Score</span>
        <span className="score-value">{score}</span>
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
        <button>Edit Stops</button>
        <button>Heatmap</button>
        <button>More Analysis</button>
      </div>
    </div>
  );
}
