package org.example;

import org.example.discovery.heuristic.DependencyGraph;
import org.example.discovery.heuristic.Heuristic;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.pm.gen.bootstrap.BootstrapGeneralization;
import org.jbpt.pm.gen.bootstrap.Generalization;
import org.jbpt.pm.log.EventLog;
import org.jbpt.pm.log.Trace;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
//        EventLog eventLog = new EventLog().parseEventLogFromXES("src/main/resources/log1.xes");
        Heuristic heuristic = new Heuristic(sampleEventLog());
//        heuristic.getDependencyGraph().printDependencyGraph();
//        heuristic.getDependencyGraphWithoutFreq().printDependencyGraph();
//        System.out.println();
        DirectedGraph dfg = heuristic.getDirectedGraph();
        Generalization generalization = BootstrapGeneralization.getBootstrapGeneralization(dfg, sampleEventLog(), 100, 100, 100, 2, 1, false);
        System.out.println(generalization);
    }

    private static EventLog sampleEventLog() {
        EventLog eventLog = new EventLog();
        for (int i = 0; i < 5; i++) {
            eventLog.add(new Trace(Arrays.asList("a", "b", "b", "b", "c", "f")));
        }
        for (int i = 0; i < 20; i++) {
            eventLog.add(new Trace(Arrays.asList("a", "b", "c", "f")));
        }
        for (int i = 0; i < 1; i++) {
            eventLog.add(new Trace(Arrays.asList("a", "d", "d", "e", "f")));
        }
        for (int i = 0; i < 10; i++) {
            eventLog.add(new Trace(Arrays.asList("a", "d", "e", "e", "f")));
        }
        for (int i = 0; i < 10; i++) {
            eventLog.add(new Trace(Arrays.asList("a", "d", "e", "f", "a", "b", "c", "f", "a", "d", "e", "f")));
        }
        for (int i = 0; i < 20; i++) {
            eventLog.add(new Trace(Arrays.asList("a", "d", "e", "f")));
        }
        return eventLog;
    }

}