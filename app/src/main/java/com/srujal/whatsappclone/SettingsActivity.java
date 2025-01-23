package com.srujal.whatsappclone;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import java.io.InputStream;
import java.io.OutputStream;

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

        IMGUR_CLIENT_ID =  getString(R.string.imgur_client_iD);

        getSupportActionBar().hide();

        binding.btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        Picasso.get().load(users.getProfilePic())
                                .placeholder(R.drawable.avatar)
                                .into(binding.profileImage);
                        binding.etUsername.setText(users.getUserName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
    });

        binding.addImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        }
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
        File imageFile = new File(FileUtils.getPath(this, selectedImageUri));
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*")))
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image")
                .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
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
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
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

private String parseImageUrl(String responseBody) {
    // Parse the JSON response to extract the image URL
    // You can use a library like Gson or JSONObject
    // For simplicity, let's assume the URL is in the "link" field
    try {
        JSONObject json = new JSONObject(responseBody);
        return json.getJSONObject("data").getString("link");
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
    private static File getFileFromUri(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        File tempFile = new File(context.getCacheDir(), "tempImage.jpg");
        try (InputStream inputStream = contentResolver.openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return tempFile;
    }
}
