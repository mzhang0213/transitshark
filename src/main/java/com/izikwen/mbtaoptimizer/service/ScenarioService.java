package com.izikwen.mbtaoptimizer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izikwen.mbtaoptimizer.dto.request.AddStopRequest;
import com.izikwen.mbtaoptimizer.dto.request.ChangeFrequencyRequest;
import com.izikwen.mbtaoptimizer.dto.request.CreateScenarioRequest;
import com.izikwen.mbtaoptimizer.dto.request.MoveStopRequest;
import com.izikwen.mbtaoptimizer.dto.response.ScenarioResponse;
import com.izikwen.mbtaoptimizer.entity.OptimizationScenario;
import com.izikwen.mbtaoptimizer.entity.ScenarioChange;
import com.izikwen.mbtaoptimizer.exception.ResourceNotFoundException;
import com.izikwen.mbtaoptimizer.repository.OptimizationScenarioRepository;
import com.izikwen.mbtaoptimizer.repository.ScenarioChangeRepository;
import org.springframework.stereotype.Service;

@Service
public class ScenarioService {
    private final OptimizationScenarioRepository scenarioRepository;
    private final ScenarioChangeRepository changeRepository;
    private final ObjectMapper objectMapper;

    public ScenarioService(OptimizationScenarioRepository scenarioRepository,
                           ScenarioChangeRepository changeRepository,
                           ObjectMapper objectMapper) {
        this.scenarioRepository = scenarioRepository;
        this.changeRepository = changeRepository;
        this.objectMapper = objectMapper;
    }

    public ScenarioResponse createScenario(CreateScenarioRequest request) {
        OptimizationScenario scenario = new OptimizationScenario();
        scenario.setName(request.getName());
        scenario.setAreaName(request.getAreaName());
        scenario.setStatus("DRAFT");
        return toResponse(scenarioRepository.save(scenario));
    }

    public void moveStop(Long scenarioId, MoveStopRequest request) { saveChange(scenarioId, "MOVE_STOP", request); }
    public void addStop(Long scenarioId, AddStopRequest request) { saveChange(scenarioId, "ADD_STOP", request); }
    public void changeFrequency(Long scenarioId, ChangeFrequencyRequest request) { saveChange(scenarioId, "CHANGE_FREQUENCY", request); }

    public OptimizationScenario getScenarioEntity(Long scenarioId) {
        return scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Scenario not found: " + scenarioId));
    }

    private void saveChange(Long scenarioId, String changeType, Object payload) {
        OptimizationScenario scenario = getScenarioEntity(scenarioId);
        try {
            ScenarioChange change = new ScenarioChange();
            change.setScenario(scenario);
            change.setChangeType(changeType);
            change.setPayloadJson(objectMapper.writeValueAsString(payload));
            changeRepository.save(change);
            if (!"DRAFT".equalsIgnoreCase(scenario.getStatus())) {
                scenario.setStatus("UPDATED");
                scenarioRepository.save(scenario);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save scenario change", e);
        }
    }

    private ScenarioResponse toResponse(OptimizationScenario scenario) {
        ScenarioResponse response = new ScenarioResponse();
        response.setId(scenario.getId());
        response.setName(scenario.getName());
        response.setAreaName(scenario.getAreaName());
        response.setStatus(scenario.getStatus());
        response.setCreatedAt(scenario.getCreatedAt());
        return response;
    }
}
