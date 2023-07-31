package com.soar.agent.architecture.enums;

// A utility enum class for global and general names

public enum UtilitiesEnum {
    ACTIVESTATUS("active"),
    INACTIVESTATUS("inactive"),
    REACHEDSTATUS("reached"),
    DOTSEPERATOR("."),
    DASHSEPERATOR("-"),
    MEMORYSTATUS("status"),
    ON_STATUS("on"),
    OFF_STATUS("off");


    private UtilitiesEnum(String name){
        this.name = name;
    }
    
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
