package com.example.groupassignment_group8;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log; // Make sure this import is here
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.groupassignment_group8.MainPart;
import com.example.groupassignment_group8.UserSyncCallback;
import com.example.groupassignment_group8.User;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity"; // Your TAG for logging
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private MainPart mainPartHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mainPartHelper = new MainPart();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        Button buttonLogin = findViewById(R.id.button_login);
        TextView textViewRegister = findViewById(R.id.text_view_register);
        TextView textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);

        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        textViewForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting email/password login for: " + email); // ADDED LOG

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithEmailAndPassword onComplete called."); // ADDED LOG
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmailAndPassword SUCCESS."); // ADDED LOG
                        FirebaseUser user = mAuth.getCurrentUser();
                        handleUserPostAuth(user);
                    } else {
                        Log.e(TAG, "signInWithEmailAndPassword FAILED: " + task.getException().getMessage()); // ADDED LOG
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Enter your email address");
        builder.setView(emailInput);

        builder.setPositiveButton("Send Reset Email", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(LoginActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void signInWithGoogle() {
        Log.d(TAG, "Initiating Google Sign-In intent."); // ADDED LOG
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called. Request Code: " + requestCode + ", Result Code: " + resultCode); // ADDED LOG

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.d(TAG, "GoogleSignIn.getSignedInAccountFromIntent successful."); // ADDED LOG
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed (ApiException): " + e.getStatusCode() + " - " + e.getMessage(), e); // More detailed log
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Consider calling handleUserPostAuth(null) here to explicitly indicate failure
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Authenticating with Firebase using Google credential."); // ADDED LOG
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential onComplete called."); // ADDED LOG
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential SUCCESS."); // ADDED LOG
                        FirebaseUser user = mAuth.getCurrentUser();
                        handleUserPostAuth(user);
                    } else {
                        Log.e(TAG, "signInWithCredential FAILED: " + task.getException().getMessage()); // ADDED LOG
                        Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, "onStart: User already logged in. UID: " + currentUser.getUid()); // ADDED LOG
            handleUserPostAuth(currentUser);
        } else {
            Log.d(TAG, "onStart: No user currently logged in."); // ADDED LOG
        }
    }

    private void handleUserPostAuth(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "handleUserPostAuth called. User authenticated. Syncing Firestore data for UID: " + user.getUid()); // ADDED LOG
            Toast.makeText(this, "Authentication successful, syncing user data...", Toast.LENGTH_SHORT).show();

            mainPartHelper.fetchOrCreateUserData(user.getUid(), new UserSyncCallback() {
                @Override
                public void onUserSyncSuccess(User userProfile) {
                    Log.d(TAG, "Firestore data sync SUCCESS for " + user.getUid() + ": " + userProfile.getUsername()); // ADDED LOG
                    Toast.makeText(LoginActivity.this, "Welcome, " + userProfile.getUsername() + "!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                }

                @Override
                public void onUserSyncFailure(String errorMessage) {
                    Log.e(TAG, "Firestore data sync FAILED for " + user.getUid() + ": " + errorMessage); // ADDED LOG
                    Toast.makeText(LoginActivity.this, "Error syncing user data: " + errorMessage + ". Proceeding anyway.", Toast.LENGTH_LONG).show();
                    navigateToMainActivity();
                }
            });
        } else {
            Log.w(TAG, "handleUserPostAuth called with null user. Authentication likely failed."); // ADDED LOG
            Toast.makeText(this, "Could not get authenticated user. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainActivity() {
        Log.d(TAG, "Navigating to MainActivity."); // ADDED LOG
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}