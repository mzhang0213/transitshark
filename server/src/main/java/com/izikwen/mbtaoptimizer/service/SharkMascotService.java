package com.izikwen.mbtaoptimizer.service;

import org.springframework.stereotype.Service;

@Service
public class SharkMascotService {

    public String moodForScore(double score) {
        if (score >= 85) return "ECSTATIC_SHARK";
        if (score >= 72) return "HAPPY_SHARK";
        if (score >= 60) return "THINKING_SHARK";
        return "SAD_SHARK";
    }
}
