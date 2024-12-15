package com.example.referenceapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.referenceapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password, phone, firstName, lastName, school;
    private Button btnRegister, btnChooseAvatar;
    private ImageView avatarPreview, backRegisterBtn;
    private Uri avatarUri;

    private FirebaseAuth auth;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        school = findViewById(R.id.school);
        btnRegister = findViewById(R.id.register);
        btnChooseAvatar = findViewById(R.id.chooseAvatar);
        avatarPreview = findViewById(R.id.avatarPreview);
        backRegisterBtn = findViewById(R.id.backRegisterBtn);

        auth = FirebaseAuth.getInstance();

        // Đăng ký ActivityResultLauncher
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        avatarUri = result.getData().getData();
                        avatarPreview.setImageURI(avatarUri); // Hiển thị ảnh xem trước
                    }
                });

        btnChooseAvatar.setOnClickListener(v -> openFileChooser());
        btnRegister.setOnClickListener(v -> validateAndRegister());
        setVariable();
    }

    private void setVariable() {
        backRegisterBtn.setOnClickListener(v -> finish());
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void validateAndRegister() {
        String txt_email = email.getText().toString();
        String txt_password = password.getText().toString();
        String txt_phone = phone.getText().toString();
        String txt_firstName = firstName.getText().toString();
        String txt_lastName = lastName.getText().toString();
        String txt_school = school.getText().toString();

        if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) ||
                TextUtils.isEmpty(txt_phone) || TextUtils.isEmpty(txt_firstName) ||
                TextUtils.isEmpty(txt_lastName) || TextUtils.isEmpty(txt_school)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (txt_password.length() < 6) {
            Toast.makeText(this, "Password too short !!!", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(txt_email, txt_password, txt_phone, txt_firstName, txt_lastName, txt_school);
        }
    }

    private void registerUser(String email, String password, String phone, String firstName, String lastName, String school) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (avatarUri != null) {
                    uploadAvatarAndSaveUserData(email, phone, firstName, lastName, school);
                } else {
                    saveUserDataWithoutAvatar(email, phone, firstName, lastName, school, "");
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatarAndSaveUserData(String email, String phone, String firstName, String lastName, String school) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "avatars/" + System.currentTimeMillis() + "_" + avatarUri.getLastPathSegment();
        StorageReference avatarRef = storageRef.child(fileName);

        avatarRef.putFile(avatarUri).addOnSuccessListener(taskSnapshot ->
                avatarRef.getDownloadUrl().addOnSuccessListener(uri ->
                        saveUserDataWithoutAvatar(email, phone, firstName, lastName, school, uri.toString())
                )
        ).addOnFailureListener(e ->
                Toast.makeText(RegisterActivity.this, "Failed to upload avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void saveUserDataWithoutAvatar(String email, String phone, String firstName, String lastName, String school, String avatarUrl) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("fullName", firstName + " " + lastName);
        userMap.put("school", school);
        userMap.put("avatarUrl", avatarUrl);
        userMap.put("role", "student");
        userMap.put("createdAt", System.currentTimeMillis());

        reference.setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to save user info", Toast.LENGTH_SHORT).show();
            }
        });
    }
}