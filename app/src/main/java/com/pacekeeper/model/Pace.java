package com.pacekeeper.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Pace implements Parcelable {
    private String timeText;
    private long time;

    public Pace(long time) {
        long minutes = time / 60;
        long seconds = time % 60;
        if (seconds == 0) {
            this.timeText = minutes + ":" + seconds + "0";
        } else {
            this.timeText = minutes + ":" + seconds;
        }
        this.time = time;
    }

    protected Pace(Parcel in) {
        timeText = in.readString();
        time = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timeText);
        dest.writeLong(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Pace> CREATOR = new Creator<Pace>() {
        @Override
        public Pace createFromParcel(Parcel in) {
            return new Pace(in);
        }

        @Override
        public Pace[] newArray(int size) {
            return new Pace[size];
        }
    };

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
