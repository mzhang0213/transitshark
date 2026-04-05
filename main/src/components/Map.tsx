'use client';

import dynamic from 'next/dynamic';
import 'leaflet/dist/leaflet.css';
import type { TransitLine, ZoneInfo, ZoneScore } from '@/types/transit';

const MapComponent = dynamic(() => import('./MapClient'), {
  ssr: false,
  loading: () => (
    <div className="w-full h-full bg-gray-200 dark:bg-gray-800 flex items-center justify-center">
      <p className="text-gray-600 dark:text-gray-400 text-lg">Loading map...</p>
    </div>
  )
}) as React.ComponentType<MapProps>;

interface MapProps {
  lines: TransitLine[];
  zones: ZoneInfo[];
  zoneScores: ZoneScore[];
  editMode: boolean;
  movedStops: Set<string>;
  scoreVersion: number;
  onStopMoved: (stopId: number, newLat: number, newLng: number, mbtaStopId: string) => void;
  onServiceChanged: (lineId: number, serviceLevel: number) => void;
}

const Map = (props: MapProps) => {
  return (
    <div className="w-full h-full">
      <MapComponent {...props} />
    </div>
  );
};

export default Map;
