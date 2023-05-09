package com.soar.agent.architecture.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;
import com.soar.agent.architecture.api.repository.SemanticMemoryRepository;
import com.soar.agent.architecture.events.SemanticMemoryResponder;
import com.soar.agent.architecture.robot.RobotAgent;

@Service
public class SemanticMemoryService implements SemanticMemoryRepository {

    @Autowired
    private RobotAgent robotAgent;

    @Autowired
    private SemanticMemoryResponder semanticMemoryResponder;

    /*
     * This method runs once at the start of the program with the @EventListener and
     * on every API get call
     */

    @Override
    // @PostConstruct
    @EventListener(ApplicationReadyEvent.class)
    public Map<String, Set<String>> getSemanticMemoryAttributes() {
        semanticMemoryResponder.manuallyEnableDB();
        Map<String, Set<String>> result = semanticMemoryResponder.retrieveAllAttributes();

        // update the latest values for the live agent use
        // robotAgent.setSmemAttributes(result);

        return result;
    }

    @Override
    public void saveSemanticMemoryAttributes(SemanticMemoryEntity semanticMemoryEntity) {
        semanticMemoryResponder.manuallyEnableDB();
        semanticMemoryResponder.addSemanticKnowledge(semanticMemoryEntity);
    }

}
