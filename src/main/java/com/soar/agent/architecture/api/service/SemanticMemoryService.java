package com.soar.agent.architecture.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;
import com.soar.agent.architecture.api.repository.SemanticMemoryRepository;
import com.soar.agent.architecture.events.SemanticMemoryResponder;
import com.soar.agent.architecture.robot.RobotAgent;

@Service
public class SemanticMemoryService implements SemanticMemoryRepository{

    @Autowired
    private RobotAgent robotAgent;

    @Autowired
    private SemanticMemoryResponder semanticMemoryResponder;

    @Override
    public Map<String, Set<String>> getSemanticMemoryAttributes() {
        semanticMemoryResponder.manuallyEnableDB();
        Map<String, Set<String>> result = semanticMemoryResponder.retrieveAllAttributes();

        return result;
    }

    @Override
    public void saveSemanticMemoryAttributes(SemanticMemoryEntity semanticMemoryEntity) {
        semanticMemoryResponder.manuallyEnableDB();
        semanticMemoryResponder.addSemanticKnowledge(semanticMemoryEntity);
    }
    
}
