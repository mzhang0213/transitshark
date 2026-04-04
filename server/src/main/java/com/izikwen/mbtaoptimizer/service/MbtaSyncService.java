package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.client.MbtaApiClient;
import org.springframework.stereotype.Service;

@Service
public class MbtaSyncService {
    private final MbtaApiClient mbtaApiClient;

    public MbtaSyncService(MbtaApiClient mbtaApiClient) {
        this.mbtaApiClient = mbtaApiClient;
    }

    public String fetchStopsRaw() { return mbtaApiClient.getStopsRaw(); }
    public String fetchRoutesRaw() { return mbtaApiClient.getRoutesRaw(); }
    public String fetchPredictionsRaw() { return mbtaApiClient.getPredictionsRaw(); }
}
