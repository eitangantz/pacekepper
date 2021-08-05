package com.pacekeeper.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.pacekeeper.utils.ConvertUtils;

import java.util.ArrayList;

public class RunningSession implements Parcelable {
    private long selectedPace;
    private double distance;
    private String distanceType;
    private double resultDistance;
    private long time;
    private String name;
    private String description;
    private boolean isGoalCompleted;
    private RunningStatus runningStatus;
    private long startTime;
    private long pausedDuring = 0;
    private long endTime;
    private long resultTime;
    private long currentPace;
    private String imageName;
    private ArrayList<LatLng> locations = new ArrayList<>();

    public RunningSession(long selectedPace, double distance) {
        this.selectedPace = selectedPace;
        this.distance = distance;
    }

    protected RunningSession(Parcel in) {
        selectedPace = in.readLong();
        distance = in.readDouble();
        distanceType = in.readString();
        resultDistance = in.readDouble();
        time = in.readLong();
        name = in.readString();
        description = in.readString();
        isGoalCompleted = in.readByte() != 0;
        startTime = in.readLong();
        pausedDuring = in.readLong();
        endTime = in.readLong();
        resultTime = in.readLong();
        currentPace = in.readLong();
        imageName = in.readString();
        locations = in.createTypedArrayList(LatLng.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(selectedPace);
        dest.writeDouble(distance);
        dest.writeString(distanceType);
        dest.writeDouble(resultDistance);
        dest.writeLong(time);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (isGoalCompleted ? 1 : 0));
        dest.writeLong(startTime);
        dest.writeLong(pausedDuring);
        dest.writeLong(endTime);
        dest.writeLong(resultTime);
        dest.writeLong(currentPace);
        dest.writeString(imageName);
        dest.writeTypedList(locations);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RunningSession> CREATOR = new Creator<RunningSession>() {
        @Override
        public RunningSession createFromParcel(Parcel in) {
            return new RunningSession(in);
        }

        @Override
        public RunningSession[] newArray(int size) {
            return new RunningSession[size];
        }
    };

    public long getSelectedPace() {
        return selectedPace;
    }

    public void setSelectedPace(long selectedPace) {
        this.selectedPace = selectedPace;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDistanceType() {
        return distanceType;
    }

    public void setDistanceType(String distanceType) {
        this.distanceType = distanceType;
    }

    public double getResultDistanceInConfiguredUnit() {
        return ConvertUtils.metersToConfigured(resultDistance);
    }

    public void setResultDistance(double resultDistance) {
        this.resultDistance = resultDistance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getAveragePace() {
        return resultDistance != 0 ? Math.round((resultTime - pausedDuring) / ConvertUtils.metersToConfigured(resultDistance)) : 0;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isGoalCompleted() {
        return isGoalCompleted;
    }

    public void checkGoalCompleted() {
        isGoalCompleted = resultDistance >= distance
                && (resultTime - pausedDuring) < time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RunningStatus getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(RunningStatus runningStatus) {
        this.runningStatus = runningStatus;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getPausedDuring() {
        return pausedDuring;
    }

    public void setPausedDuring(long pausedDuring) {
        this.pausedDuring = pausedDuring;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getResultTime() {
        return resultTime;
    }

    public void setResultTime(long resultTime) {
        this.resultTime = resultTime;
    }

    public long getCurrentPace() {
        return currentPace;
    }

    public void setCurrentPace(long currentPace) {
        this.currentPace = currentPace;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public ArrayList<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LatLng> locations) {
        this.locations = locations;
    }

    public void copyStats(RunningSession updatedRunningSession) {
        startTime = updatedRunningSession.startTime;
        pausedDuring = updatedRunningSession.pausedDuring;
        endTime = updatedRunningSession.endTime;
        resultDistance = updatedRunningSession.resultDistance;
        resultTime = updatedRunningSession.resultTime;
        currentPace = updatedRunningSession.currentPace;
        locations = updatedRunningSession.locations;
    }

    public enum RunningStatus {
        STARTED, PAUSED, FINISHED;
    }

}
