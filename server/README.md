# TransitShark Backend

Spring Boot backend for the TransitShark transit network optimization tool. Fetches live data from the MBTA API, computes zones from stop locations, and scores them based on synthesized demand vs. transit service coverage.

## Tech Stack
- Java 21, Spring Boot 3.3.5
- PostgreSQL (Supabase) — stores generated demand datasets
- MBTA V3 API — live stop, route, and schedule data
- Spring Data JPA, Hibernate
- SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`)

## Before Running
1. Create a `server/.env` file with your MBTA API key:
   ```
   MBTA_API_KEY=your-key-here
   ```
   Get a free key at https://api-v3.mbta.com

2. Supabase credentials are in `src/main/resources/application.yml` — update if needed.

## Run
```bash
./mvnw spring-boot:run
```
Or in IntelliJ: open as Maven project, run `MbtaOptimizerApplication`.

Server starts on port **5777**.

## Geographic Bounds
All endpoints are clipped to metro Boston (`GeoBounds.java`):
```
Lat: 42.23 → 42.45   (Quincy edge → Medford/Malden edge)
Lng: -71.18 → -70.99  (Watertown/Newton edge → East Boston/harbor)
```
Bus routes to Cape Cod, far suburbs, etc. are excluded.

---

## API Endpoints

### GET `/api/heatmap`
Returns demand aggregated by zone for heatmap display. Uses the active dataset if one is selected, otherwise falls back to synthesized demand.

**Request**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `timeOfDay` | Integer (query) | 13 | Hour of day (0-23) |

**Response** `200 OK`
```json
[
  {
    "zoneId": "Z-001",
    "zoneName": "Z-001",
    "demand": 0.65,
    "centerLat": 42.35,
    "centerLng": -71.06
  }
]
```

---

### GET `/api/ridership-demand`
Returns per-stop ridership demand for a given hour.

**Request**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `timeOfDay` | Integer (query) | 13 | Hour of day (0-23) |

**Response** `200 OK`
```json
[
  {
    "stopId": 1,
    "mbtaStopId": "place-pktrm",
    "name": "Park Street",
    "demand": 74.3,
    "lat": 42.3564,
    "lng": -71.0624
  }
]
```

---

### GET `/api/optimization`
Returns all stops ranked by optimization need (high demand + low connectivity = high priority).

**Request**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `timeOfDay` | Integer (query) | 13 | Hour of day (0-23) |

**Response** `200 OK` — sorted descending by demand
```json
[
  {
    "stopId": 42,
    "mbtaStopId": "place-sstat",
    "name": "South Station",
    "demand": 91.2,
    "lat": 42.3523,
    "lng": -71.0552
  }
]
```

---

### GET `/api/zones`
Returns all zones with location coordinates. Zones are computed as a ~2km grid over all in-bounds MBTA stops. Deterministic: same stops produce the same zone IDs every time.

**Response** `200 OK`
```json
[
  {
    "zoneId": "Z-001",
    "name": "Z-001",
    "centerLat": 42.35,
    "centerLng": -71.06,
    "minLat": 42.34,
    "minLng": -71.07,
    "maxLat": 42.36,
    "maxLng": -71.05
  }
]
```

---

### GET `/api/zones/scores`
Calculates a score (0-100) for every zone. High score = high demand but poor service (needs optimization).

**Score formula:**
- **Demand** — from the active dataset (`populationDensity`, `jobDensity`, `carOwnership`) or synthesized from city-center distance
- **Service** — nearby stops weighted by `frequency / distance` (rail > bus)
- **Zone score** = `50 + (demand - service) * 50`, clamped to [0, 100]

**Response** `200 OK`
```json
[
  {
    "zoneId": "Z-001",
    "zoneName": "Z-001",
    "score": 62.5
  }
]
```

---

### POST `/api/modify-stop`
Modify a stop (move or delete) and get recalculated zone scores.

**Request Body**
```json
{
  "stopId": 1,
  "modificationType": "MOVE",
  "modification": {
    "newLat": 42.358,
    "newLng": -71.060
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `stopId` | Long | yes | ID of the stop to modify |
| `modificationType` | String | yes | `MOVE` or `DELETE` |
| `modification.newLat` | Double | for MOVE | New latitude |
| `modification.newLng` | Double | for MOVE | New longitude |

**Response** `200 OK` — recalculated `List<ZoneScore>`

---

### POST `/api/modify-line`
Modify a transit line and get recalculated zone scores. `time` refers to a 15-minute window starting at the specified hour.

**Request Body**
```json
{
  "line": 5,
  "modificationType": "INCR_SERVICE",
  "modification": {
    "stopId": 12,
    "frequencyChangeMinutes": 5
  },
  "time": 8
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `line` | Long | yes | Route/line ID |
| `modificationType` | String | yes | `INCR_SERVICE`, `DECR_SERVICE`, `ADD_STOP`, or `REMOVE_STOP` |
| `modification.stopId` | Long | for ADD/REMOVE | Stop to add or remove |
| `modification.frequencyChangeMinutes` | Integer | for INCR/DECR | Frequency adjustment |
| `modification.stopName` | String | optional | Name for new stop |
| `modification.lat` | Double | optional | Latitude for new stop |
| `modification.lng` | Double | optional | Longitude for new stop |
| `time` | Integer | optional | Hour of day for 15-min window |

**Response** `200 OK`
```json
{
  "zoneScores": [
    { "zoneId": "Z-001", "zoneName": "Z-001", "score": 75.0 }
  ],
  "time": 8
}
```

---

### GET `/api/network`
Returns the transit network from the live MBTA API, clipped to metro Boston bounds.

**Request**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `type` | String (query) | `0,1` | Route type filter. `0`=LightRail, `1`=HeavyRail, `2`=CommuterRail, `3`=Bus, `4`=Ferry |

**Response** `200 OK`
```json
{
  "lines": [
    {
      "lineId": 2602613,
      "mbtaRouteId": "Red",
      "lineName": "Red Line",
      "color": "DA291C",
      "mode": "heavy_rail",
      "stops": [
        {
          "stopId": 1,
          "mbtaStopId": "place-alfcl",
          "name": "Alewife",
          "lat": 42.3954,
          "lng": -71.1426
        }
      ],
      "edges": [
        {
          "fromStopId": 1,
          "toStopId": 2,
          "distanceMeters": 1830.0
        }
      ]
    }
  ]
}
```

---

### POST `/api/datasets/generate`
Generate a random demand dataset for all zones and store in Supabase. Per-zone factors follow realistic city distributions (higher density near center, more car ownership in suburbs) with randomness.

**Request**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `seed` | Long (query) | no | Random seed for reproducibility |

**Response** `200 OK`
```json
{
  "id": 1,
  "name": "dataset-20260404-183022",
  "active": false,
  "createdAt": "2026-04-04T18:30:22Z",
  "zones": [
    {
      "zoneId": "Z-001",
      "populationDensity": 0.82,
      "jobDensity": 0.91,
      "carOwnership": 0.35
    }
  ]
}
```

---

### GET `/api/datasets`
List all generated datasets (without zone-level detail).

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "name": "dataset-20260404-183022",
    "active": true,
    "createdAt": "2026-04-04T18:30:22Z"
  }
]
```

---

### GET `/api/datasets/{id}`
Get a single dataset with all zone-level data.

**Response** `200 OK` — same shape as generate response.

---

### POST `/api/datasets/{id}/activate`
Select a dataset as active. Deactivates any previously active dataset. Zone scoring endpoints will use this dataset's demand factors.

**Response** `200 OK` — the activated dataset with zone data.

---

### DELETE `/api/datasets/{id}`
Delete a dataset and its zone data.

**Response** `204 No Content`

---

## Error Responses

```json
{
  "timestamp": "2026-04-04T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Stop not found: 999"
}
```

| Status | When |
|--------|------|
| `400` | Validation failure or invalid modification type |
| `404` | Stop, route, or dataset not found |
| `500` | Unexpected server error |
