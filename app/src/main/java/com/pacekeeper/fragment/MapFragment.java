package com.pacekeeper.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pacekeeper.R;
import com.pacekeeper.activity.RunningActivity;
import com.pacekeeper.listener.RunningSessionListener;
import com.pacekeeper.model.RunningSession;
import com.pacekeeper.utils.FileUtils;
import com.pacekeeper.utils.PrefUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @BindView(R.id.top_goal_completed)
    LinearLayout topGoalCompleted;
    @BindView(R.id.top_goal_not_completed)
    LinearLayout topGoalNotCompleted;
    @BindView(R.id.map)
    MapView mapView;
    @BindView(R.id.distance)
    TextView distance;
    @BindView(R.id.distance_units)
    TextView distanceUnits;
    @BindView(R.id.current_time)
    TextView currentTime;
    @BindView(R.id.current_avg_pace)
    TextView currentAvgPace;
    @BindView(R.id.activity_name)
    EditText activityName;
    @BindView(R.id.activity_description)
    EditText activityDescription;
    private RunningSessionListener runningSessionListener;
    private RunningSession runningSession;
    private GoogleMap googleMap;

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
            setDataInUI();
        });
        setDataInUI();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private void setDataInUI() {
        if (runningSession != null) {
            distance.setText(String.format("%.2f", runningSession.getResultDistanceInConfiguredUnit()));
            distanceUnits.setText(PrefUtils.getInstance().getMeasurement().equals("Miles") ? "Mile" : "km");
            currentTime.setText(DateUtils.formatElapsedTime(runningSession.getResultTime()));
            currentAvgPace.setText(DateUtils.formatElapsedTime(runningSession.getAveragePace()));
            if (runningSession.isGoalCompleted()) {
                topGoalCompleted.setVisibility(View.VISIBLE);
                topGoalNotCompleted.setVisibility(View.GONE);
            } else {
                topGoalCompleted.setVisibility(View.GONE);
                topGoalNotCompleted.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_map;
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        initMap();
    }

    private void initMap() {
        if (googleMap != null && runningSession != null) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(runningSession.getLocations());
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : runningSession.getLocations()) {
                builder.include(latLng);
            }
            LatLngBounds bounds = null;
            if (runningSession.getLocations().size() > 0) {
                bounds = builder.build();
            }
            CameraUpdate cameraUpdate;
            if (bounds != null) {
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,
                        runningSession.getLocations().size() == 1 ? 150 : 20);
                googleMap.moveCamera(cameraUpdate);
                googleMap.addPolyline(polylineOptions);
            }
            googleMap.setOnMapLoadedCallback(() -> googleMap.snapshot(bitmap -> {
                runningSession.setImageName(FileUtils.saveImage(bitmap));
            }));
        }
    }

    @OnClick(R.id.save)
    void onSaveClick() {
        runningSession.setName(activityName.getText().toString());
        runningSession.setDescription(activityDescription.getText().toString());
        PrefUtils.getInstance().saveRunningSession(runningSession);
        Intent intent = new Intent(getContext(), RunningActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.discard)
    void onDiscardClick() {
        Intent intent = new Intent(getContext(), RunningActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
