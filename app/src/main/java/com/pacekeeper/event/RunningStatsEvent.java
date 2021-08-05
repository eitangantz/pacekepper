package com.pacekeeper.event;

import com.pacekeeper.model.RunningSession;

public class RunningStatsEvent {

    public RunningSession runningSession;

    public RunningStatsEvent(RunningSession runningSession) {
        this.runningSession = runningSession;
    }
}
