export interface TransitStop {
  stopId: number;
  mbtaStopId: string;
  name: string;
  lat: number;
  lng: number;
}

export interface TransitEdge {
  fromStopId: number;
  toStopId: number;
  distanceMeters: number;
}

export interface TransitLine {
  lineId: number;
  lineName: string;
  color: string;
  mode: string;
  stops: TransitStop[];
  edges: TransitEdge[];
}

export interface ZoneInfo {
  zoneId: string;
  name: string;
  centerLat: number;
  centerLng: number;
  minLat: number;
  minLng: number;
  maxLat: number;
  maxLng: number;
}

export interface ZoneScore {
  zoneId: string;
  zoneName: string;
  score: number;
}
