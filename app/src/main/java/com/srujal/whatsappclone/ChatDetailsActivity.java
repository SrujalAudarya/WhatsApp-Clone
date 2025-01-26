package com.srujal.whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji2.bundled.BundledEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.srujal.whatsappclone.Adapters.ChatAdapter;
import com.srujal.whatsappclone.Models.Messages;
import com.srujal.whatsappclone.databinding.ActivityChatDetailsBinding;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailsActivity extends AppCompatActivity {

    ActivityChatDetailsBinding binding;

    FirebaseDatabase database;
    FirebaseAuth auth;
    String userName, reciverId, profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final String senderId = auth.getUid();
        reciverId = getIntent().getStringExtra("userid");
        userName = getIntent().getStringExtra("userName");
        profilePic = getIntent().getStringExtra("profileImg");

        binding.tvUsername.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        // Initialize EmojiCompat
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);

        // Initialize EmojiPopup
        binding.emojiKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatDetailsActivity.this, "work in progress...", Toast.LENGTH_SHORT).show();
            }
        });

//        // Observe receiver's status
//        DatabaseReference userStatusRef = database.getReference().child("Users").child(reciverId).child("status");
//        userStatusRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    String status = snapshot.getValue(String.class);
//
//                    if ("Online".equalsIgnoreCase(status)) {
//                        binding.tvUserStatus.setText("Online");
//                    } else {
//                        long lastSeen = Long.parseLong(status);
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
//                        String formattedTime = "Last Seen: " + sdf.format(new Date(lastSeen));
//                        binding.tvUserStatus.setText(formattedTime);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Handle potential errors
//            }
//        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        final ArrayList<Messages> messagesArrayList = new ArrayList<>();

        final ChatAdapter chatAdapter = new ChatAdapter(messagesArrayList, this, reciverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(linearLayoutManager);

        final String senderRoom = senderId + reciverId;
        final String receiverRoom = reciverId + senderId;

        // Load previous messages only once
        database.getReference().child("Chats").child(senderRoom)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            assert messages != null;
                            messages.setMessageId(dataSnapshot.getKey());
                                messagesArrayList.add(messages);
                        }
                        chatAdapter.notifyDataSetChanged(); // Notify adapter after loading data
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors
                    }
                });

        // Send message logic
        binding.btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.etMessage.getText().toString().trim();
                if (message.isEmpty()) {
                    return;
                }

                final Messages messages = new Messages(senderId, message);
                messages.setTimeStamp(new Date().getTime());
                binding.etMessage.setText("");

                // Add the message to sender and receiver rooms
                database.getReference().child("Chats").child(senderRoom)
                        .push()
                        .setValue(messages)
                        .addOnSuccessListener(aVoid -> {
                            database.getReference().child("Chats").child(receiverRoom)
                                    .push()
                                    .setValue(messages)
                                    .addOnSuccessListener(aVoid1 -> {
                                        messagesArrayList.add(messages);
                                        chatAdapter.notifyItemInserted(messagesArrayList.size() - 1);

                                        // Update last message
                                        database.getReference().child("Users").child(senderId).child("lastMessage")
                                                .setValue(message);
                                        database.getReference().child("Users").child(reciverId).child("lastMessage")
                                                .setValue(message);
                                    });
                        });
            }
        });
    }
}
