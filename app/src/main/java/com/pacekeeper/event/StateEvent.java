package com.pacekeeper.event;

import com.pacekeeper.model.RunningSession;

public class StateEvent {

    public StateEvent(RunningSession.RunningStatus state) {
        this.state = state;
    }

    public RunningSession.RunningStatus state;
}
