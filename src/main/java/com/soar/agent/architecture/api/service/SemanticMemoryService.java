package com.soar.agent.architecture.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.soar.agent.architecture.api.repository.SemanticMemoryRepository;

@Service
public class SemanticMemoryService implements SemanticMemoryRepository{

    @Override
    public List<String> getSemanticMemoryAttributes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSemanticMemoryAttributes'");
    }

    @Override
    public void saveSemanticMemoryAttributes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveSemanticMemoryAttributes'");
    }
    
}
