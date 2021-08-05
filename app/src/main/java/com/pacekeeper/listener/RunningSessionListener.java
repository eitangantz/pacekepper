package com.pacekeeper.listener;

import com.pacekeeper.model.RunningSession;

public interface RunningSessionListener {
    void setSession(RunningSession runningSession);

    RunningSession getSession();

    void setUpdateCallback(UpdateCallback updateCallback);

    public interface UpdateCallback {
        void update();
    }
}
