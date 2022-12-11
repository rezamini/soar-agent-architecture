package com.soar.agent.architecture.enums;

public enum GraphEnum {
    MEMORY_CARET("^"),
    FRAME_TITLE("Soar Working Memory Dynamic Graph (Knowledge Graph for Cognition Inspection) "),
    MAIN_MENUE_TITLE("Menu"),
    SUB_MENUE_ENABLE_NODE_MENU("Enable Nodes Menu"),
    SUB_MENUE_ENABLE_ZOOM_CONTROL("Enable Zoom Control"),
    TOOLBAR_TITLE("Draggable Toolbar"),
    NODE_MENU_MAIN_TITLE("Working Memory Nodes: "),
    VERTICAL_BAR_SEPERATOR("|"),
    ;

    private String name;
    
    private GraphEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
