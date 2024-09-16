package org.example.discovery.heuristic;

import java.util.HashMap;
import java.util.Map;

public class DependencyGraph extends HashMap<String, Map<String, Double>> {

    public void printDependencyGraph() {
        System.out.print("    ");
        for (String colKey : this.keySet()) {
            System.out.print(colKey + "   ");
        }
        System.out.println();

        for (String rowKey : this.keySet()) {
            System.out.print(rowKey + "   ");
            Map<String, Double> rowMap = this.get(rowKey);
            for (String colKey : this.keySet()) {
                System.out.print(rowMap.get(colKey) + "   ");
            }
            System.out.println();
        }
    }

}
