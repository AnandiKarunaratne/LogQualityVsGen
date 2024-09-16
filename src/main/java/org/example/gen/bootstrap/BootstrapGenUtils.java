package org.example.gen.bootstrap;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.discovery.heuristic.Heuristic;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.pm.gen.bootstrap.BootstrapGeneralization;
import org.jbpt.pm.gen.bootstrap.Generalization;
import org.jbpt.pm.log.EventLog;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BootstrapGenUtils {

    // read a csv file and get log name, bootstrap settings
    // discover the dfg
    // get generalization
    // write to a new csv with the log, noise level, and bootstrap settings

    public static void main(String[] args) {
        String inputCsvFile = "results.25.08.224.csv";
        String outputCsvFile = "output.csv";

        try (CSVReader reader = new CSVReader(new FileReader(inputCsvFile))) {
            List<String[]> records = reader.readAll();
            records.remove(0);

            try (FileWriter writer = new FileWriter(outputCsvFile, true)) {
                for (String[] record : records) {
                    String noise = record[2];

                    if (noise.equals("0.5") || noise.equals("1")) { // noisy
                        String logFilePath = record[1];
                        int logSize = Integer.parseInt(record[3]);
                        int k = Integer.parseInt(record[10]);
                        int g = Integer.parseInt(record[11]);
                        int n = Integer.parseInt(record[12]);
                        int m = 128;
                        double p = 1.0;

                        EventLog eventLog = new EventLog().parseEventLogFromXES(logFilePath);
                        DirectedGraph dfg = new Heuristic(eventLog).getDirectedGraph();

                        Generalization generalization = BootstrapGeneralization.getBootstrapGeneralization(dfg, eventLog, n, m, g, k, p, false);
                        String csvLine = logSize + "," + k + "," + g + "," + n + "," + m + "," + p + "," + generalization;
                        writer.write(csvLine + "\n");
                    }
                }
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
