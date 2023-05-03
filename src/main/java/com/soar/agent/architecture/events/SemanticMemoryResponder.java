package com.soar.agent.architecture.events;

import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.smem.SemanticMemory;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.JdbcTools;
import org.jsoar.util.adaptables.Adaptables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;
import com.soar.agent.architecture.enums.MemoryEnum;
import com.soar.agent.architecture.robot.RobotAgent;

@Service
public class SemanticMemoryResponder extends SemanticMemoryEvent {
    private final String DEFAULT_SMEM_DB_NAME = "smem-default-db";
    private Connection conn;
    private SemanticMemory smem;

    public SemanticMemoryResponder(@Autowired RobotAgent agent,
            @Value("${unknown.param:explore-smem-db2}") String dbName) {
        super(agent, dbName);
        initSemanticDB();
    }

    @Override
    public void initSemanticDB() {
        smem = Adaptables.adapt(robotAgent.getThreadedAgent(), SemanticMemory.class);
        openConnection();

    }

    // @Override
    // public void addSemanticKnowledge(SemanticMemoryEntity semanticMemoryEntity) {
    // try {
    // robotAgent.getThreadedAgent().getInterpreter().eval("smem --add { " +
    // generateTestData() + "}");
    // smem.smem_go(true);

    // } catch (SoarException e) {
    // e.printStackTrace();
    // }

    // }

    @Override
    public void addSemanticKnowledge(SemanticMemoryEntity semanticMemoryEntity) {
        try {
            if (semanticMemoryEntity != null) {
                StringBuilder sb = new StringBuilder();
                String name = semanticMemoryEntity.getName() != null ? semanticMemoryEntity.getName() : "default-name";
                
                String identifierName = "<" + name.substring(0, 2) + ">";
                // add the main name. top hierarchy/parent name
                sb.append("(<ss> ^").append(name).append(" ").append(identifierName).append(" )");

                if (semanticMemoryEntity.getAttributes() != null && semanticMemoryEntity.getAttributes().size() > 0) {
                    for (Map.Entry<String, List<String>> pair : semanticMemoryEntity.getAttributes().entrySet()) {
                        if (pair.getValue() != null && pair.getValue().size() > 0) {

                            for(String keyValue: pair.getValue()){
                                sb.append("( ")
                                .append(identifierName)
                                .append(" ^")
                                .append(pair.getKey())
                                .append(" ")
                                .append(keyValue)
                                .append(" )");
                            }
                        }
                    }
                }

                if(sb.length() != 0){
                    System.out.println("adding smem data : "+ sb.toString());
                    robotAgent.getThreadedAgent().getInterpreter().eval("smem --add { " + sb.toString() + "}");
                    smem.smem_go(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void manuallyEnableDB() {

        if (dbName != null) {
            try {
                robotAgent.getThreadedAgent().getInterpreter().eval("smem --set learning on");
                robotAgent.getThreadedAgent().getInterpreter()
                        .eval("smem --set path src/main/resources/databases/smem/" + dbName + ".sqlite");
                robotAgent.getThreadedAgent().getInterpreter().eval("smem --set append-database on");
                robotAgent.getThreadedAgent().getInterpreter().eval("smem --set lazy-commit off");

            } catch (SoarException e) {
                e.printStackTrace();
            }
        }
    }

    private void openConnection() {
        // get file path to be usef for connection if exists
        String tempDbName = dbName != null ? dbName : MemoryEnum.DEFAULT_SMEM_DB_NAME.getName();

        URL resourceURL = getClass().getResource("/databases/smem/" + tempDbName + ".sqlite");
        String url = "jdbc:sqlite::memory:";

        if (resourceURL != null) {
            String URL = resourceURL.getPath();
            URL = "jdbc:sqlite:" + URL.substring(1);
        }

        try {
            // it seems if we dont open the connection the addSemanticKnowledge wont take
            // place. this might be a bug from the soar. This case happens specifically for
            // when we call this class after loading the file
            // and we pass null db name. A temp random connection need to open in such
            // cases. This could be a bug from soar side.
            conn = JdbcTools.connect("org.sqlite.JDBC", url);
            conn.close();

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

    @Override
    public Set<String> getAttributeValues(String attributeName) {
        Set<String> result = new HashSet<String>();
        attributeName = attributeName.toLowerCase();

        if (robotAgent != null) {
            try {

                StringWriter sw = new StringWriter();
                robotAgent.getThreadedAgent().getPrinter().pushWriter(sw);

                // print the smem data
                robotAgent.getThreadedAgent().getInterpreter().eval("print @");

                // detach the writer
                robotAgent.getThreadedAgent().getPrinter().popWriter();

                // get string writer result
                String writerResult = sw.toString();

                // split by whitespaces
                String[] split = writerResult.split("\\s+");

                for (int i = split.length - 1; i >= 0; i--) {
                    String current = split[i];

                    // for example contains ^color
                    if (current.contains("^" + attributeName)) {
                        if (i + 1 < split.length) {
                            String value = split[i + 1].toLowerCase();
                            result.add(value.replace("|", ""));
                        }
                    }
                }
            } catch (SoarException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public Map<String, Set<String>> retrieveAllAttributes() {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();

        if (robotAgent != null) {
            try {

                StringWriter sw = new StringWriter();
                robotAgent.getThreadedAgent().getPrinter().pushWriter(sw);

                // print the smem data
                robotAgent.getThreadedAgent().getInterpreter().eval("print @");

                // detach the writer
                robotAgent.getThreadedAgent().getPrinter().popWriter();

                // get string writer result
                String writerResult = sw.toString();

                // split by whitespaces
                String[] split = writerResult.split("\\s+");

                for (int i = split.length - 1; i >= 0; i--) {
                    String attributeName = split[i];

                    // for example contains ^color
                    if (attributeName.startsWith("^")) {
                        attributeName = attributeName.toLowerCase().replace("^", "");

                        if (i + 1 < split.length) {
                            String attributeValue = split[i + 1].toLowerCase();

                            if (!attributeValue.startsWith("<") && !attributeValue.startsWith("@")) {
                                attributeValue = attributeValue.toLowerCase().replace("|", "");

                                Set<String> previousSet = new HashSet<String>();

                                if (result.containsKey(attributeName)) {
                                    previousSet.addAll(result.get(attributeName));

                                }
                                previousSet.add(attributeValue);

                                result.put(attributeName, previousSet);
                            }
                        }
                    }
                }
            } catch (SoarException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
