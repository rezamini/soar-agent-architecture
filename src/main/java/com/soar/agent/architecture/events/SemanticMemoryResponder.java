package com.soar.agent.architecture.events;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.parser.original.Lexeme;
import org.jsoar.kernel.smem.DefaultSemanticMemory;
import org.jsoar.kernel.smem.SemanticMemoryDatabase;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.JdbcTools;
import org.jsoar.util.adaptables.AdaptableContainer;

import com.google.common.collect.Tables;

public class SemanticMemoryResponder extends SemanticMemoryEvent{

    private AdaptableContainer context;
    private Connection conn;
    private DefaultSemanticMemory smem;
    
    public SemanticMemoryResponder(ThreadedAgent agent) {
        super(agent);

        initSemanticDB();
    }

    @Override
    public void initSemanticDB() {
        context = AdaptableContainer.from(agent);

        String URL =  getClass().getResource("/databases/smem/smem-db.sqlite").getPath();
        URL = "jdbc:sqlite:" + URL.substring(1);
        try {
            conn = JdbcTools.connect("org.sqlite.JDBC", URL);
            SemanticMemoryDatabase db = new SemanticMemoryDatabase("org.sqlite.JDBC", conn);
            db.getConnection();

            smem = new DefaultSemanticMemory(context, db);
            smem.initialize();
            smem.smem_attach();

        } catch (SoarException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSemanticKnowledge() {
    }
    
}
