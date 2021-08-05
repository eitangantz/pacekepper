package com.pacekeeper.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.pacekeeper.R;
import com.pacekeeper.utils.PrefUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {
    @BindView(R.id.switch_pause)
    SwitchCompat switchCompat;
    @BindView(R.id.text_measurements)
    TextView textMeasurements;
    @BindView(R.id.text_audio)
    TextView textAudio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchCompat.setChecked(PrefUtils.getInstance().getAutoPause());
        textMeasurements.setText(PrefUtils.getInstance().getMeasurement());
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefUtils.getInstance().setAutoPause(isChecked);
            }
        });
    }

    @OnClick(R.id.layout_measurements)
    void choseMeasurements() {
        String[] options = {"Miles", "Kilometers"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a measurement");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefUtils.getInstance().setMeasurement(options[which]);
                textMeasurements.setText(options[which]);
            }
        });
        builder.show();

    }

    @OnClick(R.id.layot_audio)
    void audio() {
        String[] audios = {"Male voice", "Female voice"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a audio");
        builder.setItems(audios, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PrefUtils.getInstance().setAudioCue(audios[which]);
                textAudio.setText(audios[which]);
            }
        });
        builder.show();
    }

    @OnClick(R.id.btn_back)
    void back() {
        onBackPressed();
    }


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_settings;
    }
}
