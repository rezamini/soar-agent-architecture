package com.soar.agent.architecture.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Map<String, List<String>> getSemanticMemoryAttributes() {
        System.out.println("XXXXXXXXXXXXXXXXXXXX");
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        // robotAgent.getSmemResponder().manuallyEnableDB();
        // semanticMemoryResponder.manuallyEnableDB();
        
        System.out.println(semanticMemoryResponder.retrieveAllAttributes());
        

        return result;
    }

    @Override
    public void saveSemanticMemoryAttributes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveSemanticMemoryAttributes'");
    }
    
}
