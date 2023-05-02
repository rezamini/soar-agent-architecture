package com.soar.agent.architecture.api.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface SemanticMemoryRepository {
    
    public Map<String, List<String>> getSemanticMemoryAttributes();
    public void saveSemanticMemoryAttributes();
}
