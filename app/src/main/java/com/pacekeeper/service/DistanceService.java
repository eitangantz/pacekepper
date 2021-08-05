package com.pacekeeper.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.format.DateUtils;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.pacekeeper.R;
import com.pacekeeper.activity.RunningActivity;
import com.pacekeeper.event.RunningStatsEvent;
import com.pacekeeper.event.StateEvent;
import com.pacekeeper.model.RunningSession;
import com.pacekeeper.utils.AudioManager;
import com.pacekeeper.utils.ConvertUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.pacekeeper.activity.RunningActivity.KEY_RUNNING_SESSION;

public class DistanceService extends Service {
    private static final int DISTANCE_NOTIFICATION_ID = 18273645;
    private static final String CHANNEL_ID = "ch_01";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 100;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int NEW_LOCATION = 1;

    private static boolean isRunning;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private NotificationManager notificationManager;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location location;
    private NotificationChannel channel;
    private ArrayList<Location> locations = new ArrayList<>();
    private double distance = 0;
    private RunningSession runningSession;
    private long pauseStartTime = 0;
    private long resumeTime = 0;
    private Notification.Builder builder;
    private Timer soundTimer;
    private Timer last500mTimer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        runningSession = (RunningSession) intent.getExtras().get(KEY_RUNNING_SESSION);
        runningSession.setStartTime(Calendar.getInstance().getTimeInMillis());
        runningSession.setRunningStatus(RunningSession.RunningStatus.STARTED);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (runningSession.getRunningStatus() == RunningSession.RunningStatus.FINISHED) {
                    timer.cancel();
                }
                notificationManager.notify(DISTANCE_NOTIFICATION_ID, getNotification());
                EventBus.getDefault().post(new RunningStatsEvent(runningSession));
            }
        }, 1000, 1000);
        soundTimer = new Timer();
        soundTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (runningSession.getRunningStatus() == RunningSession.RunningStatus.FINISHED) {
                    soundTimer.cancel();
                }
                long currentPace = runningSession.getCurrentPace();
                double distance = runningSession.getResultDistanceInConfiguredUnit();
                long startedPace = runningSession.getSelectedPace();
                double finalDistance = runningSession.getDistance();
                double distanceToGo = finalDistance - distance;
                long goalTime = runningSession.getResultTime();
                long time = System.currentTimeMillis() - runningSession.getStartTime();
                long timeLeft = goalTime - time;
                long neededPace = Math.round(distanceToGo != 0 ? Math.abs(timeLeft) / distance : 0);
                double metersToGo = ConvertUtils.getMeters(distanceToGo);
                if (metersToGo < 500) {
                    last500mTimer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (runningSession.getRunningStatus() == RunningSession.RunningStatus.FINISHED) {
                                timer.cancel();
                            }
                            AudioManager.getInstance().playTrack(DistanceService.this, R.raw.go_go_go_dont_stop_now_go_faster);
                        }
                    }, 10000, 10000);
                    soundTimer.cancel();
                } else {
                    if (currentPace > startedPace) {
                        if (neededPace < currentPace) {
                            AudioManager.getInstance().playTrack(DistanceService.this, R.raw.great_pace_good_job);
                        } else {
                            AudioManager.getInstance().playTrack(DistanceService.this, R.raw.good_pace_but_not_enough);
                        }
                    } else {
                        if (neededPace < currentPace) {
                            AudioManager.getInstance().playTrack(DistanceService.this, R.raw.slow_pace_still_fine);
                        } else {
                            AudioManager.getInstance().playTrack(DistanceService.this, R.raw.slow_pace_go_faster);
                        }
                    }
                }


            }
        }, 60000, 60000);
        startForeground(DISTANCE_NOTIFICATION_ID, getNotification());
        return START_NOT_STICKY;
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, RunningActivity.class);
        notificationIntent.putExtra(KEY_RUNNING_SESSION, runningSession);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        long time = 0;
        switch (runningSession.getRunningStatus()) {
            case STARTED:
                time = Calendar.getInstance().getTimeInMillis()
                        - runningSession.getStartTime() - runningSession.getPausedDuring();
                break;
            case PAUSED:
                time = pauseStartTime
                        - runningSession.getStartTime() - runningSession.getPausedDuring();
                break;
            case FINISHED:
                time = runningSession.getEndTime()
                        - runningSession.getStartTime() - runningSession.getPausedDuring();
                break;
        }

        time /= 1000;

        if (builder == null) {
            builder = new Notification.Builder(this).setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_stat_name);
        }
        runningSession.setEndTime(Calendar.getInstance().getTimeInMillis());
        runningSession.setResultTime(time);
        builder.setContentTitle("distance: "
                + String.format("%.2f", ConvertUtils.metersToConfigured(distance)) + "/" + runningSession.getDistance())
                .setContentText("time: " + DateUtils.formatElapsedTime(time)
                        + "/" + DateUtils.formatElapsedTime(runningSession.getTime()) +
                        "\npace:" + DateUtils.formatElapsedTime(runningSession.getAveragePace()) + "/"
                        + DateUtils.formatElapsedTime(runningSession.getSelectedPace()))
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        return builder.build();
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == NEW_LOCATION && location.getAccuracy() < 50) {
                if (locations.size() > 0) {
                    if (runningSession.getRunningStatus() == RunningSession.RunningStatus.STARTED) {
                        double tempDistance = location.distanceTo(locations.get(locations.size() - 1));
                        if (tempDistance > locations.get(locations.size() - 1).getAccuracy()) {
                            distance += tempDistance;
                            long tempTime = (location.getTime() - locations.get(locations.size() - 1).getTime()) / 1000;
                            locations.add(location);
                            runningSession.getLocations().add(new LatLng(location.getLatitude(), location.getLongitude()));
                            runningSession.setCurrentPace(Math.round(tempDistance != 0 ? tempTime
                                    / ConvertUtils.metersToConfigured(tempDistance) : 0));
                            runningSession.setResultDistance(distance);
                        }
                    } else if (runningSession.getRunningStatus() == null) {
                        locations.set(0, location);
                    }
                } else {
                    locations.add(location);
                    runningSession.getLocations().add(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        }

    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (runningSession.getRunningStatus() == RunningSession.RunningStatus.FINISHED) {
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    if (soundTimer!= null){
                        soundTimer.cancel();
                    }
                    if (last500mTimer!= null){
                        last500mTimer.cancel();
                    }
                    DistanceService.this.stopSelf();
                }
                Message msg = serviceHandler.obtainMessage();
                msg.arg1 = NEW_LOCATION;
                location = locationResult.getLastLocation();
                serviceHandler.handleMessage(msg);
            }
        };

        createLocationRequest();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, serviceLooper);
        } catch (SecurityException ignored) {
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        isRunning = false;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onStateChange(StateEvent stateEvent) {
        if (runningSession.getRunningStatus() != stateEvent.state) {
            runningSession.setRunningStatus(stateEvent.state);
            switch (stateEvent.state) {
                case PAUSED:
                    pauseStartTime = Calendar.getInstance().getTimeInMillis();
                    break;
                case STARTED:
                    if (pauseStartTime != 0) {
                        resumeTime = Calendar.getInstance().getTimeInMillis();
                        runningSession.setPausedDuring(runningSession.getPausedDuring()
                                + resumeTime - pauseStartTime);
                    }
                    break;
                case FINISHED:
                    runningSession.setEndTime(Calendar.getInstance().getTimeInMillis());
                    EventBus.getDefault().post(new RunningStatsEvent(runningSession));
                    break;
            }
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
