package com.pacekeeper.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.pacekeeper.R;
import com.pacekeeper.event.RunningStatsEvent;
import com.pacekeeper.event.StateEvent;
import com.pacekeeper.listener.RunningSessionListener;
import com.pacekeeper.model.RunningSession;
import com.pacekeeper.service.DistanceService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class RunningActivity extends BaseActivity implements RunningSessionListener {
    public static final String KEY_RUNNING_SESSION = "running session";
    private static final Object RUNNING_NOTIFICATION_ID = 5;
    @BindView(R.id.toobar_title)
    TextView toolbarTitle;
    @BindView(R.id.layout_btn_choise)
    ViewGroup layoutBtnChoice;
    @BindView(R.id.btn_pause)
    ViewGroup btnPause;
    @BindView(R.id.buttons)
    ViewGroup bottomButtons;
    private RunningSession runningSession;

    private NavController navController;
    private UpdateCallback updateCallback;

    public static void start(@NonNull Context context, RunningSession runningSession) {
        Intent intent = new Intent(context, RunningActivity.class);
        intent.putExtra(KEY_RUNNING_SESSION, runningSession);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(KEY_RUNNING_SESSION)) {
            runningSession = getIntent().getExtras().getParcelable(KEY_RUNNING_SESSION);
            updateCallback.update();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerEventBus();
        if (hasExtras()) {
            runningSession = getIntent().getExtras().getParcelable(KEY_RUNNING_SESSION);
        }
        navController = Navigation.findNavController(this, R.id.running_activity_fragment_container);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.runningFragment) {
                toolbarTitle.setText(R.string.your_goal);
                if (!DistanceService.isRunning()) {
                    Intent intent = new Intent(this, DistanceService.class);
                    intent.putExtra(KEY_RUNNING_SESSION, runningSession);
                    startService(intent);
                }
            } else if (destination.getId() == R.id.mapFragment) {
                toolbarTitle.setText(R.string.map);
            }
        });
    }

    @OnClick(R.id.btn_pause)
    void pauseClick() {
        layoutBtnChoice.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
        if (runningSession.getRunningStatus() != RunningSession.RunningStatus.PAUSED) {
            runningSession.setRunningStatus(RunningSession.RunningStatus.PAUSED);
            EventBus.getDefault().post(new StateEvent(RunningSession.RunningStatus.PAUSED));
        }
    }

    @OnClick(R.id.btn_resume)
    void resumeClick() {
        btnPause.setVisibility(View.VISIBLE);
        layoutBtnChoice.setVisibility(View.GONE);
        if (runningSession.getRunningStatus() != RunningSession.RunningStatus.STARTED) {
            runningSession.setRunningStatus(RunningSession.RunningStatus.STARTED);
            EventBus.getDefault().post(new StateEvent(RunningSession.RunningStatus.STARTED));
        }
    }

    @OnClick(R.id.btn_finish)
    void finishClick() {
        if (runningSession.getRunningStatus() != RunningSession.RunningStatus.FINISHED) {
            runningSession.setRunningStatus(RunningSession.RunningStatus.FINISHED);
            runningSession.checkGoalCompleted();
            EventBus.getDefault().post(new StateEvent(RunningSession.RunningStatus.FINISHED));
        }
    }

    @OnClick(R.id.btn_settings)
    void settingsClick() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_running;
    }

    @Override
    public void setSession(RunningSession runningSession) {
        this.runningSession = runningSession;
    }

    @Override
    public RunningSession getSession() {
        return runningSession;
    }

    @Override
    public void setUpdateCallback(UpdateCallback updateCallback) {
        this.updateCallback = updateCallback;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateChange(StateEvent stateEvent) {
        if (runningSession.getRunningStatus() != stateEvent.state) {
            runningSession.setRunningStatus(stateEvent.state);
            switch (stateEvent.state) {
                case PAUSED:
                    pauseClick();
                    break;
                case STARTED:
                    resumeClick();
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatsChange(RunningStatsEvent statEvent) {
        runningSession.copyStats(statEvent.runningSession);
        if (updateCallback != null) {
            updateCallback.update();
        }
        if(runningSession.getRunningStatus() == RunningSession.RunningStatus.FINISHED) {
            bottomButtons.setVisibility(View.GONE);
            navController.navigate(R.id.mapFragment);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterEventBus();
    }

    @Override
    public void onBackPressed() {
    }
}
