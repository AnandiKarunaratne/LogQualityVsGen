package org.example.discovery.heuristic;

import org.jbpt.pm.log.EventLog;
import org.example.log.EventLogUtils;
import org.jbpt.pm.log.Trace;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;

import java.util.*;


public class Heuristic {

    private final int POSITIVE_OBSERVATIONS;
    private final double DEPENDENCY_THRESHOLD;
    private final double RELATIVE_TO_BEST_THRESHOLD;
    private final EventLog EVENT_LOG;
    private final Map<String, Integer> DF_RELATIONS = new HashMap<>();
    private final DependencyGraph DEPENDENCY_GRAPH = new DependencyGraph();
    private final Set<String> DISTINCT_ACTIVITIES;
    private final double AND_THRESHOLD = 0.1;
    private final DependencyGraph dependencyGraphWithoutFreq = new DependencyGraph();

    public Heuristic(EventLog eventLog) {
        this.EVENT_LOG = EventLogUtils.getEventLogWithInputOutput(eventLog, "INPUT", "OUTPUT");
        this.DISTINCT_ACTIVITIES = EventLogUtils.getDistinctActivities(EVENT_LOG);
        this.POSITIVE_OBSERVATIONS = 1;
        this.DEPENDENCY_THRESHOLD = 0.45;
        this.RELATIVE_TO_BEST_THRESHOLD = 0.4;
        setCommonProperties();
    }

    public Heuristic(EventLog eventLog, int positiveObservations, double dependencyThreshold, double relativeToBestThreshold) {
        this.EVENT_LOG = eventLog;
        this.DISTINCT_ACTIVITIES = EventLogUtils.getDistinctActivities(EVENT_LOG);
        this.POSITIVE_OBSERVATIONS = positiveObservations;
        this.DEPENDENCY_THRESHOLD = dependencyThreshold;
        this.RELATIVE_TO_BEST_THRESHOLD = relativeToBestThreshold;
        setCommonProperties();
    }

    public DependencyGraph getDependencyGraph() {
        return DEPENDENCY_GRAPH;
    }

    public DependencyGraph getDependencyGraphWithoutFreq() {
        return dependencyGraphWithoutFreq;
    }

    private void setCommonProperties() {
        setDirectlyFollowsRelations();
        buildBasicDependencyGraph();
        updateDependencyGraphWithDependencyThreshold();
        updateDependencyGraphWithRelativeToBestThreshold();
        identifyAnd();
        updateDFG();
    }

    private void setDirectlyFollowsRelations() {
        Map<String, Integer> tempDirectlyFollowsRelations = new HashMap<>();
        // update the directly follows relations with all possible relations.
        for (Trace trace : EVENT_LOG) {
            for (int i = 0; i < trace.size() - 1; i++) {
                String key = getDirectlyFollowsKey(trace.get(i), trace.get(i + 1));
                tempDirectlyFollowsRelations.put(key, tempDirectlyFollowsRelations.getOrDefault(key, 0) + 1);
            }
        }

        // check for the frequency and only get the directly follows relations occurring at least PO times.
        for (String key : tempDirectlyFollowsRelations.keySet()) {
            if (tempDirectlyFollowsRelations.get(key) >= POSITIVE_OBSERVATIONS) {
                DF_RELATIONS.put(key, tempDirectlyFollowsRelations.get(key));
            }
        }
    }

    private void buildBasicDependencyGraph() {
        for (String A : DISTINCT_ACTIVITIES) {
            DEPENDENCY_GRAPH.putIfAbsent(A, new HashMap<>());
            for (String B : DISTINCT_ACTIVITIES) {
                if (!A.equals(B)) {
                    int AB = DF_RELATIONS.getOrDefault(getDirectlyFollowsKey(A, B), 0);
                    int BA = DF_RELATIONS.getOrDefault(getDirectlyFollowsKey(B, A), 0);
                    double dependencyMeasure = (double) (AB - BA) / (AB + BA + 1);
                    DEPENDENCY_GRAPH.get(A).put(B, dependencyMeasure);
                }
            }
        }

        updateDependencyGraphForSelfLoops();
        updateDependencyGraphForShortLoops();
    }

    private void updateDependencyGraphForSelfLoops() {
        for (String A : DISTINCT_ACTIVITIES) {
            int frequency = DF_RELATIONS.getOrDefault(getDirectlyFollowsKey(A, A), 0);
            double dependencyRelation = frequency / (frequency + 1.0);
            DEPENDENCY_GRAPH.get(A).put(A, dependencyRelation);
        }
    }

    private void updateDependencyGraphForShortLoops() {
        Map<String, Map<String, Integer>> shortLoops = new HashMap<>();
        for (Trace trace : EVENT_LOG) {
            for (int i = 0; i < trace.size() - 2; i++) {
                String A = trace.get(i);
                String B = trace.get(i + 1);
                String C = trace.get(i + 2);
                if (A.equals(C) && !A.equals(B)){ // pattern ABA
                    shortLoops.putIfAbsent(A, new HashMap<>());
                    shortLoops.get(A).put(B, shortLoops.get(A).getOrDefault(B, 0) + 1);
                }
            }
        }
        for (String A : shortLoops.keySet()) {
            for (String B : shortLoops.get(A).keySet()) {
                int ABA = shortLoops.get(A).get(B); // ABA
                int BAB = shortLoops.getOrDefault(B, new HashMap<>()).getOrDefault(A, 0); // BAB
                double dependencyRelation = (ABA + BAB) / (ABA + BAB + 1.0);
                if (DEPENDENCY_GRAPH.containsKey(A) && DEPENDENCY_GRAPH.containsKey(B)) {
                    DEPENDENCY_GRAPH.get(A).put(B, dependencyRelation);
                    DEPENDENCY_GRAPH.get(B).put(A, dependencyRelation);
                }
            }
        }
    }

    private void updateDependencyGraphWithDependencyThreshold() {
        for (String from : DEPENDENCY_GRAPH.keySet()) {
            for (String to : DEPENDENCY_GRAPH.get(from).keySet()) {
                // remove the dependencies measures lower than the threshold
                if (!(DEPENDENCY_GRAPH.get(from).get(to) > DEPENDENCY_THRESHOLD)) {
                    DEPENDENCY_GRAPH.get(from).put(to, 0.0);
                }
            }
        }
    }

    private void updateDependencyGraphWithRelativeToBestThreshold() {
        Map<String, Double> axValues = new HashMap<>();
        Map<String, Double> ybValues = new HashMap<>();

        // update the ax yb values.
        for (String A : DEPENDENCY_GRAPH.keySet()) {
            for (String B : DEPENDENCY_GRAPH.get(A).keySet()) {
                double dependencyMeasure = DEPENDENCY_GRAPH.get(A).get(B);
                double ax = axValues.getOrDefault(A, 0.0);
                if (dependencyMeasure > ax) {
                    axValues.put(A, dependencyMeasure);
                }
                double yb = ybValues.getOrDefault(B, 0.0);
                if (dependencyMeasure > yb) {
                    ybValues.put(B, dependencyMeasure);
                }
            }
        }

        for (String A : DEPENDENCY_GRAPH.keySet()) {
            for (String B : DEPENDENCY_GRAPH.get(A).keySet()) {
                double ab = DEPENDENCY_GRAPH.get(A).get(B);
                double ax = axValues.getOrDefault(A, 0.0);
                double yb = ybValues.getOrDefault(B, 0.0);

                if (Math.abs(ab - ax) > RELATIVE_TO_BEST_THRESHOLD) {
                    if (Math.abs(ab - yb) > RELATIVE_TO_BEST_THRESHOLD) {
                        DEPENDENCY_GRAPH.get(A).put(B, 0.0);
                    }
                }
            }
        }
    }

    private void identifyAnd() {
        for (String A : DISTINCT_ACTIVITIES) {
            for (String B : DISTINCT_ACTIVITIES) {
                if (!A.equals(B)) {
                    String AB = getDirectlyFollowsKey(A, B);
                    if (DF_RELATIONS.containsKey(AB)) {
                        int ab = DF_RELATIONS.get(AB);
                        for (String C : DISTINCT_ACTIVITIES) {
                            if (!A.equals(C) && !B.equals(C)) {
                                String AC = getDirectlyFollowsKey(A, C);
                                String BC = getDirectlyFollowsKey(B, C);
                                String CB = getDirectlyFollowsKey(C, B);

                                if (DF_RELATIONS.containsKey(AC) && DF_RELATIONS.containsKey(BC) && DF_RELATIONS.containsKey(CB)) {
                                    int ac = DF_RELATIONS.getOrDefault(getDirectlyFollowsKey(A, C), 0);
                                    int bc = DF_RELATIONS.getOrDefault(getDirectlyFollowsKey(B, C), 0);
                                    int cb = DF_RELATIONS.getOrDefault(getDirectlyFollowsKey(C, B), 0);

                                    double dependencyRelation = (bc + cb) / (ab + ac + 1.0);

                                    if (dependencyRelation > AND_THRESHOLD) {
                                        if (DEPENDENCY_GRAPH.get(B).containsKey(C)) {
                                            if (DEPENDENCY_GRAPH.get(B).get(C) < dependencyRelation) {
                                                DEPENDENCY_GRAPH.get(B).put(C, dependencyRelation);
                                            }
                                        }
                                    }
                                    else {
                                        System.out.println(A + ", " + B + ", " + C);
                                    }
                                }
                            }
                        }
                    }
                }


            }
        }
    }

    private void updateDFG() {
        for (String from : DEPENDENCY_GRAPH.keySet()) {
            dependencyGraphWithoutFreq.putIfAbsent(from, new HashMap<>());
            for (String to : DEPENDENCY_GRAPH.get(from).keySet()) {
                if (DEPENDENCY_GRAPH.get(from).get(to) > 0) {
                    dependencyGraphWithoutFreq.get(from).put(to, 1.0);
                } else {
                    dependencyGraphWithoutFreq.get(from).put(to, 0.0);
                }
            }
        }
    }

    private String getDirectlyFollowsKey(String A, String B) {
        return A + "->" + B;
    }

    public DirectedGraph getDirectedGraph() {
        DirectedGraph graph = new DirectedGraph();
        Map<String, Vertex> vertices = new HashMap<>();
        for (String activity : DISTINCT_ACTIVITIES) {
            vertices.putIfAbsent(activity, new Vertex(activity));
        }

        for (String from : dependencyGraphWithoutFreq.keySet()) {
            for (String to : dependencyGraphWithoutFreq.get(from).keySet()) {
                if (dependencyGraphWithoutFreq.get(from).get(to) > 0) {
                    Vertex fromVertex = vertices.get(from);
                    Vertex toVertex = vertices.get(to);
                    graph.addEdge(fromVertex, toVertex);
                }
            }
        }
        return graph;
    }

}
