package com.soar.agent.architecture.api.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SemanticMemoryEntity {
    private String name;
    private Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    public String getName() {
        return name;
    }

    public void setParentName(String name) {
        this.name = name;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

}
