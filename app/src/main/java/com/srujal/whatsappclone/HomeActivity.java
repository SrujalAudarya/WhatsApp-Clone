package com.srujal.whatsappclone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.srujal.whatsappclone.Fragments.CallsFragment;
import com.srujal.whatsappclone.Fragments.ChatsFragment;
import com.srujal.whatsappclone.Fragments.StatusFragment;
import com.srujal.whatsappclone.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    ActivityHomeBinding binding;
    BottomNavigationView bottom_nav;
    boolean doubletab = false;
    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(HomeActivity.this, googleSignInOptions);

        bottom_nav = findViewById(R.id.home_nav);
        bottom_nav.setOnNavigationItemSelectedListener(HomeActivity.this);
        bottom_nav.setSelectedItemId(R.id.chat);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (auth.getCurrentUser() != null) {
//            String userId = auth.getUid();
//            DatabaseReference userStatusRef = database.getReference().child("Users").child(userId).child("status");
//
//            // Set user's last seen
//            userStatusRef.setValue(String.valueOf(System.currentTimeMillis()));
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (auth.getCurrentUser() != null) {
//            String userId = auth.getUid();
//            DatabaseReference userStatusRef = database.getReference().child("Users").child(userId).child("status");
//
//            // Set user as "Online"
//            userStatusRef.setValue("Online");
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.setting) {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.logout) {
            signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AlertDialog.Builder ad = new AlertDialog.Builder(HomeActivity.this);
        ad.setTitle("Sign Out");
        ad.setIcon(R.drawable.logout);
        ad.setMessage("Are you Sure you want To Sign Out");
        ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ad.setNegativeButton("Sign Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                auth.signOut();
                LoginManager.getInstance().logOut();
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
            }
        }).create().show();
    }

    ChatsFragment chatsFragment = new ChatsFragment();
    StatusFragment statusFragment = new StatusFragment();
    CallsFragment callsFragment = new CallsFragment();
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.chat) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment, chatsFragment).commit();
            setTitle("WhatsApp");
        }
        if (item.getItemId() == R.id.status) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment, statusFragment).commit();
            setTitle("Status");
        }
        if (item.getItemId() == R.id.call) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment, callsFragment).commit();
            setTitle("Calls");
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (doubletab) {
            // This ensures the app is closed completely
            finishAffinity(); // Closes all activities in the stack
            super.onBackPressed();
        } else {
            doubletab = true;
            Toast.makeText(HomeActivity.this, "Press again to exit app", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubletab = false, 2000); // Reset doubletap after 2 seconds
        }
    }
}