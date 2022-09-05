package com.soar.agent.architecture.enums;
/*
 * Enum class to identify/retrieve the agent direction and angle
 * Note: the angles are anticlockwise based on the current simulation environment
 * 0 degrees if going straight forward -  east
 * 90 degrees if going to the left / north
 * 180 degrees if turning back / west
 * 270 degrees if going to the right / south
 */
public enum DirectionEnum {
    NORTH("north", 90),
    EAST("east", 0),
    WEST("west", 180),
    SOUTH("south", 270);

    private String name;
    private int angle;

    private DirectionEnum(String name, int angle){
        this.name = name;
        this.angle = angle;

    }

    public static DirectionEnum findByName(String name) {
        DirectionEnum result = null;
        for (DirectionEnum direction : values()) {
            if (direction.name().equalsIgnoreCase(name)) {
                result = direction;
                break;
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public int getAngle() {
        return angle;
    }

    // public String getDirection() {

    //     switch (this) {
    //         case NORTH:
    //             return "north";

    //         case EAST:
    //             return "east";

    //         case WEST:
    //             return "west";

    //         case SOUTH:
    //             return "south";

    //         default:
    //             return null;
    //     }
    // }

    // public int getDirectionAngle(){

    // }

}

