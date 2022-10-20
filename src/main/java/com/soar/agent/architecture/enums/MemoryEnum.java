package com.soar.agent.architecture.enums;

public enum MemoryEnum {
  //General
  BASIC_NAME("name"),
  IDENTITY("self"),
  MINIMUM_BOUNDING_BOX("mbb"),
  POSITION("pose"),
  POSITION_X("x"),
  POSITION_Y("y"),
  YAW("yaw"),
  DISTANCE("distance"),
  DIRECTION_COMMAND("direction-command"),
  //Landmark Memory Enums  
  LANDMARK_MAIN("landmarks"),
  LANDMARK_SUB("landmark");

  private String name;

  private MemoryEnum(String name){
    this.name = name;
  }

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}
}