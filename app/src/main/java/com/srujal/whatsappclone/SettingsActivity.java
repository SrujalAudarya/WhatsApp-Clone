package com.srujal.whatsappclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.srujal.whatsappclone.Models.Users;
import com.srujal.whatsappclone.databinding.ActivitySettingsBinding;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    private static String IMGUR_CLIENT_ID;
    private static final int IMAGE_PICK_CODE = 15;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        IMGUR_CLIENT_ID = getString(R.string.imgur_client_iD);

        getSupportActionBar().hide();

        // Back button action
        binding.btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Save button action
        binding.btnSave.setOnClickListener(view -> {
            String about = binding.etAbout.getText().toString();
            String username = binding.etUsername.getText().toString();

            HashMap<String, Object> objectHashMap = new HashMap<>();
            objectHashMap.put("about", about);
            objectHashMap.put("userName", username);

            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                    .updateChildren(objectHashMap);

            Toast.makeText(SettingsActivity.this, "Data Successfully Changed", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Load existing user data
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        Picasso.get().load(users.getProfilePic())
                                .placeholder(R.drawable.avatar)
                                .into(binding.profileImage);
                        binding.etUsername.setText(users.getUserName());
                        binding.etAbout.setText(users.getAbout());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Add image button action
        binding.addImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            binding.profileImage.setImageURI(selectedImageUri);
            uploadImageToImgur();
        }
    }

    private void uploadImageToImgur() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = compressImage(selectedImageUri);
            if (imageFile == null) {
                Toast.makeText(this, "Error compressing image", Toast.LENGTH_SHORT).show();
                return;
            }

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getName(),
                            RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        String imageUrl = parseImageUrl(responseBody);

                        runOnUiThread(() -> {
                            Toast.makeText(SettingsActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            updateProfileImage(imageUrl);
                        });
                    } else {
                        Log.e("ImgurUpload", "Error: " + response.code() + ", Response: " + response.body().string());
                        runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileImage(String imageUrl) {
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .child("profilePic").setValue(imageUrl);
        Picasso.get().load(imageUrl).into(binding.profileImage);
    }

    private File compressImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            File tempFile = new File(getCacheDir(), "compressedImage.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            // Compress the image
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

            outputStream.flush();
            outputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseImageUrl(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONObject("data").getString("link");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
