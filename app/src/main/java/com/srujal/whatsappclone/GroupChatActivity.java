package com.srujal.whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.srujal.whatsappclone.Adapters.GroupChatAdapter;
import com.srujal.whatsappclone.Models.GroupChatModel;
import com.srujal.whatsappclone.databinding.ActivityGroupChatBinding;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    private ActivityGroupChatBinding binding;
    private ArrayList<GroupChatModel> groupChatMessages;
    private FirebaseDatabase database;
    private GroupChatAdapter adapter;
    private String senderId, senderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize variables
        database = FirebaseDatabase.getInstance();
        groupChatMessages = new ArrayList<>();
        senderId = FirebaseAuth.getInstance().getUid();

        binding.tvUsername.setText("Friends Chats");

        // Fetch sender name from the database
        database.getReference().child("Users").child(senderId).child("userName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        senderName = snapshot.getValue(String.class);
                        if (senderName == null) {
                            senderName = "Unknown User";
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(GroupChatActivity.this, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set up RecyclerView adapter
        adapter = new GroupChatAdapter(groupChatMessages, this);
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(linearLayoutManager);

        // Load messages from the database
        database.getReference().child("Group Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatMessages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    GroupChatModel msg = snapshot1.getValue(GroupChatModel.class);
                    groupChatMessages.add(msg);
                }
                adapter.notifyDataSetChanged();
                binding.chatRecyclerView.scrollToPosition(groupChatMessages.size() - 1); // Auto-scroll
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });

        // Send message on button click
        binding.btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    GroupChatModel msg = new GroupChatModel(senderId, message, senderName);
                    msg.setTimeStamp(new Date().getTime());
                    database.getReference().child("Group Chat").push().setValue(msg);

                    // Clear input and hide keyboard
                    binding.etMessage.setText("");
                    hideKeyboard();
                } else {
                    Toast.makeText(GroupChatActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
