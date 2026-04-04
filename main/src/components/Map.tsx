'use client';

import dynamic from 'next/dynamic';
import 'leaflet/dist/leaflet.css';
import type { TransitLine } from '@/types/transit';

const MapComponent = dynamic(() => import('./MapClient'), {
  ssr: false,
  loading: () => (
    <div className="w-full h-full bg-gray-200 dark:bg-gray-800 flex items-center justify-center">
      <p className="text-gray-600 dark:text-gray-400 text-lg">Loading map...</p>
    </div>
  )
}) as React.ComponentType<{ lines: TransitLine[] }>;

interface MapProps {
  lines: TransitLine[];
}

const Map = ({ lines }: MapProps) => {
  return (
    <div className="w-full h-full">
      <MapComponent lines={lines} />
    </div>
  );
};

export default Map;
