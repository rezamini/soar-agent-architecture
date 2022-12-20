package com.soar.agent.architecture.enums;
/*
 * Enum class to identify/retrieve the agent direction and angle
 * Note: the angles are anticlockwise based on the current simulation environment
 * 0 degrees if going straight forward -  east
 * 90 degrees if going to the left / north
 * 180 degrees if turning back / west
 * 270 degrees if going to the right / south
 * 
 * Note: The order of the enum is important for the opposite direction method.
 */
public enum DirectionEnum {
    NORTH("north", 90),
    NORTHEAST("northeast", 45),
    NORTHWEST("northwest", 135),
    EAST("east", 0),
    SOUTH("south", 270),
    SOUTHWEST("southwest", 225),
    SOUTHEAST("southeast", 315),
    WEST("west", 180);

    private String name;
    private int angle;

    private DirectionEnum(String name, int angle){
        this.name = name;
        this.angle = angle;

    }

    public static DirectionEnum findByName(String name) {
        DirectionEnum result = null;
        for (DirectionEnum direction : values()) {
            if (direction.getName().equalsIgnoreCase(name)) {
                result = direction;
                break;
            }
        }
        return result;
    }

    public static DirectionEnum findByAngleDegree(int degree) {
        DirectionEnum result = null;
        for (DirectionEnum direction : values()) {
            if (direction.getAngle() == degree) {
                result = direction;
                break;
            }
        }
        return result;
    }

    //get the opposit direction of the current direction
    public DirectionEnum getOppositeDirection() {
        DirectionEnum[] array = values();
    
        return array[(ordinal() + 4) % array.length];
    }
    //Method overloading: get the opposit direction of the current direction with enum argument
    public static DirectionEnum getOppositeDirection(DirectionEnum currentDirectionEnum) {
        DirectionEnum result = null;
        if(currentDirectionEnum != null){
            DirectionEnum[] array = values();
            result = array[(currentDirectionEnum.ordinal() + 4) % array.length];
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

