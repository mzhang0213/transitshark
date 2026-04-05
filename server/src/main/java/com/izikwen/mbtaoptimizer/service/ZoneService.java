package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.config.GeoBounds;
import com.izikwen.mbtaoptimizer.dto.response.ZoneInfoResponse;
import com.izikwen.mbtaoptimizer.dto.response.ZoneScoreResponse;
import com.izikwen.mbtaoptimizer.entity.ZoneData;
import com.izikwen.mbtaoptimizer.repository.DataSetRepository;
import com.izikwen.mbtaoptimizer.repository.ZoneDataRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ZoneService {

    private static final double LAT_CELL = 0.003;
    private static final double LNG_CELL = 0.003;

    private static final double POP_SIGMA_KM = 6.0;
    private static final double JOB_SIGMA_KM = 3.0;
    private static final double W_POP = 0.40;
    private static final double W_JOB = 0.40;
    private static final double W_CAR = 0.20;

    private static final double SERVICE_RADIUS_KM = 3.0;
    private static final double FREQ_HEAVY_RAIL = 12;
    private static final double FREQ_LIGHT_RAIL = 10;
    private static final double FREQ_BUS = 4;
    private static final double FREQ_COMMUTER = 2;
    private static final double[] FREQ_LIST = {FREQ_HEAVY_RAIL, FREQ_LIGHT_RAIL, FREQ_BUS, FREQ_COMMUTER};

    private final TransitState state;
    private final DataSetRepository dataSetRepo;
    private final ZoneDataRepository zoneDataRepo;

    public ZoneService(TransitState state,
                       DataSetRepository dataSetRepo,
                       ZoneDataRepository zoneDataRepo) {
        this.state = state;
        this.dataSetRepo = dataSetRepo;
        this.zoneDataRepo = zoneDataRepo;
    }

    // ---- public API ---------------------------------------------------

    public List<ZoneInfoResponse> getZones() {
        List<ZoneInfoResponse> zones = state.getZones();
        if (zones.isEmpty()) {
            zones = computeZones();
            state.setZones(zones);
        }
        return zones;
    }

    public List<ZoneScoreResponse> getZoneScores() {
        List<TransitState.Stop> stops = state.getStops();
        List<ZoneInfoResponse> zones = getZones();
        double[] center = centroid(stops);

        Map<String, ZoneData> activeData = loadActiveDataset(
                zones.stream().map(ZoneInfoResponse::getZoneId).toList());

        List<Double> demandScores = new ArrayList<>();
        List<Double> serviceScores = new ArrayList<>();

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
        for (int i = 0; i < zones.size(); i++) {
            zoneScores.add(demandScores.get(i) - serviceScores.get(i));
        }

        normScores(demandScores);
        normScores(serviceScores);
        normScores(zoneScores);

        List<ZoneScoreResponse> results = new ArrayList<>();
        List<TransitState.ZoneScore> tracked = new ArrayList<>();

        for (int i = 0; i < zones.size(); i++) {
            double svc = serviceScores.get(i);
            double dem = demandScores.get(i);
            double score = svc - dem;
            ZoneInfoResponse z = zones.get(i);

            results.add(new ZoneScoreResponse(z.getZoneId(), z.getName(), score));
            tracked.add(new TransitState.ZoneScore(
                    z.getZoneId(), z.getCenterLat(), z.getCenterLng(),
                    dem, svc, score));
        }

        state.recordScores("api", tracked);
        return results;
    }

    // ---- normalization -------------------------------------------------

    private void normScores(List<Double> rawScores) {
        double max = rawScores.getFirst();
        for (double d : rawScores) max = Math.max(d, max);
        if (max == 0) return;
        for (int i = 0; i < rawScores.size(); i++) {
            rawScores.set(i, rawScores.get(i) / max);
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
        double popDensity = gaussian(distKm, POP_SIGMA_KM) * spatialNoise(lat, lng, 1);
        double jobDensity = gaussian(distKm, JOB_SIGMA_KM) * spatialNoise(lat, lng, 2);
        double carOwnership = 1.0 - 0.7 * gaussian(distKm, POP_SIGMA_KM);
        double raw = W_POP * popDensity + W_JOB * jobDensity - W_CAR * carOwnership;
        return clamp01(raw + 0.2);
    }

    // ---- service score ------------------------------------------------

    private double serviceScore(double lat, double lng, List<TransitState.Stop> stops) {
        double raw = 0;
        for (TransitState.Stop s : stops) {
            double distKm = haversineKm(lat, lng, s.lat(), s.lng());
            if (distKm > SERVICE_RADIUS_KM || distKm < 0.01) continue;
            raw += normFreq(s.routeType()) * distKm;
        }
        return raw;
    }

    private double normFreq(int routeType) {
        double max = FREQ_LIST[0];
        for (double freq : FREQ_LIST) max = Math.max(freq, max);
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

    // ---- exclusion zones (water) ----------------------------------------

    private static final double[][] EXCLUSIONS = {
        {42.350, -71.045, 42.372, -70.995},
        {42.330, -71.040, 42.348, -71.005},
        {42.346, -71.055, 42.355, -71.042},
        {42.329, -71.020, 42.340, -71.005},
        {42.358, -71.020, 42.378, -70.995},
    };

    private static boolean isExcluded(double lat, double lng) {
        for (double[] ex : EXCLUSIONS) {
            if (lat >= ex[0] && lat <= ex[2] && lng >= ex[1] && lng <= ex[3]) return true;
        }
        return false;
    }

    // ---- zone grid computation ----------------------------------------

    private List<ZoneInfoResponse> computeZones() {
        double originLat = Math.floor(GeoBounds.MIN_LAT / LAT_CELL) * LAT_CELL;
        double originLng = Math.floor(GeoBounds.MIN_LNG / LNG_CELL) * LNG_CELL;
        int rows = (int) Math.ceil((GeoBounds.MAX_LAT - originLat) / LAT_CELL) + 1;
        int cols = (int) Math.ceil((GeoBounds.MAX_LNG - originLng) / LNG_CELL) + 2;

        List<ZoneInfoResponse> zones = new ArrayList<>();
        int zoneNum = 1;

        for (int r = 0; r < rows; r++) {
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

    private double[] centroid(List<TransitState.Stop> stops) {
        double sumLat = 0, sumLng = 0;
        for (TransitState.Stop s : stops) { sumLat += s.lat(); sumLng += s.lng(); }
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
}
