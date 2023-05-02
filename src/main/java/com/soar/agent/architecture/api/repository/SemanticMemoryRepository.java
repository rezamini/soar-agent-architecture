package com.soar.agent.architecture.api.repository;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;

@Repository
public interface SemanticMemoryRepository {
    
    public Map<String, Set<String>> getSemanticMemoryAttributes();
    public void saveSemanticMemoryAttributes(SemanticMemoryEntity semanticMemoryEntity);
}
