package com.soar.agent.architecture.events;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.smem.SemanticMemory;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.JdbcTools;
import org.jsoar.util.adaptables.Adaptables;

public class SemanticMemoryResponder extends SemanticMemoryEvent {
    private final String DEFAULT_SMEM_DB_NAME = "smem-default-db";
    private Connection conn;
    private SemanticMemory smem;

    public SemanticMemoryResponder(ThreadedAgent agent, String dbName) {
        super(agent, dbName);
        initSemanticDB();
    }

    @Override
    public void initSemanticDB() {
        smem = Adaptables.adapt(agent, SemanticMemory.class);
        openConnection();

    }

    @Override
    public void addSemanticKnowledge() {
        try {
            agent.getInterpreter().eval("smem --add { " + generateTestData() + "}");
            smem.smem_go(true);

        } catch (SoarException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void manuallyEnableDB() {

        if(dbName != null){
            try {
                agent.getInterpreter().eval("smem --set learning on");
                agent.getInterpreter().eval("smem --set path src/main/resources/databases/smem/" + dbName + ".sqlite");
                agent.getInterpreter().eval("smem --set append-database on");
                agent.getInterpreter().eval("smem --set lazy-commit off");
    
            } catch (SoarException e) {
                e.printStackTrace();
            }
        }
    }

    private void openConnection(){
        // get file path to be usef for connection if exists
        String tempDbName = dbName != null ? dbName : DEFAULT_SMEM_DB_NAME;

        URL resourceURL = getClass().getResource("/databases/smem/" + tempDbName + ".sqlite");
        String url = "jdbc:sqlite::memory:";

        if (resourceURL != null) {
            String URL = resourceURL.getPath();
            URL = "jdbc:sqlite:" + URL.substring(1);
        }

        try {
            // it seems if we dont open the connection the addSemanticKnowledge wont take
            // place. this might be a bug from the soar. This case happens specifically for when we call this class after loading the file
            // and we pass null db name. A temp random connection need to open in such cases. This could be a bug from soar side.
            conn = JdbcTools.connect("org.sqlite.JDBC", url);
            // conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateTestData() {
        // new Random().nextInt(100)
        StringBuilder sb = new StringBuilder();
        sb.append("(<ss> ^test-landmark-properties <l1>  )")
                .append("(<l1> ^color red ^type shape2D-" + new Random().nextInt(100) + " )");

        return sb.toString();
    }

}
