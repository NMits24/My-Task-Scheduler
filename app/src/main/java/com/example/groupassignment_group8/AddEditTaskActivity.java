package com.example.groupassignment_group8;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.libraries.places.api.Places;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {
    public static final String EXTRA_FIRESTORE_ID = "com.example.groupassignment_group8.EXTRA_FIRESTORE_ID";
    public static final String EXTRA_TITLE = "com.example.groupassignment_group8.EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION = "com.example.groupassignment_group8.EXTRA_DESCRIPTION";
    public static final String EXTRA_PRIORITY = "com.example.groupassignment_group8.EXTRA_PRIORITY";
    public static final String EXTRA_REMINDER_TIME = "com.example.groupassignment_group8.EXTRA_REMINDER_TIME";
    public static final String EXTRA_SOUND_URI = "com.example.groupassignment_group8.EXTRA_SOUND_URI";
    public static final String EXTRA_APP_PACKAGE = "com.example.groupassignment_group8.EXTRA_APP_PACKAGE";
    public static final String EXTRA_LOCATION_NAME = "com.example.groupassignment_group8.EXTRA_LOCATION_NAME";
    public static final String EXTRA_LATITUDE = "com.example.groupassignment_group8.EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE = "com.example.groupassignment_group8.EXTRA_LONGITUDE";

    private EditText editTextTitle;
    private EditText editTextDescription;
    private Spinner spinnerPriority;
    private Button buttonSetReminder, buttonSelectSound, buttonSelectApp, buttonSetLocation;
    private ImageButton clearReminderButton, clearSoundButton, clearAppButton, clearLocationButton;

    private Calendar reminderCalendar;
    private Uri selectedRingtoneUri;
    private String selectedAppPackageName;
    private String selectedLocationName;
    private double selectedLatitude = -1;
    private double selectedLongitude = -1;
    private boolean isReminderSet = false;

    // --- MODIFICATION START ---
    // Launcher for the new MapPickerActivity
    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLatitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, -1);
                    selectedLongitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, -1);
                    selectedLocationName = data.getStringExtra(MapPickerActivity.EXTRA_ADDRESS);
                    updateLocationButtonText();
                }
            });
    // --- MODIFICATION END ---

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)) {
                        selectedRingtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        updateSoundButtonText();
                    } else if (data.getComponent() != null) {
                        selectedAppPackageName = data.getComponent().getPackageName();
                        updateAppButtonText();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        spinnerPriority = findViewById(R.id.spinner_priority);
        buttonSetReminder = findViewById(R.id.button_set_reminder);
        buttonSelectSound = findViewById(R.id.button_select_sound);
        buttonSelectApp = findViewById(R.id.button_select_app);
        buttonSetLocation = findViewById(R.id.button_set_location);
        clearReminderButton = findViewById(R.id.button_clear_reminder);
        clearSoundButton = findViewById(R.id.button_clear_sound);
        clearAppButton = findViewById(R.id.button_clear_app);
        clearLocationButton = findViewById(R.id.button_clear_location);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Low Priority", "Medium Priority", "High Priority"});
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        reminderCalendar = Calendar.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FIRESTORE_ID)) {
            if (actionBar != null) actionBar.setTitle("Edit Task");
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
            spinnerPriority.setSelection(intent.getIntExtra(EXTRA_PRIORITY, 1) - 1);

            long reminderTime = intent.getLongExtra(EXTRA_REMINDER_TIME, 0);
            if (reminderTime > 0) {
                isReminderSet = true;
                reminderCalendar.setTimeInMillis(reminderTime);
                updateReminderButtonText();
            }

            String soundUriString = intent.getStringExtra(EXTRA_SOUND_URI);
            if (soundUriString != null && !soundUriString.isEmpty()) {
                selectedRingtoneUri = Uri.parse(soundUriString);
                updateSoundButtonText();
            }

            selectedAppPackageName = intent.getStringExtra(EXTRA_APP_PACKAGE);
            if (selectedAppPackageName != null && !selectedAppPackageName.isEmpty()) {
                updateAppButtonText();
            }

            selectedLocationName = intent.getStringExtra(EXTRA_LOCATION_NAME);
            selectedLatitude = intent.getDoubleExtra(EXTRA_LATITUDE, -1);
            selectedLongitude = intent.getDoubleExtra(EXTRA_LONGITUDE, -1);
            if (selectedLocationName != null && !selectedLocationName.isEmpty()) {
                updateLocationButtonText();
            }

        } else {
            if (actionBar != null) actionBar.setTitle("Add Task");
        }

        buttonSetReminder.setOnClickListener(v -> showDateTimePicker());
        buttonSelectSound.setOnClickListener(v -> openRingtonePicker());
        buttonSelectApp.setOnClickListener(v -> openAppPicker());
        buttonSetLocation.setOnClickListener(v -> openLocationPicker());

        clearReminderButton.setOnClickListener(v -> clearReminder());
        clearSoundButton.setOnClickListener(v -> clearSound());
        clearAppButton.setOnClickListener(v -> clearApp());
        clearLocationButton.setOnClickListener(v -> clearLocation());
    }

    private void openAppPicker() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        Intent pickerIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickerIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
        activityResultLauncher.launch(pickerIntent);
    }

    private void openRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
        activityResultLauncher.launch(intent);
    }

    // --- MODIFICATION START ---
    // This method now launches the MapPickerActivity
    private void openLocationPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        // Pass the current location to the map picker if it exists
        if (selectedLatitude != -1 && selectedLongitude != -1) {
            intent.putExtra(MapPickerActivity.EXTRA_LATITUDE, selectedLatitude);
            intent.putExtra(MapPickerActivity.EXTRA_LONGITUDE, selectedLongitude);
        }
        mapPickerLauncher.launch(intent);
    }
    // --- MODIFICATION END ---

    private void saveTask() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        int priority = spinnerPriority.getSelectedItemPosition() + 1;

        if (title.trim().isEmpty()) {
            Toast.makeText(this, "Please insert a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isReminderSet && reminderCalendar.getTimeInMillis() > System.currentTimeMillis()) {
            scheduleNotification(title, description);
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_DESCRIPTION, description);
        data.putExtra(EXTRA_PRIORITY, priority);
        data.putExtra(EXTRA_REMINDER_TIME, isReminderSet ? reminderCalendar.getTimeInMillis() : 0L);
        if (selectedRingtoneUri != null) {
            data.putExtra(EXTRA_SOUND_URI, selectedRingtoneUri.toString());
        }
        if (selectedAppPackageName != null) {
            data.putExtra(EXTRA_APP_PACKAGE, selectedAppPackageName);
        }
        if (selectedLocationName != null) {
            data.putExtra(EXTRA_LOCATION_NAME, selectedLocationName);
        }
        data.putExtra(EXTRA_LATITUDE, selectedLatitude);
        data.putExtra(EXTRA_LONGITUDE, selectedLongitude);

        String firestoreId = getIntent().getStringExtra(EXTRA_FIRESTORE_ID);
        if (firestoreId != null) {
            data.putExtra(EXTRA_FIRESTORE_ID, firestoreId);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    private void clearReminder() {
        isReminderSet = false;
        reminderCalendar.setTimeInMillis(0);
        buttonSetReminder.setText("Set Reminder");
        clearReminderButton.setVisibility(View.GONE);
    }

    private void clearSound() {
        selectedRingtoneUri = null;
        buttonSelectSound.setText("Select Sound");
        clearSoundButton.setVisibility(View.GONE);
    }

    private void clearApp() {
        selectedAppPackageName = null;
        buttonSelectApp.setText("Select App to Launch");
        clearAppButton.setVisibility(View.GONE);
    }

    private void clearLocation() {
        selectedLocationName = null;
        selectedLatitude = -1;
        selectedLongitude = -1;
        buttonSetLocation.setText("Set Location");
        clearLocationButton.setVisibility(View.GONE);
    }

    private void updateAppButtonText() {
        if (selectedAppPackageName != null && !selectedAppPackageName.isEmpty()) {
            try {
                PackageManager pm = getPackageManager();
                String appName = pm.getApplicationLabel(pm.getApplicationInfo(selectedAppPackageName, 0)).toString();
                buttonSelectApp.setText(String.format("App: %s", appName));
                clearAppButton.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                buttonSelectApp.setText("App Selected");
                clearAppButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            reminderCalendar.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderCalendar.set(Calendar.MINUTE, minute);
                isReminderSet = true;
                updateReminderButtonText();
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void updateReminderButtonText() {
        if (isReminderSet) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            buttonSetReminder.setText(String.format("Reminder: %s", sdf.format(reminderCalendar.getTime())));
            clearReminderButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateSoundButtonText() {
        if (selectedRingtoneUri != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
            String title = ringtone.getTitle(this);
            buttonSelectSound.setText(String.format("Sound: %s", title));
            clearSoundButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateLocationButtonText() {
        if (selectedLocationName != null && !selectedLocationName.isEmpty()) {
            buttonSetLocation.setText(String.format("Location: %s", selectedLocationName));
            clearLocationButton.setVisibility(View.VISIBLE);
        }
    }

    private void scheduleNotification(String title, String description) {
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        if (selectedRingtoneUri != null) {
            intent.putExtra("sound_uri", selectedRingtoneUri.toString());
        }
        if (selectedAppPackageName != null) {
            intent.putExtra("app_package", selectedAppPackageName);
        }
        intent.putExtra("latitude", selectedLatitude);
        intent.putExtra("longitude", selectedLongitude);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_edit_task_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save_task) {
            saveTask();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
