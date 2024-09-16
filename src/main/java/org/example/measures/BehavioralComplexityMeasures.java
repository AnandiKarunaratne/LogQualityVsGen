package org.example.measures;

import org.example.log.EventLog;
import org.example.log.Trace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BehavioralComplexityMeasures {

    private final EventLog eventLog;
    private final int logSize;

    public BehavioralComplexityMeasures(EventLog eventLog) {
        this.eventLog = eventLog;
        this.logSize = eventLog.size();
    }

    public double getAverageTraceLength() {
        int totalEvents = 0;
        for (Trace trace : eventLog) {
            totalEvents += trace.size();
        }
        return totalEvents / (double) logSize;
    }

    public double getAverageTraceSize() {
        int totalEvents = 0;
        for (Trace trace : eventLog) {
            totalEvents += getTraceSize(trace);
        }
        return totalEvents / (double) logSize;
    }

    public double getDensity() {
        return getAverageTraceSize() / getAverageTraceLength();
    }

    public double getTraceHeterogeneityRate() {
        return getNumOfDistinctTraces() / (double) logSize;
    }

    public double getTraceSimilarityRate() {
        int NTC = getNumOfDistinctTraces();

        if (NTC == 1) {
            return 0;
        }
        List<Trace> distinctTraces = getDistinctTraces();
        double total = 0;

        for (int i = 0; i < NTC; i++) {
            for (int j = i + 1; j < NTC; j++) {
                Trace trace1 = distinctTraces.get(i);
                Trace trace2 = distinctTraces.get(j);

                int maxLength = Math.max(trace1.size(), trace2.size());
                int levenshteinDistance = getLevenshteinDistance(trace1, trace2);
                double value = (maxLength - levenshteinDistance) / (double) maxLength;
                total += value;
            }
        }
        return total / (NTC * (NTC - 1));
    }

    public double getComplexityFactor() {
        int NTC = getNumOfDistinctTraces();
        double TSR = getTraceSimilarityRate();
        double density = getDensity();
        double ATS = getAverageTraceSize();

        return Math.pow(NTC, ((1 - TSR) + density)) * ATS;
    }

    public Complexity getComplexityFactorDesc() {
        return getComplexity(getComplexityFactor());
    }

    private int getTraceSize(Trace trace) {
        Set<String> distinctEvents = new HashSet<>(trace);
        return distinctEvents.size();
    }

    private int getNumOfDistinctTraces() {
        Set<Trace> distinctTraces = new HashSet<>(eventLog);
        return distinctTraces.size();
    }

    private List<Trace> getDistinctTraces() {
        Set<Trace> distinctTraces = new HashSet<>(eventLog);
        return new ArrayList<>(distinctTraces);
    }

    private int getLevenshteinDistance(List<String> trace1, List<String> trace2) {
        int len1 = trace1.size();
        int len2 = trace2.size();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = trace1.get(i - 1).equals(trace2.get(j - 1)) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }

    private Complexity getComplexity(double value) {
        if (Double.isNaN(value) || value < 0) {
            return null;
        } else if (value < 30) {
            return Complexity.SIMPLE;
        } else if (value < 53) {
            return Complexity.SOMEWHAT_COMPLEX;
        } else if (value < 75) {
            return Complexity.COMPLEX;
        } else if (value < 95) {
            return Complexity.VERY_COMPLEX;
        } else {
            return Complexity.HIGHLY_COMPLEX;
        }
    }

}
