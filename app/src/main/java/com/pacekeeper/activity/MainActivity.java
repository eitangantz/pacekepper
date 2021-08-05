package com.pacekeeper.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.pacekeeper.R;
import com.pacekeeper.adapter.DistanceAdapter;
import com.pacekeeper.adapter.PaceAdapter;
import com.pacekeeper.model.Pace;
import com.pacekeeper.model.RunningSession;
import com.pacekeeper.utils.CenterLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    private static final int MY_PERMISSIONS_REQUEST = 35;
    private static final int REQUEST_CHECK_LOCATION_SETTINGS = 36;
    @BindView(R.id.seekbar)
    SeekBar seekBar;
    @BindView(R.id.recyclerview_pace)
    RecyclerView recyclerView;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.recyclerview_distance)
    RecyclerView recyclerViewDistance;
    private List<Pace> paces;
    private long[] paceTime = {180, 210, 240, 270, 300, 330, 360, 390, 420, 450, 480, 510, 540, 570, 600, 630, 660, 690, 710, 740, 770, 900};
    private Pace selectedPace;
    private PaceAdapter paceAdapter;
    private CenterLayoutManager centerItemLayoutManager;
    private DistanceAdapter distanceAdapter;
    private double selectedDistance = 0.5;
    private ArrayList<Double> distanceItems;
    private boolean distanceSelectedByUser = false;
    private RunningSession runningSession;
    private long calculatedTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPaceChoices();
        initDistanceChoices();
        seekBar.incrementProgressBy(1 / 2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double pr;
                pr = progress * 0.5 + 0.5;
                selectedDistance = pr;
                if (distanceAdapter != null && fromUser) {
                    int pos = distanceItems.indexOf(pr);
                    distanceAdapter.setSelectedPosition(pos);
                    centerItemLayoutManager.smoothScrollToPosition(recyclerViewDistance, null, pos);
                    distanceSelectedByUser = true;
                }
                updateTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initDistanceChoices() {
        distanceItems = new ArrayList<>();
        for (double i = 0.5; i <= 50; i += 0.5) {
            distanceItems.add(i);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels - 100;
        centerItemLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDistance.setLayoutManager(centerItemLayoutManager);
        distanceAdapter = new DistanceAdapter(distanceItems, 0, (distance, pos) -> {
            distanceSelectedByUser = true;
            distanceAdapter.setSelectedPosition(pos);
            seekBar.setProgress(((int) distance.doubleValue()) * 2 - 1);
            updateTime();
            centerItemLayoutManager.smoothScrollToPosition(recyclerViewDistance, null, pos);
        });
        recyclerViewDistance.setAdapter(distanceAdapter);
        recyclerViewDistance.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int current_position;
            private boolean direction;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    double position = centerItemLayoutManager.findFirstVisibleItemPosition()
                            + (centerItemLayoutManager.findLastVisibleItemPosition() - centerItemLayoutManager.findFirstVisibleItemPosition()) / 2.0;
                    int clear_position = direction ? (int) Math.ceil(position) : (int) position;
                    if (clear_position != current_position && !distanceSelectedByUser) {
                        centerItemLayoutManager.smoothScrollToPosition(recyclerViewDistance, null, clear_position);
                        seekBar.setProgress(clear_position);
                        distanceAdapter.setSelectedPosition(clear_position);
                        updateTime();
                        current_position = clear_position;
                    }
                    distanceSelectedByUser = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                direction = dx >= 0;
            }
        });
    }

    private void initPaceChoices() {
        paces = new ArrayList<>();
        for (long l : paceTime) {
            Pace pace = new Pace(l);
            paces.add(pace);
        }
        paceAdapter = new PaceAdapter(paces, pace -> {
            selectedPace = pace;
            updateTime();
            paceAdapter.notifyDataSetChanged();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(paceAdapter);
    }

    private void updateTime() {
        if (selectedPace != null) {
            double miles = selectedDistance;
            calculatedTime = (long) (miles * selectedPace.getTime());
            time.setText(DateUtils.formatElapsedTime(calculatedTime));
        }
    }

    @OnClick(R.id.btn_start)
    void startClick() {
        checkLocationServices();
    }

    private void checkLocationServices() {
        if (selectedPace != null && selectedDistance != 0) {
            runningSession = new RunningSession(selectedPace.getTime(), selectedDistance);
            runningSession.setTime(calculatedTime);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST);
            } else {
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                SettingsClient client = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnSuccessListener(this, locationSettingsResponse -> {
                    if (locationSettingsResponse.getLocationSettingsStates().isLocationUsable()) {
                        RunningActivity.start(MainActivity.this, runningSession);
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.location_dialog)
                                .setPositiveButton(R.string.turn_on, (dialog, which) -> {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, REQUEST_CHECK_LOCATION_SETTINGS);
                                }).create().show();
                    }
                });

                task.addOnFailureListener(this, e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException ignored) {
                        }
                    }
                });
            }
        } else {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.info_toast, findViewById(R.id.toast_layout_root));
            AlertDialog toast = new AlertDialog.Builder(this).setView(layout).create();
            layout.findViewById(R.id.button).setOnClickListener(v -> toast.dismiss());
            toast.show();
            toast.getWindow().setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL);
            toast.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            toast.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @OnClick(R.id.btn_settings)
    void settingsClick() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_history)
    void historyClick() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationServices();
                } else {
                    Toast.makeText(this, getString(R.string.permission_toast), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_CHECK_LOCATION_SETTINGS:
                RunningActivity.start(this, runningSession);
                break;
        }
    }
}
