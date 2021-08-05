package com.pacekeeper.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pacekeeper.R;
import com.pacekeeper.listener.RunningSessionListener;
import com.pacekeeper.model.RunningSession;
import com.pacekeeper.utils.PrefUtils;

import butterknife.BindView;

public class RunningFragment extends BaseFragment {
    @BindView(R.id.distance)
    TextView distance;
    @BindView(R.id.distance_type)
    TextView distanceType;
    @BindView(R.id.distance_type_1)
    TextView distanceType1;
    @BindView(R.id.av_pace)
    TextView avgPace;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.current_distance)
    TextView currentDistance;
    @BindView(R.id.current_pace)
    TextView currentPace;
    @BindView(R.id.current_time)
    TextView currentTime;
    @BindView(R.id.current_avg_pace)
    TextView currentAvgPace;

    private RunningSessionListener runningSessionListener;
    private RunningSession runningSession;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RunningSessionListener) {
            runningSessionListener = (RunningSessionListener) context;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        runningSession = runningSessionListener.getSession();
        runningSessionListener.setUpdateCallback(() -> {
            runningSession = runningSessionListener.getSession();
            if (runningSession != null) {
                init();
            }
            currentDistance.setText(String.format("%.2f", runningSession.getResultDistanceInConfiguredUnit()));
            currentPace.setText(DateUtils.formatElapsedTime(runningSession.getCurrentPace()));
            currentTime.setText(DateUtils.formatElapsedTime(runningSession.getResultTime()));
            currentAvgPace.setText(DateUtils.formatElapsedTime(runningSession.getAveragePace()));
        });
        if (runningSession != null) {
            init();
        }
        currentDistance.setText("0");
        currentPace.setText("00:00");
        currentTime.setText("00:00");
        currentAvgPace.setText("00:00");
    }

    private void init() {
        distance.setText(String.valueOf(runningSession.getDistance()));
        setGoalTime();
        setGoalAvgPace();
    }

    private void setGoalAvgPace() {
        long minutes = runningSession.getSelectedPace() / 60;
        long seconds = runningSession.getSelectedPace() - minutes * 60;
        if (seconds == 0) {
            avgPace.setText(minutes + ":" + seconds + "0");
        } else {
            avgPace.setText(minutes + ":" + seconds);
        }
    }

    private void setGoalTime() {
        double miles = runningSession.getDistance();
        time.setText(DateUtils.formatElapsedTime(runningSession.getTime()));
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean miles = PrefUtils.getInstance().getMeasurement().equals("Miles");
        distanceType.setText(miles ? R.string.mile : R.string.km);
        distanceType1.setText(miles ? R.string.mile : R.string.km);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_running;
    }
}
