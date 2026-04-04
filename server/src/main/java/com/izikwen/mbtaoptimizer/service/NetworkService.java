package com.izikwen.mbtaoptimizer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.izikwen.mbtaoptimizer.client.MbtaApiClient;
import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse;
import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse.EdgeDto;
import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse.LineDto;
import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse.StopDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetworkService {

    private static final Logger log = LoggerFactory.getLogger(NetworkService.class);
    private final MbtaApiClient mbtaClient;

    public NetworkService(MbtaApiClient mbtaClient) {
        this.mbtaClient = mbtaClient;
    }

    /**
     * Builds the full transit network from the live MBTA API.
     * Fetches routes, then for each route fetches route_patterns
     * with included stops to get correct stop ordering and edges.
     *
     * @param filterType optional route type filter (0=LightRail,1=HeavyRail,2=CommuterRail,3=Bus)
     */
    public NetworkResponse getNetwork(String filterType) {
        JsonNode routesJson = mbtaClient.getRoutes(filterType);
        JsonNode routesData = routesJson.path("data");

        List<LineDto> lines = new ArrayList<>();

        for (JsonNode routeNode : routesData) {
            String routeId = routeNode.path("id").asText();
            JsonNode attrs = routeNode.path("attributes");

            String longName = attrs.path("long_name").asText(null);
            String shortName = attrs.path("short_name").asText(null);
            String color = attrs.path("color").asText(null);
            int type = attrs.path("type").asInt(-1);
            String mode = typeToMode(type);

            // Fetch route_patterns with representative_trip.stops for ordered stops
            List<StopDto> orderedStops;
            try {
                orderedStops = fetchOrderedStops(routeId);
            } catch (Exception e) {
                log.warn("Failed to fetch route_patterns for {}, falling back to /stops", routeId, e);
                orderedStops = fetchStopsFallback(routeId);
            }

            if (orderedStops.isEmpty()) continue;

            // Build edges between consecutive stops
            List<EdgeDto> edges = new ArrayList<>();
            for (int i = 0; i < orderedStops.size() - 1; i++) {
                StopDto from = orderedStops.get(i);
                StopDto to = orderedStops.get(i + 1);
                double dist = haversine(from.getLat(), from.getLng(), to.getLat(), to.getLng());
                edges.add(new EdgeDto(from.getStopId(), to.getStopId(), Math.round(dist * 10.0) / 10.0));
            }

            LineDto line = new LineDto();
            line.setLineId(routeId.hashCode() & 0xFFFFFFFFL); // stable numeric id from string
            line.setLineName(longName != null && !longName.isEmpty() ? longName : shortName);
            line.setColor(color);
            line.setMode(mode);
            line.setMbtaRouteId(routeId);
            line.setStops(orderedStops);
            line.setEdges(edges);
            lines.add(line);
        }

        return new NetworkResponse(lines);
    }

    /**
     * Fetches stops in trip order using route_patterns + representative_trip.stops.
     * Uses the first (canonical) direction's pattern.
     */
    private List<StopDto> fetchOrderedStops(String routeId) {
        JsonNode rpJson = mbtaClient.getRoutePatterns(routeId);
        JsonNode included = rpJson.path("included");

        // Build a map of stop id -> stop data from "included"
        Map<String, JsonNode> stopNodesById = new LinkedHashMap<>();
        Map<String, JsonNode> tripNodesById = new LinkedHashMap<>();
        for (JsonNode inc : included) {
            String type = inc.path("type").asText();
            String id = inc.path("id").asText();
            if ("stop".equals(type)) {
                stopNodesById.put(id, inc);
            } else if ("trip".equals(type)) {
                tripNodesById.put(id, inc);
            }
        }

        // Find the first route_pattern's representative_trip
        JsonNode patternsData = rpJson.path("data");
        if (patternsData.isEmpty()) return List.of();

        // Pick direction 0 pattern (outbound)
        JsonNode chosenPattern = null;
        for (JsonNode pat : patternsData) {
            int dir = pat.path("attributes").path("direction_id").asInt(-1);
            if (dir == 0) { chosenPattern = pat; break; }
        }
        if (chosenPattern == null) chosenPattern = patternsData.get(0);

        // Get the representative trip id
        String tripId = chosenPattern.path("relationships")
                .path("representative_trip").path("data").path("id").asText(null);
        if (tripId == null) return List.of();

        // Get the trip's stop relationships (ordered)
        JsonNode tripNode = tripNodesById.get(tripId);
        if (tripNode == null) return List.of();

        JsonNode tripStops = tripNode.path("relationships").path("stops").path("data");
        if (tripStops.isMissingNode() || !tripStops.isArray()) return List.of();

        List<StopDto> result = new ArrayList<>();
        long idCounter = 1;
        for (JsonNode stopRef : tripStops) {
            String stopId = stopRef.path("id").asText();
            JsonNode stopNode = stopNodesById.get(stopId);
            if (stopNode == null) continue;

            JsonNode sa = stopNode.path("attributes");
            result.add(new StopDto(
                    idCounter++,
                    stopId,
                    sa.path("name").asText("Unknown"),
                    sa.path("latitude").asDouble(0),
                    sa.path("longitude").asDouble(0)
            ));
        }
        return result;
    }

    /**
     * Fallback: fetch stops from /stops?filter[route]=X (unordered).
     */
    private List<StopDto> fetchStopsFallback(String routeId) {
        JsonNode stopsJson = mbtaClient.getStopsForRoute(routeId);
        JsonNode stopsData = stopsJson.path("data");

        List<StopDto> stops = new ArrayList<>();
        long idCounter = 1;
        for (JsonNode stopNode : stopsData) {
            JsonNode sa = stopNode.path("attributes");
            stops.add(new StopDto(
                    idCounter++,
                    stopNode.path("id").asText(),
                    sa.path("name").asText("Unknown"),
                    sa.path("latitude").asDouble(0),
                    sa.path("longitude").asDouble(0)
            ));
        }
        return stops;
    }

    private String typeToMode(int type) {
        return switch (type) {
            case 0 -> "light_rail";
            case 1 -> "heavy_rail";
            case 2 -> "commuter_rail";
            case 3 -> "bus";
            case 4 -> "ferry";
            default -> "unknown";
        };
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
