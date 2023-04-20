package com.soar.agent.architecture.events;

import java.net.URL;
import java.sql.Connection;

import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.smem.SemanticMemory;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.JdbcTools;
import org.jsoar.util.adaptables.Adaptables;

public class SemanticMemoryResponder extends SemanticMemoryEvent{
    private Connection conn;
    private SemanticMemory smem;
    
    public SemanticMemoryResponder(ThreadedAgent agent) {
        super(agent);
        
        initSemanticDB();
    }

    @Override
    public void initSemanticDB() {
        //initialise smem
        smem = Adaptables.adapt(agent, SemanticMemory.class);

        //get file path to be usef for connection if exists
        URL resourceURL = getClass().getResource("/databases/smem/explore-smem-db.sqlite");
        String url = "jdbc:sqlite::memory:";

        if(resourceURL != null){
            String URL =  resourceURL.getPath();
            URL = "jdbc:sqlite:" + URL.substring(1);
        }
        
        try {
            //it seems if we dont open the connection the addSemanticKnowledge wont take place
            //this might be a bug from the soar
            conn = JdbcTools.connect("org.sqlite.JDBC", url);
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSemanticKnowledge() {
        try {
            agent.getInterpreter().eval("smem --set learning on");
            agent.getInterpreter().eval("smem --add { " + generateTestData() +"}");
            smem.smem_go(true);
            
        } catch (SoarException e) {
            e.printStackTrace();
        }
        
    }

    private String generateTestData(){
        // new Random().nextInt(100)
        StringBuilder sb = new StringBuilder();
        sb.append("(<ss> ^test-landmark-properties <l1>  )")
          .append("(<l1> ^color red ^type shape2D )");

        return sb.toString();
    }
    
}
