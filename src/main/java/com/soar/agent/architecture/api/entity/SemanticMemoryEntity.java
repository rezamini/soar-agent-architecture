package com.soar.agent.architecture.api.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SemanticMemoryEntity {
    private Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

}
