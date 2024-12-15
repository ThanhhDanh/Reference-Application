package com.example.referenceapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.referenceapp.R;
import com.example.referenceapp.adapter.LessonListAdapter;
import com.example.referenceapp.model.Documents;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListLessonActivity extends AppCompatActivity {

    private int categoryId;
    private FirebaseDatabase database;
    private TextView titleTxt;
    private ImageView backBtn;
    private ProgressBar progressBarListLesson;
    private RecyclerView lessonListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_lesson);

        database = FirebaseDatabase.getInstance();

        AnhXa();
        getIntentExtra();
        initList();
    }

    @SuppressLint("WrongViewCast")
    private void AnhXa() {
        titleTxt = findViewById(R.id.titleTxt);
        backBtn = findViewById(R.id.backEmptyBtn);
        progressBarListLesson = findViewById(R.id.progressBarListLesson);
        lessonListView = findViewById(R.id.lessonListView);
    }

    private void initList() {
        DatabaseReference myRef = database.getReference("Documents");
        progressBarListLesson.setVisibility(View.VISIBLE);
        ArrayList<Documents> list = new ArrayList<>();
        Query query = myRef.orderByChild("CategoryId").equalTo(categoryId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot issue:snapshot.getChildren()) {
                        list.add(issue.getValue(Documents.class));
                    }
                    if(!list.isEmpty()){
                        lessonListView.setLayoutManager(new LinearLayoutManager(ListLessonActivity.this, LinearLayoutManager.VERTICAL, false));
                        lessonListView.setAdapter(new LessonListAdapter(list));
                    }
                    progressBarListLesson.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        String categoryName = getIntent().getStringExtra("CategoryName");

        titleTxt.setText(categoryName);
        backBtn.setOnClickListener(v -> finish());
    }
}