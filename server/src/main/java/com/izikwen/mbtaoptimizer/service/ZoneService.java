package com.izikwen.mbtaoptimizer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.izikwen.mbtaoptimizer.client.MbtaApiClient;
import com.izikwen.mbtaoptimizer.config.GeoBounds;
import com.izikwen.mbtaoptimizer.dto.response.ZoneInfoResponse;
import com.izikwen.mbtaoptimizer.dto.response.ZoneScoreResponse;
import com.izikwen.mbtaoptimizer.entity.ZoneData;
import com.izikwen.mbtaoptimizer.repository.DataSetRepository;
import com.izikwen.mbtaoptimizer.repository.ZoneDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes zones as a pure function of stop locations from the transit API.
 * Deterministic: same stops → same zones every time.
 *
 * Zone score = demand_score - service_score (the "gap").
 *   High score = high demand, poor service → needs optimization.
 *
 * Demand comes from the active dataset (if one is selected) or falls back
 * to a deterministic synthesis based on city-center distance.
 *
 * Service is computed from nearby stops weighted by inverse distance * frequency.
 */
@Service
public class ZoneService {

    private static final Logger log = LoggerFactory.getLogger(ZoneService.class);

    private static final double LAT_CELL = 0.003;
    private static final double LNG_CELL = 0.003;

    // --- demand synthesis fallback params ---
    private static final double POP_SIGMA_KM = 6.0;
    private static final double JOB_SIGMA_KM = 3.0;
    private static final double W_POP = 0.40;
    private static final double W_JOB = 0.40;
    private static final double W_CAR = 0.20;

    // --- service score params ---
    private static final double SERVICE_RADIUS_KM = 3.0;
    private static final double FREQ_HEAVY_RAIL   = 12;
    private static final double FREQ_LIGHT_RAIL   = 10;
    private static final double FREQ_BUS          = 4;
    private static final double FREQ_COMMUTER     = 2;
    private static final double[] FREQ_LIST = {FREQ_HEAVY_RAIL,FREQ_LIGHT_RAIL,FREQ_BUS,FREQ_COMMUTER};

    private final MbtaApiClient mbtaClient;
    private final DataSetRepository dataSetRepo;
    private final ZoneDataRepository zoneDataRepo;

    public ZoneService(MbtaApiClient mbtaClient,
                       DataSetRepository dataSetRepo,
                       ZoneDataRepository zoneDataRepo) {
        this.mbtaClient = mbtaClient;
        this.dataSetRepo = dataSetRepo;
        this.zoneDataRepo = zoneDataRepo;
    }

    // ---- public API ---------------------------------------------------

    public List<ZoneInfoResponse> getZones() {
        return computeZones(fetchTypedStops());
    }

    public List<ZoneScoreResponse> getZoneScores() {
        List<TypedStop> stops = fetchTypedStops();
        List<ZoneInfoResponse> zones = computeZones(stops);
        double[] center = centroid(stops);

        // Try to load active dataset factors
        Map<String, ZoneData> activeData = loadActiveDataset(
                zones.stream().map(ZoneInfoResponse::getZoneId).toList());

        List<Double> demandScores = new ArrayList<>();
        List<Double> serviceScores = new ArrayList<>();
        List<ZoneScoreResponse> scores = new ArrayList<>();
        List<String[]> csvRows = new ArrayList<>();
        for (ZoneInfoResponse z : zones) {
            double demand;
            ZoneData zd = activeData.get(z.getZoneId());
            if (zd != null) {
                demand = W_POP * zd.getPopulationDensity()
                    + W_JOB * zd.getJobDensity()
                    - W_CAR * zd.getCarOwnership();
                demand = clamp01(demand + 0.2);
            } else {
                demand = demandScoreSynthesized(z.getCenterLat(), z.getCenterLng(), center);
            }
            demandScores.add(demand);
        }
        for (ZoneInfoResponse z : zones) {
            double service = serviceScore(z.getCenterLat(), z.getCenterLng(), stops);
            serviceScores.add(service);
        }

        List<Double> zoneScores = new ArrayList<>();
        for (int i=0;i<zones.size();i++) {
            double gap = demandScores.get(i) - serviceScores.get(i);
            zoneScores.add(gap);
        }

        normScores(demandScores);
        normScores(serviceScores);
        normScores(zoneScores);

        for (int i=0;i<zones.size();i++) {
            Double currServiceScore = serviceScores.get(i);
            Double currDemandScore = demandScores.get(i);
            double score = currServiceScore - currDemandScore;
            ZoneInfoResponse z = zones.get(i);

            scores.add(new ZoneScoreResponse(z.getZoneId(), z.getName(), score));
            csvRows.add(new String[]{
                z.getZoneId(),
                String.valueOf(z.getCenterLat()),
                String.valueOf(z.getCenterLng()),
                String.format("%.4f", currDemandScore),
                String.format("%.4f", currServiceScore),
                String.format("%.1f", score)
            });
        }

        //exportCsv(csvRows);
        return scores;
    }

    private void normScores(List<Double> rawScores){
        double max = rawScores.getFirst();
        for (double d : rawScores){
            max = Math.max(d,max);
        }
        for (int i=0;i<rawScores.size();i++){
            rawScores.set(i,rawScores.get(i)/max);
        }
    }

    // ---- CSV export ----------------------------------------------------

    private void exportCsv(List<String[]> rows) {
        try {
            Path logsDir = Paths.get("logs");
            Files.createDirectories(logsDir);
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path csvPath = logsDir.resolve("zone_scores_" + timestamp + ".csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath.toFile()))) {
                pw.println("zone_id,center_lat,center_lng,demand,service,score");
                for (String[] row : rows) {
                    pw.println(String.join(",", row));
                }
            }
            log.info("Exported zone scores CSV: {}", csvPath.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to export zone scores CSV: {}", e.getMessage());
        }
    }

    // ---- active dataset lookup ----------------------------------------

    private Map<String, ZoneData> loadActiveDataset(List<String> zoneIds) {
        return dataSetRepo.findByActiveTrue()
                .map(ds -> zoneDataRepo.findByDataSet_IdAndZoneIdIn(ds.getId(), zoneIds)
                        .stream()
                        .collect(Collectors.toMap(ZoneData::getZoneId, zd -> zd)))
                .orElse(Map.of());
    }

    // ---- demand synthesis fallback -------------------------------------

    private double demandScoreSynthesized(double lat, double lng, double[] cityCenter) {
        double distKm = haversineKm(lat, lng, cityCenter[0], cityCenter[1]);
        double popDensity  = gaussian(distKm, POP_SIGMA_KM) * spatialNoise(lat, lng, 1);
        double jobDensity  = gaussian(distKm, JOB_SIGMA_KM) * spatialNoise(lat, lng, 2);
        double carOwnership = 1.0 - 0.7 * gaussian(distKm, POP_SIGMA_KM);
        double raw = W_POP * popDensity + W_JOB * jobDensity - W_CAR * carOwnership;
        return clamp01(raw + 0.2);
    }

    // ---- service score ------------------------------------------------

    private double serviceScore(double lat, double lng, List<TypedStop> stops) {
        double raw = 0;
        for (TypedStop s : stops) {
            double distKm = haversineKm(lat, lng, s.lat, s.lng);
            if (distKm > SERVICE_RADIUS_KM || distKm < 0.01) continue;
            raw += normFreq(s.routeType) * distKm;
        }
        return raw;
    }

    private double normFreq(int routeType) {
        double max = FREQ_LIST[0];
        for (double freq : FREQ_LIST){
            max = Math.max(freq,max);
        }
        return freqForType(routeType) / max;
    }

    private double freqForType(int routeType) {
        return switch (routeType) {
            case 1 -> FREQ_HEAVY_RAIL;
            case 0 -> FREQ_LIGHT_RAIL;
            case 3 -> FREQ_BUS;
            case 2 -> FREQ_COMMUTER;
            default -> FREQ_BUS;
        };
    }

    // ---- exclusion zones (water, parks, busy commercial) ----------------

    private static final double[][] EXCLUSIONS = {
        // Boston Inner Harbor
        {42.350, -71.045, 42.372, -70.995},
        // Reserved Channel / South Boston waterfront
        {42.330, -71.040, 42.348, -71.005},
        // Fort Point Channel
        {42.346, -71.055, 42.355, -71.042},
        // Pleasure Bay / Castle Island water
        {42.329, -71.020, 42.340, -71.005},
        // Logan Airport runways / tarmac
        {42.358, -71.020, 42.378, -70.995},
    };

    /** Returns true if the center point falls inside any exclusion rectangle. */
    private static boolean isExcluded(double lat, double lng) {
        for (double[] ex : EXCLUSIONS) {
            if (lat >= ex[0] && lat <= ex[2] && lng >= ex[1] && lng <= ex[3]) {
                return true;
            }
        }
        return false;
    }

    // ---- zone grid computation (brick/offset, full coverage) -----------

    /**
     * Tiles the entire GeoBounds area with a brick-pattern grid.
     * Zones are created everywhere EXCEPT exclusion areas (water, parks, commercial).
     * Stops are NOT required — residential areas far from transit still get zones.
     */
    private List<ZoneInfoResponse> computeZones(List<TypedStop> stops) {
        double originLat = Math.floor(GeoBounds.MIN_LAT / LAT_CELL) * LAT_CELL;
        double originLng = Math.floor(GeoBounds.MIN_LNG / LNG_CELL) * LNG_CELL;
        int rows = (int) Math.ceil((GeoBounds.MAX_LAT - originLat) / LAT_CELL) + 1;
        int cols = (int) Math.ceil((GeoBounds.MAX_LNG - originLng) / LNG_CELL) + 2;

        List<ZoneInfoResponse> zones = new ArrayList<>();
        int zoneNum = 1;

        for (int r = 0; r < rows; r++) {
            // Even sub-row
            for (int c = 0; c < cols; c++) {
                double cMinLat = round6(originLat + r * LAT_CELL);
                double cMaxLat = round6(cMinLat + LAT_CELL);
                double cMinLng = round6(originLng + c * LNG_CELL);
                double cMaxLng = round6(cMinLng + LNG_CELL);
                double cenLat = round6((cMinLat + cMaxLat) / 2);
                double cenLng = round6((cMinLng + cMaxLng) / 2);

                if (!GeoBounds.inBounds(cenLat, cenLng)) continue;
                if (isExcluded(cenLat, cenLng)) continue;

                String id = String.format("Z-%04d", zoneNum++);
                zones.add(new ZoneInfoResponse(id, id, cenLat, cenLng,
                        cMinLat, cMinLng, cMaxLat, cMaxLng));
            }
            // Odd sub-row (offset by half cell in both directions)
            for (int c = 0; c < cols; c++) {
                double cMinLat = round6(originLat + r * LAT_CELL + LAT_CELL / 2);
                double cMaxLat = round6(cMinLat + LAT_CELL);
                double cMinLng = round6(originLng + c * LNG_CELL + LNG_CELL / 2);
                double cMaxLng = round6(cMinLng + LNG_CELL);
                double cenLat = round6((cMinLat + cMaxLat) / 2);
                double cenLng = round6((cMinLng + cMaxLng) / 2);

                if (!GeoBounds.inBounds(cenLat, cenLng)) continue;
                if (isExcluded(cenLat, cenLng)) continue;

                String id = String.format("Z-%04d", zoneNum++);
                zones.add(new ZoneInfoResponse(id, id, cenLat, cenLng,
                        cMinLat, cMinLng, cMaxLat, cMaxLng));
            }
        }
        return zones;
    }

    // ---- MBTA API fetch -----------------------------------------------

    List<TypedStop> fetchTypedStops() {
        List<TypedStop> stops = new ArrayList<>();
        for (String typeFilter : List.of("0,1", "3")) {
            int routeType = typeFilter.equals("3") ? 3 : 1;
            try {
                JsonNode json = mbtaClient.getAllStops(typeFilter);
                for (JsonNode stop : json.path("data")) {
                    double lat = stop.path("attributes").path("latitude").asDouble(0);
                    double lng = stop.path("attributes").path("longitude").asDouble(0);
                    int vt = stop.path("attributes").path("vehicle_type").asInt(routeType);
                    if (lat != 0 && lng != 0 && GeoBounds.inBounds(lat, lng)) {
                        stops.add(new TypedStop(lat, lng, vt));
                    }
                }
            } catch (Exception e) {
                // skip
            }
        }
        return stops;
    }

    // ---- helpers -------------------------------------------------------

    private double gaussian(double dist, double sigma) {
        return Math.exp(-(dist * dist) / (2 * sigma * sigma));
    }

    private double spatialNoise(double lat, double lng, int seed) {
        long bits = Double.doubleToLongBits(round6(lat)) * 31
                  + Double.doubleToLongBits(round6(lng)) * 17
                  + seed * 1_000_003L;
        bits ^= (bits >>> 21);
        bits ^= (bits << 35);
        bits ^= (bits >>> 4);
        double noise = ((bits & 0xFFFFL) / (double) 0xFFFFL);
        return 0.7 + noise * 0.6;
    }

    private double[] centroid(List<TypedStop> stops) {
        double sumLat = 0, sumLng = 0;
        for (TypedStop s : stops) { sumLat += s.lat; sumLng += s.lng; }
        return new double[]{ sumLat / stops.size(), sumLng / stops.size() };
    }

    private double clamp01(double v) { return Math.max(0, Math.min(1, v)); }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double round6(double v) {
        return Math.round(v * 1_000_000.0) / 1_000_000.0;
    }

    record TypedStop(double lat, double lng, int routeType) {}
}
