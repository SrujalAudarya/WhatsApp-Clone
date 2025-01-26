package com.srujal.whatsappclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.srujal.whatsappclone.Models.Users;
import com.srujal.whatsappclone.databinding.ActivityLoginBinding;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog dialog;
    private boolean isPasswordVisible = false;
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); // Hides the ActionBar for this activity
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Google Sign-In Setup
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);

        // Initialize ProgressDialog
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setTitle("Creating Account");
        dialog.setMessage("We're creating your account...");
        dialog.setCancelable(false);

        callbackManager = CallbackManager.Factory.create();

        // Eye icon to toggle password visibility
        binding.eyeCheck.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.eyeCheck.setImageResource(R.drawable.eye_icon); // Set the eye icon to 'closed'
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                isPasswordVisible = false; // Update the state
            } else {
                binding.eyeCheck.setImageResource(R.drawable.disable_eye); // Set the eye icon to 'open'
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                isPasswordVisible = true; // Update the state
            }
        });


        // Google Sign-In button click listener
        binding.btnGoogle.setOnClickListener(view -> signIn());

        // Facebook Login button click listener
        binding.btnFaceBook.setOnClickListener(view -> {
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
        });

        // Register the Facebook login callback
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Facebook login canceled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, "Facebook login failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Sign up button click listener
        binding.btnSignUp.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString().trim().toLowerCase();
            String password = binding.etPassword.getText().toString().trim();
            String username = binding.etName.getText().toString().trim();

            // Input validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Username, Email, and Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(LoginActivity.this, "Password must contain at least one special character, one uppercase letter, one lowercase letter, one digit, and minimum length of 8 characters", Toast.LENGTH_LONG).show();
                return;
            }

            // Show progress dialog
            dialog.show();

            // Create a new user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        // Hide progress dialog
                        dialog.dismiss();

                        if (task.isSuccessful()) {
                            // User creation successful, store additional user data in Firebase Database
                            String userId = task.getResult().getUser().getUid();
                            Users user = new Users(username, email, password);

                            database.getReference().child("Users").child(userId).setValue(user)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(LoginActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // User creation failed
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        binding.tvSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Already Signed in, then auto-redirect to home screen
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Something Went Wrong...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signIn() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();

                Users users = new Users();
                users.setUserId(user.getUid());
                users.setEmail(user.getEmail());
                users.setUserName(user.getDisplayName());
                users.setProfilePic(user.getPhotoUrl().toString());

                database.getReference().child("Users").child(user.getUid()).setValue(users);

                Toast.makeText(LoginActivity.this, "Sign In with Google", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        saveUserToDatabase(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Facebook authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            String name = user.getDisplayName();
            String email = user.getEmail();
            String profilePic = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

            Users newUser = new Users(name, email, profilePic);

            database.getReference().child("Users").child(userId)
                    .setValue(newUser)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Facebook login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";
        return password.matches(passwordPattern);
    }
}
