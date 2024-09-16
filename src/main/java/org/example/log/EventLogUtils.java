package org.example.log;

import java.util.HashSet;
import java.util.Set;
import org.jbpt.pm.log.EventLog;
import org.jbpt.pm.log.Trace;

public class EventLogUtils {

    public static Set<String> getDistinctActivities(EventLog eventLog) {
        Set<String> distinctActivities = new HashSet<>();
        Set<Trace> distinctTraces = getDistinctTraces(eventLog);

        for (Trace trace : distinctTraces) {
            distinctActivities.addAll(trace);
        }

        return distinctActivities;
    }

    public static Set<Trace> getDistinctTraces(EventLog eventLog) {
        return new HashSet<>(eventLog);
    }

    public static EventLog getEventLogWithInputOutput(EventLog eventLog, String input, String output) {
        EventLog eventLogWithInputOutput = new EventLog();
        for (Trace trace : eventLog) {
            trace.add(0, input);
            trace.add(output);
            eventLogWithInputOutput.add(trace);
        }
        return eventLogWithInputOutput;
    }
}
