package com.soar.agent.architecture.events;

import org.jsoar.runtime.ThreadedAgent;

public abstract class SemanticMemoryEvent {
    public final ThreadedAgent agent;

    public SemanticMemoryEvent(ThreadedAgent agent){
        this.agent = agent;
    }

    public abstract void initSemanticDB();
    public abstract void addSemanticKnowledge();
}
