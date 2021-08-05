package com.pacekeeper.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.pacekeeper.R;
import com.pacekeeper.model.RunningSession;
import com.pacekeeper.utils.DataManager;
import com.pacekeeper.utils.FileUtils;
import com.pacekeeper.utils.PrefUtils;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<RunningSession> runningSessions;

    public HistoryAdapter() {
        runningSessions = PrefUtils.getInstance().getRunningSession();
        Collections.reverse(runningSessions);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(runningSessions.get(position));
    }

    @Override
    public int getItemCount() {
        return runningSessions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        AppCompatTextView title;
        @BindView(R.id.description)
        AppCompatTextView description;
        @BindView(R.id.map_image)
        AppCompatImageView mapImage;
        @BindView(R.id.status)
        AppCompatTextView status;
        @BindView(R.id.time)
        AppCompatTextView startTime;
        @BindView(R.id.distance)
        AppCompatTextView distance;
        @BindView(R.id.distance_units)
        AppCompatTextView distanceUnits;
        @BindView(R.id.current_time)
        AppCompatTextView currentTime;
        @BindView(R.id.current_avg_pace)
        AppCompatTextView currentAvgPace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(RunningSession runningSession) {
            title.setText(runningSession.getName());
            description.setText(runningSession.getDescription());
            setPace(runningSession.getAveragePace(), currentAvgPace);
            distance.setText(String.format("%.2f", runningSession.getResultDistanceInConfiguredUnit()));
            distanceUnits.setText(PrefUtils.getInstance().getMeasurement().equals("Miles") ? "Mile" : "km");
            setGoalTime(runningSession);
            startTime.setText(DataManager.getInstance().getDateWithTime(runningSession.getStartTime()));
            if (runningSession.isGoalCompleted()) {
                status.setText(R.string.completed);
                status.setTextColor(Color.parseColor("#26A16E"));
                status.setBackgroundResource(R.drawable.bg_green);
            } else {
                status.setText(R.string.not_completed);
                status.setTextColor(Color.parseColor("#F92121"));
                status.setBackgroundResource(R.drawable.bg_red);
            }
            mapImage.setImageDrawable(FileUtils.getImage(runningSession.getImageName()));
        }

        private void setPace(long pace, TextView textView) {
            long minutes = pace / 60;
            long seconds = pace - minutes * 60;
            if (seconds == 0) {
                currentAvgPace.setText(minutes + ":" + seconds + "0");
            } else {
                currentAvgPace.setText(minutes + ":" + seconds);
            }
        }

        private void setGoalTime(RunningSession runningSession) {
            double miles = runningSession.getDistance();
            long calculatedTime = (long) (miles * runningSession.getSelectedPace());
            long hours = calculatedTime / 3600;
            long minutes = 0;
            long seconds = 0;
            if (hours > 0) {
                minutes = (calculatedTime - (hours * 3600)) / 60;
            } else {
                minutes = calculatedTime / 60;
            }
            if (hours > 0) {
                seconds = calculatedTime - ((hours * 3600) + (minutes * 60));
            } else {
                seconds = calculatedTime - minutes * 60;
            }
            String sec;
            String min;
            if (seconds == 0) {
                sec = seconds + "0";
            } else if (seconds < 10) {
                sec = "0" + seconds;
            } else {
                sec = String.valueOf(seconds);
            }

            if (minutes == 0) {
                min = minutes + "0";
            } else if (minutes < 10) {
                min = "0" + minutes;
            } else {
                min = String.valueOf(minutes);
            }

            if (hours <= 0 && seconds == 0 && minutes == 0) {
                currentTime.setText("00:00");
            } else if (hours <= 0) {
                currentTime.setText(min + ":" + sec);
            } else {
                currentTime.setText(hours + ":" + min + ":" + sec);
            }
        }
    }
}
