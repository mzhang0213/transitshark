package com.izikwen.mbtaoptimizer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.izikwen.mbtaoptimizer.client.MbtaApiClient;
import com.izikwen.mbtaoptimizer.config.GeoBounds;
import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse;
import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse.LineDto;
import com.izikwen.mbtaoptimizer.dto.response.ZoneInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Central in-memory state for the entire transit system.
 * Caches stops, lines, zones, and score computation results.
 * Lazy-loaded on first access; call refresh() to reload from MBTA API.
 */
@Component
public class TransitState {

    private static final Logger log = LoggerFactory.getLogger(TransitState.class);
    private static final int MAX_SCORE_SNAPSHOTS = 50;

    private final MbtaApiClient mbtaClient;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // ---- cached transit data -------------------------------------------

    private List<Stop> stops = List.of();
    private Map<String, Line> lines = Map.of();
    private List<ZoneInfoResponse> zones = List.of();
    private Instant lastRefresh;
    private boolean loaded = false;

    // ---- score tracking ------------------------------------------------

    private final Deque<ScoreSnapshot> scoreHistory = new ConcurrentLinkedDeque<>();
    private Map<String, ZoneScore> latestScores = Map.of();

    // ---- records -------------------------------------------------------

    /** A transit stop with location, type, name, and line membership. */
    public record Stop(String mbtaStopId, String name,
                       double lat, double lng, int routeType,
                       Set<String> lineIds) {}

    /** A transit line with its ordered stops and edges. */
    public record Line(String mbtaRouteId, String name, String color,
                       String mode, List<LineStop> stops,
                       List<Edge> edges) {}

    public record LineStop(String mbtaStopId, String name,
                           double lat, double lng) {}

    public record Edge(String fromStopId, String toStopId,
                       double distanceMeters) {}

    /** Per-zone score breakdown — all inputs that went into the computation. */
    public record ZoneScore(String zoneId, double centerLat, double centerLng,
                            double demand, double service, double score) {}

    /** A timestamped snapshot of a full score computation. */
    public record ScoreSnapshot(String id, Instant timestamp, String source,
                                int zoneCount, double meanScore,
                                double medianScore, double minScore,
                                double maxScore, List<ZoneScore> zones) {}

    public record ScoreSnapshotSummary(String id, Instant timestamp, String source,
                                       int zoneCount, double meanScore,
                                       double medianScore, double minScore,
                                       double maxScore) {}

    // ---- constructor ---------------------------------------------------

    public TransitState(MbtaApiClient mbtaClient) {
        this.mbtaClient = mbtaClient;
    }

    // ---- lazy load / refresh -------------------------------------------

    private void ensureLoaded() {
        lock.readLock().lock();
        try {
            if (loaded) return;
        } finally {
            lock.readLock().unlock();
        }
        refresh();
    }

    /** Reload all transit data from the MBTA API. */
    public void refresh() {
        lock.writeLock().lock();
        try {
            log.info("Refreshing transit state from MBTA API...");
            this.stops = fetchAllStops();
            this.lines = fetchAllLines();
            assignLinesToStops();
            this.zones = List.of(); // zones computed by ZoneService using getStops()
            this.lastRefresh = Instant.now();
            this.loaded = true;
            log.info("Transit state loaded: {} stops, {} lines",
                    stops.size(), lines.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ---- public accessors ----------------------------------------------

    public List<Stop> getStops() {
        ensureLoaded();
        lock.readLock().lock();
        try { return stops; }
        finally { lock.readLock().unlock(); }
    }

    public Map<String, Line> getLines() {
        ensureLoaded();
        lock.readLock().lock();
        try { return lines; }
        finally { lock.readLock().unlock(); }
    }

    public List<ZoneInfoResponse> getZones() {
        lock.readLock().lock();
        try { return zones; }
        finally { lock.readLock().unlock(); }
    }

    public void setZones(List<ZoneInfoResponse> zones) {
        lock.writeLock().lock();
        try { this.zones = List.copyOf(zones); }
        finally { lock.writeLock().unlock(); }
    }

    public Instant getLastRefresh() { return lastRefresh; }

    /** Move a stop in-memory by mbtaStopId. Updates both the stops list and line stops. */
    public boolean moveStop(String mbtaStopId, double newLat, double newLng) {
        ensureLoaded();
        lock.writeLock().lock();
        try {
            // Update in stops list
            List<Stop> updatedStops = new ArrayList<>();
            boolean found = false;
            for (Stop s : stops) {
                if (s.mbtaStopId.equals(mbtaStopId)) {
                    updatedStops.add(new Stop(s.mbtaStopId, s.name, newLat, newLng, s.routeType, s.lineIds));
                    found = true;
                } else {
                    updatedStops.add(s);
                }
            }
            if (!found) return false;
            this.stops = List.copyOf(updatedStops);

            // Update in line stops
            Map<String, Line> updatedLines = new LinkedHashMap<>();
            for (var entry : lines.entrySet()) {
                Line line = entry.getValue();
                List<LineStop> updatedLineStops = new ArrayList<>();
                for (LineStop ls : line.stops) {
                    if (ls.mbtaStopId.equals(mbtaStopId)) {
                        updatedLineStops.add(new LineStop(ls.mbtaStopId, ls.name, newLat, newLng));
                    } else {
                        updatedLineStops.add(ls);
                    }
                }
                // Rebuild edges with updated positions
                List<Edge> updatedEdges = new ArrayList<>();
                for (int i = 0; i < updatedLineStops.size() - 1; i++) {
                    LineStop from = updatedLineStops.get(i);
                    LineStop to = updatedLineStops.get(i + 1);
                    updatedEdges.add(new Edge(from.mbtaStopId, to.mbtaStopId,
                            haversineM(from.lat, from.lng, to.lat, to.lng)));
                }
                updatedLines.put(entry.getKey(),
                        new Line(line.mbtaRouteId, line.name, line.color, line.mode,
                                updatedLineStops, updatedEdges));
            }
            this.lines = Map.copyOf(updatedLines);

            // Invalidate cached zones so they recompute
            this.zones = List.of();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Build a NetworkResponse from cached line data (for /api/network). */
    public NetworkResponse toNetworkResponse(String filterType) {
        ensureLoaded();
        Set<String> allowedModes = parseModeFilter(filterType);
        lock.readLock().lock();
        try {
            List<NetworkResponse.LineDto> dtos = new ArrayList<>();
            for (Line line : lines.values()) {
                if (!allowedModes.contains(line.mode)) continue;

                NetworkResponse.LineDto dto = new NetworkResponse.LineDto();
                dto.setLineId((long) line.mbtaRouteId.hashCode() & 0xFFFFFFFFL);
                dto.setMbtaRouteId(line.mbtaRouteId);
                dto.setLineName(line.name);
                dto.setColor(line.color);
                dto.setMode(line.mode);

                List<NetworkResponse.StopDto> stopDtos = new ArrayList<>();
                long idCounter = 1;
                for (LineStop ls : line.stops) {
                    stopDtos.add(new NetworkResponse.StopDto(
                            idCounter++, ls.mbtaStopId, ls.name, ls.lat, ls.lng));
                }
                dto.setStops(stopDtos);

                List<NetworkResponse.EdgeDto> edgeDtos = new ArrayList<>();
                for (int i = 0; i < stopDtos.size() - 1; i++) {
                    var from = stopDtos.get(i);
                    var to = stopDtos.get(i + 1);
                    double dist = haversineM(from.getLat(), from.getLng(),
                            to.getLat(), to.getLng());
                    edgeDtos.add(new NetworkResponse.EdgeDto(
                            from.getStopId(), to.getStopId(),
                            Math.round(dist * 10.0) / 10.0));
                }
                dto.setEdges(edgeDtos);
                dtos.add(dto);
            }
            return new NetworkResponse(dtos);
        } finally {
            lock.readLock().unlock();
        }
    }

    // ---- score tracking ------------------------------------------------

    public ScoreSnapshot recordScores(String source, List<ZoneScore> zones) {
        double[] scores = zones.stream().mapToDouble(ZoneScore::score).sorted().toArray();

        ScoreSnapshot snap = new ScoreSnapshot(
                UUID.randomUUID().toString().substring(0, 8),
                Instant.now(), source, zones.size(),
                Arrays.stream(scores).average().orElse(0),
                median(scores),
                scores.length > 0 ? scores[0] : 0,
                scores.length > 0 ? scores[scores.length - 1] : 0,
                List.copyOf(zones));

        scoreHistory.addFirst(snap);
        while (scoreHistory.size() > MAX_SCORE_SNAPSHOTS) scoreHistory.removeLast();

        // Update latest scores map
        Map<String, ZoneScore> map = new LinkedHashMap<>();
        for (ZoneScore zs : zones) map.put(zs.zoneId, zs);
        this.latestScores = Map.copyOf(map);

        exportCsv(snap);
        return snap;
    }

    public Map<String, ZoneScore> getLatestScores() { return latestScores; }

    public List<ScoreSnapshotSummary> getScoreHistory() {
        return scoreHistory.stream()
                .map(s -> new ScoreSnapshotSummary(s.id, s.timestamp, s.source,
                        s.zoneCount, s.meanScore, s.medianScore,
                        s.minScore, s.maxScore))
                .toList();
    }

    public Optional<ScoreSnapshot> getScoreSnapshot(String id) {
        return scoreHistory.stream().filter(s -> s.id.equals(id)).findFirst();
    }

    // ---- summary for /api/state ----------------------------------------

    public Map<String, Object> getSummary() {
        ensureLoaded();
        lock.readLock().lock();
        try {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("lastRefresh", lastRefresh);

            // Stop stats
            Map<String, Object> stopStats = new LinkedHashMap<>();
            stopStats.put("total", stops.size());
            Map<String, Long> byType = new LinkedHashMap<>();
            for (Stop s : stops) {
                String mode = modeForType(s.routeType);
                byType.merge(mode, 1L, Long::sum);
            }
            stopStats.put("byMode", byType);
            summary.put("stops", stopStats);

            // Line stats
            Map<String, Object> lineStats = new LinkedHashMap<>();
            lineStats.put("total", lines.size());
            Map<String, Long> linesByMode = new LinkedHashMap<>();
            for (Line l : lines.values()) {
                linesByMode.merge(l.mode, 1L, Long::sum);
            }
            lineStats.put("byMode", linesByMode);
            summary.put("lines", lineStats);

            // Zone stats
            summary.put("zoneCount", zones.size());

            // Score stats
            if (!latestScores.isEmpty()) {
                double[] vals = latestScores.values().stream()
                        .mapToDouble(ZoneScore::score).sorted().toArray();
                Map<String, Object> scoreStats = new LinkedHashMap<>();
                scoreStats.put("count", vals.length);
                scoreStats.put("mean", round2(Arrays.stream(vals).average().orElse(0)));
                scoreStats.put("median", round2(median(vals)));
                scoreStats.put("min", round2(vals[0]));
                scoreStats.put("max", round2(vals[vals.length - 1]));
                scoreStats.put("snapshotsRecorded", scoreHistory.size());
                summary.put("scores", scoreStats);
            }

            return summary;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ---- MBTA data fetching --------------------------------------------

    private List<Stop> fetchAllStops() {
        Map<String, Stop> stopMap = new LinkedHashMap<>();
        for (String typeFilter : List.of("0,1", "2", "3")) {
            int defaultType = switch (typeFilter) {
                case "0,1" -> 1;
                case "2" -> 2;
                default -> 3;
            };
            try {
                JsonNode json = mbtaClient.getAllStops(typeFilter);
                for (JsonNode node : json.path("data")) {
                    String id = node.path("id").asText();
                    double lat = node.path("attributes").path("latitude").asDouble(0);
                    double lng = node.path("attributes").path("longitude").asDouble(0);
                    String name = node.path("attributes").path("name").asText("Unknown");
                    int vt = node.path("attributes").path("vehicle_type").asInt(defaultType);
                    if (lat != 0 && lng != 0 && GeoBounds.inBounds(lat, lng)) {
                        stopMap.put(id, new Stop(id, name, lat, lng, vt, new HashSet<>()));
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch stops for type {}: {}", typeFilter, e.getMessage());
            }
        }
        return List.copyOf(stopMap.values());
    }

    private Map<String, Line> fetchAllLines() {
        Map<String, Line> lineMap = new LinkedHashMap<>();
        for (String typeFilter : List.of("0,1", "2", "3")) {
            try {
                JsonNode routesJson = mbtaClient.getRoutes(typeFilter);
                for (JsonNode routeNode : routesJson.path("data")) {
                    String routeId = routeNode.path("id").asText();
                    JsonNode attrs = routeNode.path("attributes");
                    String longName = attrs.path("long_name").asText(null);
                    String shortName = attrs.path("short_name").asText(null);
                    String color = attrs.path("color").asText(null);
                    int type = attrs.path("type").asInt(-1);
                    String mode = modeForType(type);

                    List<LineStop> lineStops = fetchLineStops(routeId);
                    lineStops = lineStops.stream()
                            .filter(s -> GeoBounds.inBounds(s.lat, s.lng))
                            .toList();
                    if (lineStops.isEmpty()) continue;

                    List<Edge> edges = new ArrayList<>();
                    for (int i = 0; i < lineStops.size() - 1; i++) {
                        LineStop from = lineStops.get(i);
                        LineStop to = lineStops.get(i + 1);
                        edges.add(new Edge(from.mbtaStopId, to.mbtaStopId,
                                haversineM(from.lat, from.lng, to.lat, to.lng)));
                    }

                    String name = longName != null && !longName.isEmpty() ? longName : shortName;
                    lineMap.put(routeId, new Line(routeId, name, color, mode, lineStops, edges));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch routes for type {}: {}", typeFilter, e.getMessage());
            }
        }
        return Map.copyOf(lineMap);
    }

    private List<LineStop> fetchLineStops(String routeId) {
        try {
            JsonNode rpJson = mbtaClient.getRoutePatterns(routeId);
            JsonNode included = rpJson.path("included");
            Map<String, JsonNode> stopNodes = new LinkedHashMap<>();
            Map<String, JsonNode> tripNodes = new LinkedHashMap<>();
            for (JsonNode inc : included) {
                String incType = inc.path("type").asText();
                String id = inc.path("id").asText();
                if ("stop".equals(incType)) stopNodes.put(id, inc);
                else if ("trip".equals(incType)) tripNodes.put(id, inc);
            }

            JsonNode patterns = rpJson.path("data");
            if (patterns.isEmpty()) return List.of();

            JsonNode chosen = null;
            for (JsonNode pat : patterns) {
                if (pat.path("attributes").path("direction_id").asInt(-1) == 0) {
                    chosen = pat;
                    break;
                }
            }
            if (chosen == null) chosen = patterns.get(0);

            String tripId = chosen.path("relationships")
                    .path("representative_trip").path("data").path("id").asText(null);
            if (tripId == null) return List.of();

            JsonNode tripNode = tripNodes.get(tripId);
            if (tripNode == null) return List.of();

            JsonNode tripStops = tripNode.path("relationships").path("stops").path("data");
            if (tripStops.isMissingNode() || !tripStops.isArray()) return List.of();

            List<LineStop> result = new ArrayList<>();
            for (JsonNode ref : tripStops) {
                String stopId = ref.path("id").asText();
                JsonNode sn = stopNodes.get(stopId);
                if (sn == null) continue;
                JsonNode sa = sn.path("attributes");
                result.add(new LineStop(stopId,
                        sa.path("name").asText("Unknown"),
                        sa.path("latitude").asDouble(0),
                        sa.path("longitude").asDouble(0)));
            }
            return result;
        } catch (Exception e) {
            log.warn("Route patterns failed for {}, using fallback", routeId);
            return fetchLineStopsFallback(routeId);
        }
    }

    private List<LineStop> fetchLineStopsFallback(String routeId) {
        try {
            JsonNode stopsJson = mbtaClient.getStopsForRoute(routeId);
            List<LineStop> result = new ArrayList<>();
            for (JsonNode node : stopsJson.path("data")) {
                JsonNode sa = node.path("attributes");
                result.add(new LineStop(node.path("id").asText(),
                        sa.path("name").asText("Unknown"),
                        sa.path("latitude").asDouble(0),
                        sa.path("longitude").asDouble(0)));
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private void assignLinesToStops() {
        Map<String, Stop> stopIndex = new LinkedHashMap<>();
        for (Stop s : stops) stopIndex.put(s.mbtaStopId, s);

        for (Line line : lines.values()) {
            for (LineStop ls : line.stops) {
                Stop s = stopIndex.get(ls.mbtaStopId);
                if (s != null) s.lineIds.add(line.mbtaRouteId);
            }
        }
    }

    // ---- mode filter parsing -------------------------------------------

    private Set<String> parseModeFilter(String filterType) {
        if (filterType == null || filterType.isBlank()) {
            return Set.of("light_rail", "heavy_rail");
        }
        Set<String> modes = new HashSet<>();
        for (String t : filterType.split(",")) {
            modes.add(modeForType(Integer.parseInt(t.trim())));
        }
        return modes;
    }

    // ---- CSV export ----------------------------------------------------

    private void exportCsv(ScoreSnapshot snap) {
        try {
            Path logsDir = Paths.get("logs");
            Files.createDirectories(logsDir);
            String ts = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path csvPath = logsDir.resolve("zone_scores_" + ts + ".csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath.toFile()))) {
                pw.println("zone_id,center_lat,center_lng,demand,service,score");
                for (ZoneScore r : snap.zones) {
                    pw.printf("%s,%s,%s,%.4f,%.4f,%.4f%n",
                            r.zoneId, r.centerLat, r.centerLng,
                            r.demand, r.service, r.score);
                }
            }
            log.info("Exported zone scores CSV: {}", csvPath.toAbsolutePath());
        } catch (IOException e) {
            log.warn("CSV export failed: {}", e.getMessage());
        }
    }

    // ---- helpers -------------------------------------------------------

    private static String modeForType(int type) {
        return switch (type) {
            case 0 -> "light_rail";
            case 1 -> "heavy_rail";
            case 2 -> "commuter_rail";
            case 3 -> "bus";
            case 4 -> "ferry";
            default -> "unknown";
        };
    }

    private static double haversineM(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double median(double[] sorted) {
        if (sorted.length == 0) return 0;
        int mid = sorted.length / 2;
        return sorted.length % 2 == 0
                ? (sorted[mid - 1] + sorted[mid]) / 2.0 : sorted[mid];
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
