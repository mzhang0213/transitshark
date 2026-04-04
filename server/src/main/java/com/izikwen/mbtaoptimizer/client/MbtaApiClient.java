package com.izikwen.mbtaoptimizer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MbtaApiClient {

    private final RestClient restClient;

    public MbtaApiClient(RestClient.Builder builder, @Value("${mbta.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String getStopsRaw() {
        return restClient.get().uri("/stops").retrieve().body(String.class);
    }

    public String getRoutesRaw() {
        return restClient.get().uri("/routes").retrieve().body(String.class);
    }

    public String getPredictionsRaw() {
        return restClient.get().uri("/predictions").retrieve().body(String.class);
    }
}
