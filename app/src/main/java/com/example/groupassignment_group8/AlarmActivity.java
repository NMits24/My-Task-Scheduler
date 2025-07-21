package com.example.groupassignment_group8;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    private Ringtone ringtone;
    private String appPackageName;
    private double latitude = -1;
    private double longitude = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_alarm);

        TextView taskTitle = findViewById(R.id.alarm_task_title);
        TextView taskDescription = findViewById(R.id.alarm_task_description);
        Button dismissButton = findViewById(R.id.alarm_dismiss_button);
        Button openAppButton = findViewById(R.id.alarm_open_app_button);
        Button navigateButton = findViewById(R.id.alarm_navigate_button);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String soundUriString = getIntent().getStringExtra("sound_uri");
        appPackageName = getIntent().getStringExtra("app_package");
        latitude = getIntent().getDoubleExtra("latitude", -1);
        longitude = getIntent().getDoubleExtra("longitude", -1);

        taskTitle.setText(title);
        taskDescription.setText(description);

        if (appPackageName != null && !appPackageName.isEmpty()) {
            openAppButton.setVisibility(View.VISIBLE);
        } else {
            openAppButton.setVisibility(View.GONE);
        }

        if (latitude != -1 && longitude != -1) {
            navigateButton.setVisibility(View.VISIBLE);
        } else {
            navigateButton.setVisibility(View.GONE);
        }

        try {
            Uri soundUri = (soundUriString != null) ? Uri.parse(soundUriString) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(this, soundUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dismissButton.setOnClickListener(v -> {
            stopAlarm();
            finish();
        });

        openAppButton.setOnClickListener(v -> {
            stopAlarm();
            PackageManager pm = getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(appPackageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(this, "Could not open the selected app.", Toast.LENGTH_LONG).show();
            }
            finish();
        });

        navigateButton.setOnClickListener(v -> {
            stopAlarm();
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
            finish();
        });
    }

    private void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}