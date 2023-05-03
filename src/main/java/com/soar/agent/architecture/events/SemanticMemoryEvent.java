package com.soar.agent.architecture.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jsoar.runtime.ThreadedAgent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;
import com.soar.agent.architecture.robot.RobotAgent;

@Repository
public abstract class SemanticMemoryEvent {
    public final RobotAgent robotAgent;

    public SemanticMemoryEvent(RobotAgent agent) {
        this.robotAgent = agent;
    }

    public abstract void initSemanticDB();

    /**
     * This method should only be called if we are enabling/adding data to smem
     * before
     * loading soar file. This method enables the possibility to enter knowledge
     * even if the smem db file is not loaded from soar production. The dbName
     * should not be null. if we are calling to add smem knowledge BEFORE calling
     * soar production file manuallyEnableDB() is required. if we are calling to
     * add
     * smem knowledge AFTER calling soar production file manuallyEnableDB()
     * should
     * NOT be called, because the db name presented in the soar file is used
     * otherwise an error is thrown.
     * This is all considering the soar file has smem enabled and has smem path.y
     * If we are loading a soar file without smem info. this method has to be called regardless.
     */
    public abstract void manuallyEnableDB();

    public abstract void addSemanticKnowledge(SemanticMemoryEntity semanticMemoryEntity);

    public abstract Set<String> getAttributeValues(String attributeName);

    public abstract Map<String, Set<String>> retrieveAllAttributes();
}
