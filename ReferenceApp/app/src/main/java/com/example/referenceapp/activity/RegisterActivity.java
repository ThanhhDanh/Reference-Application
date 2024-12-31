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
import com.google.gson.Gson;

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

    private EditText email, password, phone, firstName, lastName, member;
    private AutoCompleteTextView school;
    private Button btnRegister;
    private ImageView backRegisterBtn;
    private FirebaseAuth auth;

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
        member = findViewById(R.id.member);
        btnRegister = findViewById(R.id.register);
        backRegisterBtn = findViewById(R.id.backRegisterBtn);

        auth = FirebaseAuth.getInstance();

        //btnChooseAvatar.setOnClickListener(v -> openFileChooser());
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
                    int id = Integer.parseInt(Objects.requireNonNull(schoolSnapshot.getKey()));
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

//    private void openFileChooser() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        activityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
//    }

    private void validateAndRegister() {
        String txt_email = email.getText().toString().trim();
        String txt_password = password.getText().toString().trim();
        String txt_phone = phone.getText().toString().trim();
        String txt_firstName = firstName.getText().toString().trim();
        String txt_lastName = lastName.getText().toString().trim();
        String txt_school = school.getText().toString().trim();
        String txt_member = member.getText().toString().trim();


        if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) ||
                TextUtils.isEmpty(txt_phone) || TextUtils.isEmpty(txt_firstName) ||
                TextUtils.isEmpty(txt_lastName) || TextUtils.isEmpty(txt_school)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (txt_password.length() < 6) {
            Toast.makeText(this, "Password too short!", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(txt_email, txt_password, txt_phone, txt_firstName, txt_lastName, txt_school, txt_member);
        }
    }

    private void registerUser(String email, String password, String phone, String firstName, String lastName, String school, String member) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("RegisterActivity", "User data saved successfully");
                saveUserData(email, phone, firstName, lastName, school, member);
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("RegisterActivity", "Failed to save user data", task.getException());
            }
        });
    }

    private void saveUserData(String email, String phone, String firstName, String lastName, String school, String member) {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Lấy ID trường học từ schoolMap
        Integer schoolId = schoolMap.get(school);
        if (schoolId == null) {
            Toast.makeText(this, "Invalid school selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Users
        Users user = new Users();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setSchoolId(schoolId);
        user.setRole(member);
        user.setCreatedAt(getCurrentTimestamp());
        user.setUpdatedAt(getCurrentTimestamp());

        Log.d("RegisterActivity", "User data: " + new Gson().toJson(user));

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
//    @SuppressLint("Range")
//    private String getFileName(Uri uri) {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//        if (result == null) {
//            result = uri.getPath();
//            int cut = result.lastIndexOf('/');
//            if (cut != -1) {
//                result = result.substring(cut + 1);
//            }
//        }
//        return result;
//    }

    private String getCurrentTimestamp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        }
        return null;
    }
}