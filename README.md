# MBTA Optimizer Backend

Spring Boot backend starter for an MBTA routing optimization hackathon project.

## What is included
- One-map backend design with network + heatmap endpoints
- Scenario system for moving stops / adding stops / changing frequencies
- Optimization endpoint with score, savings, cost, profit, CO2, and shark mascot mood
- Suggestion details endpoint for a frontend side panel or modal
- MBTA raw API proxy endpoints
- PostgreSQL-ready JPA structure

## Main frontend-friendly endpoints
- `GET /api/map/network`
- `GET /api/map/heatmap?areaCode=downtown&hour=13&metricType=DEMAND`
- `POST /api/scenarios`
- `POST /api/scenarios/{id}/move-stop`
- `POST /api/scenarios/{id}/add-stop`
- `POST /api/scenarios/{id}/change-frequency`
- `POST /api/optimization/{id}/run`
- `GET /api/optimization/{id}/details`

## Why the details endpoint matters
Use `GET /api/optimization/{id}/details` to power the frontend section where the user wants more explanation and supporting data about the suggestion.

Suggested UI usage:
- main map shows network or heatmap
- score cards show summary metrics
- a button like `See More Details` opens a side panel or modal
- that panel calls `/api/optimization/{scenarioId}/details`

## Before running
Update `src/main/resources/application.yml` with your real PostgreSQL or Supabase connection values.

If you use Supabase, change:
- datasource url
- username
- password

## Run
```bash
./mvnw spring-boot:run
```

or in IntelliJ:
- open as Maven project
- let dependencies download
- run `MbtaOptimizerApplication`

## Important note
The optimization logic is currently a hackathon-safe starter, not a full transit optimizer yet.
Replace `PythonOptimizerClient` with a real FastAPI or Flask service when ready.
