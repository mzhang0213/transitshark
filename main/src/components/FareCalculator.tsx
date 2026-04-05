'use client';

import { useState, useMemo } from 'react';

const TRANSIT_MODES = [
  { label: 'Local Bus', value: 'BUS', icon: '🚌', fare: 1.70, reduced: 0.85 },
  { label: 'Subway', value: 'SUBWAY', icon: '🚇', fare: 2.40, reduced: 1.10 },
  { label: 'Express Bus', value: 'EXPRESS', icon: '🚍', fare: 4.25, reduced: 2.10 },
] as const;

function computeFare(modes: string[], isReduced: boolean): { total: number; breakdown: string[] } {
  if (modes.length === 0) return { total: 0, breakdown: [] };

  const key = isReduced ? 'reduced' : 'fare';
  const breakdown: string[] = [];
  let total = 0;
  let prevMode: string | null = null;

  for (const mode of modes) {
    const info = TRANSIT_MODES.find(m => m.value === mode)!;
    const fare = info[key];

    if (prevMode === 'BUS' && mode === 'SUBWAY') {
      // Bus→Subway: pay the upgrade difference
      const busFare = TRANSIT_MODES.find(m => m.value === 'BUS')![key];
      const upgrade = Math.max(0, fare - busFare);
      total += upgrade;
      breakdown.push(`${info.label}: +$${upgrade.toFixed(2)} (transfer upgrade)`);
    } else if (prevMode === 'SUBWAY' && mode === 'BUS') {
      // Subway→Bus: free transfer
      breakdown.push(`${info.label}: FREE (transfer from Subway)`);
    } else if (prevMode === 'BUS' && mode === 'BUS') {
      // Bus→Bus: free transfer
      breakdown.push(`${info.label}: FREE (bus transfer)`);
    } else {
      total += fare;
      breakdown.push(`${info.label}: $${fare.toFixed(2)}`);
    }
    prevMode = mode;
  }

  return { total, breakdown };
}

export default function FareCalculator() {
  const [selectedModes, setSelectedModes] = useState<string[]>([]);
  const [isReducedFare, setIsReducedFare] = useState(false);

  const toggleMode = (mode: string) => {
    setSelectedModes(prev =>
      prev.includes(mode) ? prev.filter(m => m !== mode) : [...prev, mode]
    );
  };

  const { total, breakdown } = useMemo(
    () => computeFare(selectedModes, isReducedFare),
    [selectedModes, isReducedFare]
  );

  return (
    <div
      className="w-64 rounded-2xl shadow-2xl overflow-hidden"
      style={{
        background: 'rgba(15, 15, 25, 0.88)',
        backdropFilter: 'blur(16px)',
        border: '1px solid rgba(255,255,255,0.08)',
      }}
    >
      <div
        className="px-4 py-3 flex items-center gap-2"
        style={{
          background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)',
          borderBottom: '1px solid rgba(255,255,255,0.07)',
        }}
      >
        <span className="text-lg">🎫</span>
        <div>
          <p className="text-white font-bold text-sm leading-tight">MBTA Fare Calculator</p>
          <p className="text-xs" style={{ color: '#7c8db0' }}>CharlieCard rates</p>
        </div>
      </div>

      <div className="px-4 py-3 space-y-3">
        <div>
          <p className="text-xs font-semibold mb-2 uppercase tracking-widest" style={{ color: '#7c8db0' }}>
            Trip Legs (in order)
          </p>
          <div className="space-y-1.5">
            {TRANSIT_MODES.map(({ label, value, icon }) => {
              const active = selectedModes.includes(value);
              return (
                <button
                  key={value}
                  onClick={() => toggleMode(value)}
                  className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-sm font-medium transition-all duration-200"
                  style={{
                    background: active
                      ? 'linear-gradient(135deg, rgba(99,102,241,0.35) 0%, rgba(139,92,246,0.25) 100%)'
                      : 'rgba(255,255,255,0.04)',
                    border: active
                      ? '1px solid rgba(139,92,246,0.6)'
                      : '1px solid rgba(255,255,255,0.06)',
                    color: active ? '#c4b5fd' : '#9ca3af',
                  }}
                >
                  <span className="text-base">{icon}</span>
                  <span>{label}</span>
                  {active && (
                    <span className="ml-auto text-xs font-bold" style={{ color: '#a78bfa' }}>✓</span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        <div
          className="flex items-center justify-between px-3 py-2 rounded-xl"
          style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.06)' }}
        >
          <div>
            <p className="text-xs font-medium text-white">Reduced Fare</p>
            <p className="text-xs" style={{ color: '#7c8db0' }}>Student / Senior / TAP</p>
          </div>
          <button
            onClick={() => setIsReducedFare(v => !v)}
            className="relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none"
            style={{
              background: isReducedFare
                ? 'linear-gradient(135deg, #6366f1, #8b5cf6)'
                : 'rgba(255,255,255,0.12)',
            }}
          >
            <span
              className="inline-block h-4 w-4 transform rounded-full bg-white shadow-md transition-transform duration-200"
              style={{ transform: isReducedFare ? 'translateX(1.375rem)' : 'translateX(0.25rem)' }}
            />
          </button>
        </div>

        {selectedModes.length > 0 && (
          <div
            className="px-3 py-3 rounded-xl"
            style={{
              background: 'linear-gradient(135deg, rgba(16,185,129,0.15) 0%, rgba(5,150,105,0.1) 100%)',
              border: '1px solid rgba(16,185,129,0.3)',
            }}
          >
            <div className="space-y-1 mb-2">
              {breakdown.map((line, i) => (
                <p key={i} className="text-xs" style={{ color: '#6ee7b7' }}>{line}</p>
              ))}
            </div>
            <p className="text-2xl font-black tracking-tight text-center" style={{ color: '#34d399' }}>
              {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(total)}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
