package com.example.groupassignment_group8;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_TASK_REQUEST = 1;
    public static final int EDIT_TASK_REQUEST = 2;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    private TaskViewModel taskViewModel;
    private TaskListAdapter adapter;
    private List<ListItem> fullTaskList = new ArrayList<>();

    // Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up the listener for authentication state changes
        setupAuthListener();

        // Ask for necessary permissions
        askNotificationPermission();
        checkOverlayPermission();
        checkExactAlarmPermission();

        FloatingActionButton buttonAddTask = findViewById(R.id.fab);
        buttonAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivityForResult(intent, ADD_TASK_REQUEST);
        });

        RecyclerView recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new TaskListAdapter();
        recyclerView.setAdapter(adapter);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getTasksWithDateHeaders().observe(this, listItems -> {
            fullTaskList = listItems;
            adapter.submitList(fullTaskList);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            taskViewModel.delete(adapter.getTaskAt(viewHolder.getAdapterPosition()));
                            Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        })
                        .setOnCancelListener(dialog -> {
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                ColorDrawable background = new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.priority_high));

                if (dX < 0) { // Swiping to the left
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();

                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else { // view is unSwiped
                    background.setBounds(0, 0, 0, 0);
                }

                background.draw(c);
                if (dX < 0) icon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(task -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            intent.putExtra(AddEditTaskActivity.EXTRA_FIRESTORE_ID, task.getFirestoreId());
            intent.putExtra(AddEditTaskActivity.EXTRA_TITLE, task.getTitle());
            intent.putExtra(AddEditTaskActivity.EXTRA_DESCRIPTION, task.getDescription());
            intent.putExtra(AddEditTaskActivity.EXTRA_PRIORITY, task.getPriority());
            intent.putExtra(AddEditTaskActivity.EXTRA_REMINDER_TIME, task.getReminderTime());
            intent.putExtra(AddEditTaskActivity.EXTRA_SOUND_URI, task.getSoundUri());
            intent.putExtra(AddEditTaskActivity.EXTRA_APP_PACKAGE, task.getAppPackageName());
            intent.putExtra(AddEditTaskActivity.EXTRA_LOCATION_NAME, task.getLocationName());
            intent.putExtra(AddEditTaskActivity.EXTRA_LATITUDE, task.getLatitude());
            intent.putExtra(AddEditTaskActivity.EXTRA_LONGITUDE, task.getLongitude());
            startActivityForResult(intent, EDIT_TASK_REQUEST);
        });

        adapter.setOnTaskCheckedChangeListener((task, isChecked) -> {
            task.setCompleted(isChecked);
            int position = -1;
            List<ListItem> currentItems = adapter.getCurrentList();
            for (int i = 0; i < currentItems.size(); i++) {
                ListItem item = currentItems.get(i);
                if (item.getType() == ListItem.TYPE_TASK && ((TaskItem) item).getTask().getFirestoreId().equals(task.getFirestoreId())) {
                    position = i;
                    break;
                }
            }
            if (position != -1) {
                adapter.notifyItemChanged(position);
            }
            taskViewModel.update(task);
        });
    }

    private void setupAuthListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // User is signed out, navigate to LoginActivity
                Log.d("MainActivity", "AuthStateListener: User is null, redirecting to LoginActivity.");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Prevent user from coming back to MainActivity via back button
            } else {
                // User is signed in
                Log.d("MainActivity", "AuthStateListener: User is signed in: " + user.getUid());
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add the listener when the Activity starts
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove the listener when the Activity stops to avoid memory leaks
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This app needs permission to display alarms over other apps.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("This app needs permission to schedule exact alarms.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Overlay permission not granted. Full-screen alarms may not work.", Toast.LENGTH_LONG).show();
                }
            }
        } else if (resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra(AddEditTaskActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditTaskActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(AddEditTaskActivity.EXTRA_PRIORITY, 1);
            long reminderTime = data.getLongExtra(AddEditTaskActivity.EXTRA_REMINDER_TIME, 0);
            String soundUri = data.getStringExtra(AddEditTaskActivity.EXTRA_SOUND_URI);
            String appPackageName = data.getStringExtra(AddEditTaskActivity.EXTRA_APP_PACKAGE);
            String locationName = data.getStringExtra(AddEditTaskActivity.EXTRA_LOCATION_NAME);
            double latitude = data.getDoubleExtra(AddEditTaskActivity.EXTRA_LATITUDE, -1);
            double longitude = data.getDoubleExtra(AddEditTaskActivity.EXTRA_LONGITUDE, -1);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = (currentUser != null) ? currentUser.getUid() : "unknown_user";

            if (requestCode == ADD_TASK_REQUEST) {
                Task task = new Task(title, description, false, priority, userId, reminderTime, soundUri, appPackageName, locationName, latitude, longitude);
                taskViewModel.insert(task);
                Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
            } else if (requestCode == EDIT_TASK_REQUEST) {
                String firestoreId = data.getStringExtra(AddEditTaskActivity.EXTRA_FIRESTORE_ID);
                if (firestoreId == null) {
                    Toast.makeText(this, "Task can't be updated", Toast.LENGTH_SHORT).show();
                    return;
                }
                Task task = new Task(title, description, false, priority, userId, reminderTime, soundUri, appPackageName, locationName, latitude, longitude);
                task.setFirestoreId(firestoreId);
                taskViewModel.update(task);
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Task not saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        return true;
    }

    private void filter(String text) {
        List<ListItem> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            adapter.submitList(fullTaskList);
        } else {
            for (ListItem item : fullTaskList) {
                if (item.getType() == ListItem.TYPE_TASK) {
                    Task task = ((TaskItem) item).getTask();
                    if (task.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                            task.getDescription().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            }
            adapter.submitList(filteredList);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            // The AuthStateListener will automatically navigate to LoginActivity
            return true;
        } else if (itemId == R.id.filter_by_date) {
            taskViewModel.getTasksWithDateHeaders().observe(this, adapter::submitList);
            return true;
        } else if (itemId == R.id.filter_by_priority) {
            taskViewModel.getTasksWithPriorityHeaders().observe(this, adapter::submitList);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
