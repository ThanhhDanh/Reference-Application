package com.example.referenceapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.referenceapp.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password, phone, firstName, lastName;
    private AutoCompleteTextView school;
    private Button btnRegister, btnChooseAvatar;
    private ImageView avatarPreview, backRegisterBtn;
    private Uri avatarUri;

    private FirebaseAuth auth;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private List<String> schoolList = new ArrayList<>();
    private Map<String, Integer> schoolMap = new HashMap<>();

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

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        avatarUri = result.getData().getData();
                        avatarPreview.setImageURI(avatarUri);
                    }
                });

        btnChooseAvatar.setOnClickListener(v -> openFileChooser());
        btnRegister.setOnClickListener(v -> validateAndRegister());
        backRegisterBtn.setOnClickListener(v -> finish());

        loadSchoolsFromFirebase();
    }

    private void loadSchoolsFromFirebase() {
        DatabaseReference schoolRef = FirebaseDatabase.getInstance().getReference("Schools");
        schoolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                schoolList.clear();
                schoolMap.clear();
                for (DataSnapshot schoolSnapshot : snapshot.getChildren()) {
                    String name = schoolSnapshot.child("Name").getValue(String.class);
                    int id = schoolSnapshot.child("Id").getValue(Integer.class);
                    if (name != null) {
                        schoolList.add(name);
                        schoolMap.put(name, id);
                    }
                }
                setupAutoCompleteAdapter(); // Cập nhật Adapter sau khi có dữ liệu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterActivity.this, "Failed to load schools", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoCompleteAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, schoolList);
        school.setAdapter(adapter);

        // Cho phép hiển thị danh sách khi người dùng nhấn vào trường
        school.setOnClickListener(v -> school.showDropDown());

        // Xử lý khi người dùng chọn trường
        school.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSchool = (String) parent.getItemAtPosition(position);
            Integer schoolId = schoolMap.get(selectedSchool);
            Log.d("SelectedSchool", "School Name: " + selectedSchool + ", ID: " + schoolId);
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void validateAndRegister() {
        String txt_email = email.getText().toString().trim();
        String txt_password = password.getText().toString().trim();
        String txt_phone = phone.getText().toString().trim();
        String txt_firstName = firstName.getText().toString().trim();
        String txt_lastName = lastName.getText().toString().trim();
        String txt_school = school.getText().toString().trim();

        // Log các trường nhập vào
        Log.d("RegisterActivity", "Email: " + txt_email);
        Log.d("RegisterActivity", "Password: " + txt_password);
        Log.d("RegisterActivity", "Phone: " + txt_phone);
        Log.d("RegisterActivity", "First Name: " + txt_firstName);
        Log.d("RegisterActivity", "Last Name: " + txt_lastName);
        Log.d("RegisterActivity", "School: " + txt_school);

        if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) ||
                TextUtils.isEmpty(txt_phone) || TextUtils.isEmpty(txt_firstName) ||
                TextUtils.isEmpty(txt_lastName) || TextUtils.isEmpty(txt_school)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (txt_password.length() < 6) {
            Toast.makeText(this, "Password too short!", Toast.LENGTH_SHORT).show();
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
        if (avatarUri == null || avatarUri.toString().isEmpty()) {
            Log.e("AvatarUpload", "Invalid Uri");
            Toast.makeText(this, "No valid avatar selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("images");
        String fileName = System.currentTimeMillis() + "_" + getFileName(avatarUri); // Tạo tên tệp duy nhất
        StorageReference avatarRef = storageRef.child(fileName);

        Log.d("AvatarUpload", "Uploading to: " + avatarRef.getPath());

        try {
            InputStream stream = getContentResolver().openInputStream(avatarUri);
            if (stream == null) {
                Log.e("AvatarUpload", "InputStream is null");
                Toast.makeText(this, "Failed to open input stream.", Toast.LENGTH_SHORT).show();
                return;
            }

            avatarRef.putStream(stream)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("AvatarUpload", "Upload successful");
                        avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Log.d("AvatarUpload", "Download URL: " + uri.toString());
                            saveUserDataWithoutAvatar(email, phone, firstName, lastName, school, uri.toString());
                        }).addOnFailureListener(e -> {
                            Log.e("AvatarUpload", "Failed to get download URL", e);
                            Toast.makeText(this, "Failed to upload avatar.", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AvatarUpload", "Failed to upload avatar", e);
                        Toast.makeText(this, "Failed to upload avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (FileNotFoundException e) {
            Log.e("AvatarUpload", "File not found", e);
            Toast.makeText(this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserDataWithoutAvatar(String email, String phone, String firstName, String lastName, String school, String avatarUrl) {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Tạo đối tượng Users
        Users user = new Users();
        user.setAvatar(avatarUrl);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setSchoolId(Integer.parseInt(school)); // Chuyển đổi sang Integer nếu cần
        user.setRole("student");
        user.setCreatedAt(getCurrentTimestamp());
        user.setUpdatedAt(getCurrentTimestamp());

        // Lưu đối tượng Users vào Realtime Database
        reference.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to save user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức để lấy tên tệp từ Uri
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getCurrentTimestamp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        }
        return null;
    }
}