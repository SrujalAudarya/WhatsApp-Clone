package com.srujal.whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.srujal.whatsappclone.databinding.ActivityChatDetailsBinding;

public class ChatDetailsActivity extends AppCompatActivity {

    ActivityChatDetailsBinding binding;

    FirebaseDatabase database;
    FirebaseAuth auth;
    String senderId, userName, reciverId, profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        senderId = auth.getUid();
        reciverId = getIntent().getStringExtra("userid");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profileImg");

        binding.tvUsername.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}