package com.soar.agent.architecture.enums;

public enum GraphEnum {
    FRAMETITLE("Soar Working Memory Dynamic Graph (Knowledge Graph)");


    private String name;
    
    private GraphEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
