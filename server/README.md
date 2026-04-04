# TransitShark Backend

Spring Boot backend for the TransitShark transit network optimization tool.

## Tech Stack
- Java 21, Spring Boot 3.3.5
- PostgreSQL (Supabase)
- Spring Data JPA, Hibernate
- SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`)

## Before Running
Update `src/main/resources/application.yml` with your PostgreSQL/Supabase credentials:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## Run
```bash
./mvnw spring-boot:run
```
Or in IntelliJ: open as Maven project, run `MbtaOptimizerApplication`.

Server starts on port **5777**.

---

## API Endpoints

### GET `/api/heatmap`
Returns demand aggregated by zone for heatmap display.

**Request**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `timeOfDay` | Integer (query) | 13 | Hour of day (0-23) |

**Response** `200 OK` — `List<ZoneDemand>`
```json
[
  {
    "zoneId": "Z001",
    "zoneName": "Downtown Boston",
    "demand": 82.5,
    "centerLat": 42.3601,
    "centerLng": -71.0589
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

**Response** `200 OK` — `List<StopDemand>`
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

**Response** `200 OK` — `List<StopDemand>` (sorted descending by demand)
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
Returns all zones with their location coordinates.

**Request** — none

**Response** `200 OK` — `List<ZoneInfo>`
```json
[
  {
    "zoneId": "Z001",
    "name": "Downtown Boston",
    "centerLat": 42.3601,
    "centerLng": -71.0589,
    "minLat": 42.3500,
    "minLng": -71.0700,
    "maxLat": 42.3700,
    "maxLng": -71.0480
  }
]
```

---

### GET `/api/zones/scores`
Calculates and returns a service coverage score (0-100) for every zone.

Score factors:
- Number of active stops in the zone (up to 50 pts)
- Number of route segments touching the zone (up to 50 pts)

**Request** — none

**Response** `200 OK` — `List<ZoneScore>`
```json
[
  {
    "zoneId": "Z001",
    "zoneName": "Downtown Boston",
    "score": 72.5
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
    "newLat": 42.3580,
    "newLng": -71.0600
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `stopId` | Long | yes | ID of the stop to modify |
| `modificationType` | String | yes | `MOVE` or `DELETE` |
| `modification.newLat` | Double | for MOVE | New latitude |
| `modification.newLng` | Double | for MOVE | New longitude |

**Response** `200 OK` — `List<ZoneScore>` (recalculated)
```json
[
  { "zoneId": "Z001", "zoneName": "Downtown Boston", "score": 68.0 },
  { "zoneId": "Z002", "zoneName": "Back Bay", "score": 55.5 }
]
```

---

### POST `/api/modify-line`
Modify a transit line and get recalculated zone scores. The `time` param refers to a 15-minute window starting at the specified hour.

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

**Response** `200 OK` — `ModifyLineResponse`
```json
{
  "zoneScores": [
    { "zoneId": "Z001", "zoneName": "Downtown Boston", "score": 75.0 },
    { "zoneId": "Z002", "zoneName": "Back Bay", "score": 60.0 }
  ],
  "time": 8
}
```

---

### GET `/api/network`
Returns the full transit network graph: all lines with their stops and edges.

**Request** — none

**Response** `200 OK` — `NetworkResponse`
```json
{
  "lines": [
    {
      "lineId": 1,
      "lineName": "Red Line",
      "color": "DA291C",
      "mode": "subway",
      "stops": [
        {
          "stopId": 1,
          "mbtaStopId": "place-alfcl",
          "name": "Alewife",
          "lat": 42.3954,
          "lng": -71.1426
        },
        {
          "stopId": 2,
          "mbtaStopId": "place-davis",
          "name": "Davis",
          "lat": 42.3967,
          "lng": -71.1219
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

## Error Responses

All errors return a consistent JSON format:
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
| `404` | Stop, route, or zone not found |
| `500` | Unexpected server error |
