package com.soar.agent.architecture.beans;

import java.util.Iterator;

import org.jsoar.kernel.memory.Preference;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.Symbol;

public class Move {
    //from the context of the listener
    public Symbol attribute;
    public int timeTag;
    public Iterator<Wme> children;
    public Identifier identifier;
    public Iterator<Preference> preference;

    //from the rule file; .soar
    public String direction;

    public Iterator<Wme> getChildren() {
        return children;
    }

    public void setChildren(Iterator<Wme> children) {
        this.children = children;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public Iterator<Preference> getPreference() {
        return preference;
    }

    public void setPreference(Iterator<Preference> preference) {
        this.preference = preference;
    }
    
    public Symbol getAttribute() {
        return attribute;
    }

    public void setAttribute(Symbol attribute) {
        this.attribute = attribute;
    }

    public int getTimeTag() {
        return timeTag;
    }

    public void setTimeTag(int timeTag) {
        this.timeTag = timeTag;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}

