'use client';

import { useState, useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, Circle, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import { HeatmapLayer } from 'react-leaflet-heatmap-layer-v3';
import type { TransitLine, TransitStop, ZoneInfo, ZoneScore } from '@/types/transit';


interface MapClientProps {
  lines: TransitLine[];
  zones: ZoneInfo[];
  zoneScores: ZoneScore[];
  editMode: boolean;
  movedStops: Set<string>;
  initialView: { center: [number, number]; zoom: number } | null;
  onViewChange: (view: { center: [number, number]; zoom: number }) => void;
  onStopMoved: (stopId: number, newLat: number, newLng: number, mbtaStopId: string) => void;
  onServiceChanged: (lineId: number, serviceLevel: number) => void;
}

const createStopIcon = (color: string, mode: string) => {
  const isTrainMode = mode !== 'bus';
  if (isTrainMode) {
    return L.icon({
      iconUrl: `data:image/svg+xml;base64,${btoa(`
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" width="20" height="20">
          <circle cx="10" cy="10" r="8" fill="${color}" stroke="white" stroke-width="2"/>
          <circle cx="10" cy="10" r="3" fill="white"/>
        </svg>
      `)}`,
      iconSize: [20, 20],
      iconAnchor: [10, 10],
      popupAnchor: [0, -10]
    });
  } else {
    return L.icon({
      iconUrl: `data:image/svg+xml;base64,${btoa(`
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 18 18" width="18" height="18">
          <rect x="2" y="2" width="14" height="14" rx="2" fill="${color}" stroke="white" stroke-width="2"/>
          <rect x="6" y="6" width="6" height="6" rx="1" fill="white"/>
        </svg>
      `)}`,
      iconSize: [18, 18],
      iconAnchor: [9, 9],
      popupAnchor: [0, -9]
    });
  }
};

const createUserIcon = () => {
  return L.icon({
    iconUrl: `data:image/svg+xml;base64,${btoa(`
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
        <circle cx="12" cy="12" r="8" fill="#4CAF50" fill-opacity="0.3"/>
        <circle cx="12" cy="12" r="5" fill="#4CAF50" fill-opacity="0.7"/>
        <circle cx="12" cy="12" r="2" fill="#4CAF50"/>
      </svg>
    `)}`,
    iconSize: [24, 24],
    iconAnchor: [12, 12],
    popupAnchor: [0, -12]
  });
};

interface UserLocation {
  lat: number;
  lng: number;
  accuracy?: number;
  timestamp?: number;
}

const MAP_STYLES = {
  cartoVoyager: {
    name: 'CartoDB Voyager',
    url: 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, &copy; <a href="https://carto.com/attributions">CARTO</a>'
  },
  cartoDark: {
    name: 'CartoDB Dark',
    url: 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, &copy; <a href="https://carto.com/attributions">CARTO</a>'
  },
  osm: {
    name: 'OpenStreetMap',
    url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  },
  cartoLight: {
    name: 'CartoDB Light',
    url: 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png',
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, &copy; <a href="https://carto.com/attributions">CARTO</a>'
  },
};

const BUS_COLOR = '#e6a5b1';

function MapViewTracker({ onViewChange }: { onViewChange: (view: { center: [number, number]; zoom: number }) => void }) {
  useMapEvents({
    moveend: (e) => {
      const map = e.target;
      const c = map.getCenter();
      onViewChange({ center: [c.lat, c.lng], zoom: map.getZoom() });
    },
    zoomend: (e) => {
      const map = e.target;
      const c = map.getCenter();
      onViewChange({ center: [c.lat, c.lng], zoom: map.getZoom() });
    },
  });
  return null;
}

interface SelectedLine {
  lineId: number;
  lineName: string;
  color: string;
  mode: string;
  clickLatLng: [number, number];
}

const MapClient = ({ lines, zones, zoneScores, editMode, movedStops, initialView, onViewChange, onStopMoved, onServiceChanged }: MapClientProps) => {
  const defaultCenter: [number, number] = [42.3601, -71.0589];
  const [currentStyle, setCurrentStyle] = useState('cartoVoyager');
  const [userLocation, setUserLocation] = useState<UserLocation | null>(null);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [isLocating, setIsLocating] = useState(false);
  const [selectedLineId, setSelectedLineId] = useState<number | null>(null);
  const [serviceLine, setServiceLine] = useState<SelectedLine | null>(null);
  const [serviceLevels, setServiceLevels] = useState<Map<number, number>>(new Map());

  const getUserLocation = () => {
    if (!navigator.geolocation) {
      setLocationError('Geolocation is not supported by your browser');
      return;
    }
    setIsLocating(true);
    setLocationError(null);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          accuracy: position.coords.accuracy,
          timestamp: position.timestamp
        });
        setIsLocating(false);
      },
      (error) => {
        const messages: Record<number, string> = {
          [error.PERMISSION_DENIED]: 'Location access denied by user',
          [error.POSITION_UNAVAILABLE]: 'Location information unavailable',
          [error.TIMEOUT]: 'Location request timed out',
        };
        setLocationError(messages[error.code] || 'Unknown location error');
        setIsLocating(false);
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    );
  };

  useEffect(() => {
    getUserLocation();
  }, []);

  // Build edge polylines from the network data
  const edgePolylines = useMemo(() => {
    if (!Array.isArray(lines)) return [];
    const result: { positions: [number, number][]; color: string; lineName: string; isBus: boolean; lineId: number }[] = [];
    for (const line of lines) {
      const stopMap = new Map<number, TransitStop>();
      for (const stop of line.stops) {
        stopMap.set(stop.stopId, stop);
      }
      const isBus = line.mode === 'bus';
      const lineColor = isBus ? BUS_COLOR : (line.color.startsWith('#') ? line.color : `#${line.color}`);
      for (const edge of line.edges) {
        const from = stopMap.get(edge.fromStopId);
        const to = stopMap.get(edge.toStopId);
        if (from && to) {
          result.push({
            positions: [[from.lat, from.lng], [to.lat, to.lng]],
            color: lineColor,
            lineName: line.lineName,
            isBus,
            lineId: line.lineId,
          });
        }
      }
    }
    return result.sort((a, b) => (a.isBus === b.isBus ? 0 : a.isBus ? -1 : 1));
  }, [lines]);

  // Build stop markers: always show train stops, only show bus stops for selected line
  const stopMarkers = useMemo(() => {
    if (!Array.isArray(lines)) return [];
    const map = new Map<string, { stop: TransitStop; lineId: number; lines: { name: string; color: string; mode: string }[] }>();
    for (const line of lines) {
      const isBus = line.mode === 'bus';
      if (isBus && line.lineId !== selectedLineId) continue;
      const lineColor = isBus ? BUS_COLOR : (line.color.startsWith('#') ? line.color : `#${line.color}`);
      for (const stop of line.stops) {
        const existing = map.get(stop.mbtaStopId);
        if (existing) {
          existing.lines.push({ name: line.lineName, color: lineColor, mode: line.mode });
        } else {
          map.set(stop.mbtaStopId, {
            stop,
            lineId: line.lineId,
            lines: [{ name: line.lineName, color: lineColor, mode: line.mode }],
          });
        }
      }
    }
    return Array.from(map.values());
  }, [lines, selectedLineId]);

  // Build heatmap points from zone scores, normalized 0–1
  const heatmapPoints = useMemo(() => {
    if (!zones.length || !zoneScores.length) return [];
    const scoreMap = new Map<string, number>();
    for (const zs of zoneScores) {
      scoreMap.set(zs.zoneId, zs.score);
    }
    const raw = zones
      .filter(z => scoreMap.has(z.zoneId))
      .map(z => ({
        lat: z.centerLat,
        lng: z.centerLng,
        intensity: scoreMap.get(z.zoneId) || 0,
      }));
    const maxVal = Math.max(...raw.map(p => p.intensity));
    const minVal = Math.min(...raw.map(p => p.intensity));
    return raw.map(p => ({ ...p, intensity: (p.intensity-minVal) / (maxVal-minVal)}));
  }, [zones, zoneScores]);

  const isLoaded = Array.isArray(lines) && lines.length > 0;

  if (!isLoaded) {
    return (
      <div className="w-full h-full bg-gray-200 dark:bg-gray-800 flex items-center justify-center">
        <p className="text-gray-600 dark:text-gray-400 text-lg">Loading map...</p>
      </div>
    );
  }

  return (
    <div className="relative w-full h-full">
      <MapContainer
        center={initialView?.center ?? (userLocation ? [userLocation.lat, userLocation.lng] : defaultCenter)}
        zoom={initialView?.zoom ?? (userLocation ? 15 : 13)}
        style={{ height: '100%', width: '100%' }}
        className="z-0"
      >
        <MapViewTracker onViewChange={onViewChange} />
        <TileLayer
          url={MAP_STYLES[currentStyle as keyof typeof MAP_STYLES].url}
          attribution={MAP_STYLES[currentStyle as keyof typeof MAP_STYLES].attribution}
        />

        {/* Heatmap layer from zone scores - only in edit mode */}
        {editMode && heatmapPoints.length > 0 && (
          <HeatmapLayer
            points={heatmapPoints}
            longitudeExtractor={(p: { lng: number }) => p.lng}
            latitudeExtractor={(p: { lat: number }) => p.lat}
            intensityExtractor={(p: { intensity: number }) => p.intensity}
            radius={50}
            blur={50}
            max={1}
            opacity={0.5}
            gradient={{
              0.0: '#0000ff',
              0.2: '#00bfff',
              0.4: '#00e5a0',
              0.5: '#00cc00',
              0.7: '#ffdd00',
              0.85: '#ff6600',
              1.0: '#ff0000',
            }}
          />
        )}

        {/* User Location */}
        {userLocation && (
          <>
            <Marker position={[userLocation.lat, userLocation.lng]} icon={createUserIcon()}>
              <Popup>
                <b>Your Location</b><br/>
                Lat: {userLocation.lat.toFixed(6)}<br/>
                Lng: {userLocation.lng.toFixed(6)}<br/>
                Accuracy: &plusmn;{Math.round(userLocation.accuracy || 0)} meters
              </Popup>
            </Marker>
            <Circle
              center={[userLocation.lat, userLocation.lng]}
              radius={userLocation.accuracy || 10}
              color="#4CAF50"
              weight={1}
              fillColor="#4CAF50"
              fillOpacity={0.1}
            />
          </>
        )}

        {/* Route edges - colored polylines */}
        {edgePolylines.map((edge, i) => (
          <Polyline
            key={`edge-${i}`}
            positions={edge.positions}
            color={edge.lineId === (serviceLine?.lineId ?? selectedLineId) ? '#e06080' : edge.color}
            weight={edge.lineId === (serviceLine?.lineId ?? selectedLineId) ? 12 : (edge.isBus ? 6 : 5)}
            opacity={edge.isBus ? 0.6 : 1}
            eventHandlers={{
              click: (e) => {
                // Always toggle bus stop visibility when clicking a bus line
                if (edge.isBus) {
                  setSelectedLineId(selectedLineId === edge.lineId ? null : edge.lineId);
                }
                // In edit mode, also show service slider for any line
                if (editMode) {
                  const latlng = e.latlng;
                  const line = lines.find(l => l.lineId === edge.lineId);
                  if (line) {
                    setServiceLine({
                      lineId: line.lineId,
                      lineName: line.lineName,
                      color: edge.color,
                      mode: line.mode,
                      clickLatLng: [latlng.lat, latlng.lng],
                    });
                  }
                }
              },
            }}
          />
        ))}

        {/* Stop markers - circles for train, squares for bus */}
        {stopMarkers.map(({ stop, lines: stopLines }) => {
          const primaryLine = stopLines[0];
          const isMoved = movedStops.has(stop.mbtaStopId);
          const icon = createStopIcon(isMoved ? '#FFD700' : primaryLine.color, primaryLine.mode);
          return (
            <Marker
              key={`stop-${stop.mbtaStopId}`}
              position={[stop.lat, stop.lng]}
              icon={icon}
              draggable={editMode}
              eventHandlers={editMode ? {
                dragend: (e) => {
                  const marker = e.target as L.Marker;
                  const pos = marker.getLatLng();
                  onStopMoved(stop.stopId, pos.lat, pos.lng, stop.mbtaStopId);
                },
              } : {}}
            >
              <Popup>
                <div style={{ minWidth: 160 }}>
                  <b style={{ fontSize: 14 }}>{stop.name}</b>
                  <div style={{ fontSize: 11, color: '#666', marginTop: 4 }}>
                    {stop.mbtaStopId}
                  </div>
                  <div style={{ marginTop: 6 }}>
                    {stopLines.map((l, i) => (
                      <span
                        key={i}
                        style={{
                          display: 'inline-block',
                          backgroundColor: l.color,
                          color: 'white',
                          padding: '2px 8px',
                          borderRadius: 4,
                          fontSize: 11,
                          marginRight: 4,
                          marginBottom: 4,
                        }}
                      >
                        {l.name}
                      </span>
                    ))}
                  </div>
                  <div style={{ fontSize: 11, color: '#888', marginTop: 4 }}>
                    {stopLines[0].mode === 'bus' ? 'Bus Stop' : 'Train Station'}
                  </div>
                  <div style={{ fontSize: 10, color: '#aaa', marginTop: 2 }}>
                    {stop.lat.toFixed(5)}, {stop.lng.toFixed(5)}
                  </div>
                </div>
              </Popup>
            </Marker>
          );
        })}
      </MapContainer>

      {/* Service Level Slider Panel */}
      {editMode && serviceLine && (() => {
        const isBus = serviceLine.mode === 'bus';
        const defaultCars = isBus ? 5 : 4;
        const currentCars = serviceLevels.get(serviceLine.lineId) ?? defaultCars;
        const maxCars = isBus ? 30 : 12;
        return (
          <div
            className="absolute z-50 p-4 rounded-xl shadow-2xl"
            style={{
              top: 80,
              left: '50%',
              transform: 'translateX(-50%)',
              background: 'rgba(15, 15, 25, 0.92)',
              backdropFilter: 'blur(16px)',
              border: '1px solid rgba(255,255,255,0.1)',
              minWidth: 280,
            }}
          >
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div
                  style={{
                    width: 12,
                    height: 12,
                    borderRadius: isBus ? 2 : '50%',
                    backgroundColor: serviceLine.color,
                  }}
                />
                <span className="text-white font-bold text-sm">{serviceLine.lineName}</span>
              </div>
              <button
                onClick={() => setServiceLine(null)}
                className="text-gray-400 hover:text-white text-lg leading-none"
              >
                &times;
              </button>
            </div>
            <p className="text-xs text-gray-400 mb-2">
              {isBus ? 'Number of buses on this route' : 'Number of train cars on this line'}
            </p>
            <div className="flex items-center gap-3">
              <span className="text-xs text-gray-500">1</span>
              <input
                type="range"
                min={1}
                max={maxCars}
                step={1}
                value={currentCars}
                onChange={(e) => {
                  const val = Number(e.target.value);
                  setServiceLevels(prev => {
                    const next = new Map(prev);
                    next.set(serviceLine.lineId, val);
                    return next;
                  });
                }}
                onMouseUp={() => {
                  onServiceChanged(serviceLine.lineId, currentCars);
                }}
                onTouchEnd={() => {
                  onServiceChanged(serviceLine.lineId, currentCars);
                }}
                className="flex-1 cursor-pointer"
                style={{ accentColor: serviceLine.color }}
              />
              <span className="text-xs text-gray-500">{maxCars}</span>
            </div>
            <div className="text-center mt-2">
              <span className="text-sm font-mono font-bold text-white">
                {currentCars} {isBus ? (currentCars === 1 ? 'bus' : 'buses') : (currentCars === 1 ? 'car' : 'cars')}
              </span>
              {currentCars !== defaultCars && (
                <span
                  className="text-xs ml-2"
                  style={{ color: currentCars > defaultCars ? '#4ade80' : '#f87171' }}
                >
                  ({currentCars > defaultCars ? '+' : ''}{currentCars - defaultCars} from default)
                </span>
              )}
            </div>
          </div>
        );
      })()}
    </div>
  );
};

export default MapClient;
