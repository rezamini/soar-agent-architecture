package com.soar.agent.architecture.api.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface SemanticMemoryRepository {
    
    public List<String> getSemanticMemoryAttributes();
    public void saveSemanticMemoryAttributes();
}
