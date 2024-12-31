package com.example.referenceapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.referenceapp.R;
import com.example.referenceapp.model.Documents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UploadDocumentActivity extends AppCompatActivity {

    private AutoCompleteTextView schoolAutoComplete, categoryAutoComplete;
    private EditText etTitle, etDescription, etPrice, etImagePath, etStar;
    private ImageView imgSelected;
    private Button btnUpload;

    private List<String> schoolList = new ArrayList<>();
    private Map<String, Integer> schoolMap = new HashMap<>();
    private List<String> categoryList = new ArrayList<>();
    private Map<String, Integer> categoryMap = new HashMap<>();
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Integer selectedSchoolId;
    private Integer selectedCategoryId;

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        btnUpload = findViewById(R.id.btnUpload);
        schoolAutoComplete = findViewById(R.id.school);
        categoryAutoComplete = findViewById(R.id.category);
        etImagePath = findViewById(R.id.etImagePath);
        etStar = findViewById(R.id.etStar);
        imgSelected = findViewById(R.id.imgSelected);

        // Kiểm tra và yêu cầu quyền truy cập bộ nhớ
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Documents");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnUpload.setOnClickListener(v -> uploadDocument());
        etImagePath.setOnClickListener(v -> openImageChooser());

        loadSchoolsFromFirebase();
        loadCategoryFromFirebase();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, bạn có thể cho phép chọn ảnh
                openImageChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            Uri imageUri = data.getData();  // Lấy Uri của ảnh đã chọn

            if (imageUri != null) {
                imgSelected.setImageURI(imageUri);
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        StorageReference fileReference = storageReference.child("images/" + UUID.randomUUID().toString());

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Sau khi tải lên thành công, lấy URL tải lên của ảnh
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        etImagePath.setText(imageUrl); // Hiển thị URL ảnh vào EditText
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UploadDocumentActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
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
                setupAutoSchoolsCompleteAdapter(); // Cập nhật Adapter sau khi có dữ liệu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadDocumentActivity.this, "Failed to load schools", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoSchoolsCompleteAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, schoolList);
        schoolAutoComplete.setAdapter(adapter);

        // Cho phép hiển thị danh sách khi người dùng nhấn vào trường
        schoolAutoComplete.setOnClickListener(v -> schoolAutoComplete.showDropDown());

        // Xử lý khi người dùng chọn trường
        schoolAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSchool = (String) parent.getItemAtPosition(position);
            selectedSchoolId = schoolMap.get(selectedSchool);
            Log.d("SelectedSchool", "School Name: " + selectedSchool + ", ID: " + selectedSchoolId);
        });
    }

    private void loadCategoryFromFirebase() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryMap.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String name = categorySnapshot.child("Name").getValue(String.class);
                    int id = Integer.parseInt(Objects.requireNonNull(categorySnapshot.getKey()));
                    if (name != null) {
                        categoryList.add(name);
                        categoryMap.put(name, id);
                    }
                }
                setupAutoCategoryCompleteAdapter(); // Cập nhật Adapter sau khi có dữ liệu
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadDocumentActivity.this, "Failed to load category", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoCategoryCompleteAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryList);
        categoryAutoComplete.setAdapter(adapter);

        // Cho phép hiển thị danh sách khi người dùng nhấn vào trường
        categoryAutoComplete.setOnClickListener(v -> categoryAutoComplete.showDropDown());

        // Xử lý khi người dùng chọn trường
        categoryAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = (String) parent.getItemAtPosition(position);
            selectedCategoryId = categoryMap.get(selectedCategory);
            Log.d("SelectedCategory", "Category Name: " + selectedCategory + ", ID: " + selectedCategoryId);
        });
    }

    private void uploadDocument() {
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();
        double price = Double.parseDouble(etPrice.getText().toString());
        double star = Double.parseDouble(etStar.getText().toString());
        String imagePath = etImagePath.getText().toString();

        // Kiểm tra nếu trường học hoặc danh mục không hợp lệ
        if (selectedSchoolId == null) {
            Toast.makeText(this, "Please select a valid school", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId == null) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new document object
        Documents document = new Documents();
        document.setTitle(title);
        document.setDescription(description);
        document.setPrice(price);
        document.setStar(star);
        document.setImagePath(imagePath);
        document.setCategoryId(selectedCategoryId);
        document.setSchoolId(selectedSchoolId);
        document.setUserId(Integer.parseInt(Objects.requireNonNull(auth.getCurrentUser()).getUid())); // UserId of the current user
        document.setCreatedAt(getCurrentTimestamp());
        document.setUpdatedAt(getCurrentTimestamp());

        // Lưu đối tượng Documents vào Realtime Database
        String documentId = databaseReference.push().getKey();
        if (documentId != null) {
            databaseReference.child(documentId).setValue(document)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UploadDocumentActivity.this, "Document uploaded successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UploadDocumentActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(UploadDocumentActivity.this, "Failed to save document info", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getCurrentTimestamp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        }
        return null;
    }
}
