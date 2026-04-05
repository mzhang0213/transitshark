package com.izikwen.mbtaoptimizer.service;

import com.izikwen.mbtaoptimizer.dto.response.NetworkResponse;
import org.springframework.stereotype.Service;

@Service
public class NetworkService {

    private final TransitState state;

    public NetworkService(TransitState state) {
        this.state = state;
    }

    public NetworkResponse getNetwork(String filterType) {
        return state.toNetworkResponse(filterType);
    }
}
