package com.soar.agent.architecture.enums;

public enum CellTypeEnum {
    NONE("none"),
    NORMAL("normal"),
    BLOCK("block");

    private String name;

    private CellTypeEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
