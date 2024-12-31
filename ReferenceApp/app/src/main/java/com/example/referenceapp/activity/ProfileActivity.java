package com.example.referenceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.referenceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ImageView backProfileBtn;
    private TextView nameTxt, emailTxt, roleTxt;
    private Button btnEditProfile, btnLogout;
    private LinearLayout layoutFavourites, layoutDocuments, layoutUpdateDocuments;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        AnhXa();
        setVariable();

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        loadUserProfile();
    }

    private void AnhXa() {
        backProfileBtn = findViewById(R.id.backProfileBtn);
        nameTxt = findViewById(R.id.nameTxt);
        emailTxt = findViewById(R.id.emailTxt);
        roleTxt = findViewById(R.id.roleTxt);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        layoutDocuments = findViewById(R.id.layoutDocuments);
        layoutFavourites = findViewById(R.id.layoutFavourites);
        layoutUpdateDocuments = findViewById(R.id.layoutUploadDocuments);
    }

    private void loadUserProfile() {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid(); // Lấy UID người dùng hiện tại

        Log.d("UserId", userId);

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);

                    String fullName = (lastName != null ? lastName : "") + " " + (firstName != null ? firstName : "");

                    // Hiển thị dữ liệu lên giao diện
                    nameTxt.setText(fullName.trim());
                    emailTxt.setText(email);
                    roleTxt.setText(role);
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVariable() {
        backProfileBtn.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });

        layoutUpdateDocuments.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UploadDocumentActivity.class);
            startActivity(intent);
        });
    }
}