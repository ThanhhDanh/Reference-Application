package com.example.referenceapp.activity;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.referenceapp.R;
import com.example.referenceapp.adapter.CategoryAdapter;
import com.example.referenceapp.adapter.CombinedAdapter;
import com.example.referenceapp.adapter.SliderAdapter;
import com.example.referenceapp.model.Category;
import com.example.referenceapp.model.Documents;
import com.example.referenceapp.model.SliderItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMain;
    private ViewPager2 viewPager2;
    private ChipNavigationBar bottomMenu;
    private ProgressBar progressBarCategory;
    private ProgressBar progressBarBanner;
    private EditText searchEditText;
    private CombinedAdapter combinedAdapter;
    private CategoryAdapter categoryAdapter;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    // Lưu trữ danh sách danh mục ban đầu
    private List<Category> originalCategoryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        AnhXa();
        updateBottomNavigationBar();
        initCategory();
        initBanner();

        setupSearchListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomMenu.setItemSelected(R.id.home, true);
    }

    private void AnhXa() {
        recyclerViewMain = findViewById(R.id.recycleView);
        bottomMenu = findViewById(R.id.bottomMenu);
        progressBarCategory = findViewById(R.id.progressBarCategory);
        viewPager2 = findViewById(R.id.viewpager2);
        progressBarBanner = findViewById(R.id.progressBarBanner);
        searchEditText = findViewById(R.id.search);
    }

    private void initBanner() {
        DatabaseReference myRef = database.getReference("Banners");
        progressBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        items.add(issue.getValue(SliderItems.class));
                    }
                    banners(items);
                    progressBarBanner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void banners(ArrayList<SliderItems> items) {
        viewPager2.setAdapter(new SliderAdapter(items, viewPager2));
        viewPager2.setClipChildren(false);
        viewPager2.setClipToPadding(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        viewPager2.setPageTransformer(compositePageTransformer);
    }


    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category");
        progressBarCategory.setVisibility(View.VISIBLE);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Category> list = new ArrayList<>();
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Category category = issue.getValue(Category.class);
                        if (category != null) {
                            list.add(category);
                        }
                    }
                } else {
                    Log.d("InitCategory", "No categories found in Firebase.");
                }

                // Lưu danh sách ban đầu
                originalCategoryList = new ArrayList<>(list);

                // Cập nhật danh sách cho categoryAdapter
                if (categoryAdapter == null) {
                    categoryAdapter = new CategoryAdapter(list);
                    recyclerViewMain.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                    recyclerViewMain.setAdapter(categoryAdapter);
                } else {
                    categoryAdapter.setItems(list); // Cập nhật danh sách nếu adapter đã tồn tại
                }


                // Đảm bảo RecyclerView có visibility là VISIBLE
                recyclerViewMain.setVisibility(View.VISIBLE);

                progressBarCategory.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Error fetching categories: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarCategory.setVisibility(View.GONE);
            }
        });
    }

    // Cập nhật phương thức restoreCategoryList
    private void restoreCategoryList() {
        if (originalCategoryList != null && !originalCategoryList.isEmpty()) {
            if (categoryAdapter != null) {
                categoryAdapter.setItems(new ArrayList<>(originalCategoryList)); // Khôi phục danh sách ban đầu
            } else {
                Log.d("RestoreCategory", "CategoryAdapter is null");
            }

            // Đảm bảo RecyclerView có visibility là VISIBLE
            recyclerViewMain.setVisibility(View.VISIBLE);
        } else {
            Log.d("RestoreCategory", "Original category list is empty or null");
        }
    }

    private void displaySearchResults(List<Documents> documents) {
        if (combinedAdapter == null) {
            combinedAdapter = new CombinedAdapter(new ArrayList<>()); // Khởi tạo adapter với danh sách rỗng
            recyclerViewMain.setLayoutManager(new GridLayoutManager(this, 1));
            recyclerViewMain.setAdapter(combinedAdapter);
        }
        combinedAdapter.setItems(documents); // Cập nhật danh sách mới
    }

    // Tìm kiếm
    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                // Hủy các callback trước đó
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }


                // Tạo callback mới với debounce
                searchRunnable = () -> {
                    if (!query.isEmpty()) {
                        searchDatabase(query); // Chỉ tìm kiếm nếu có query
                    } else {
                        // Gọi phương thức để khôi phục danh sách danh mục
                        restoreCategoryList();
                    }
                };

                handler.postDelayed(searchRunnable, 800); // Debounce 800ms
            }
        });
    }

    private void searchDatabase(String query) {

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm kiếm trong Documents
        DatabaseReference docRef = database.getReference("Documents");
        docRef.orderByChild("Title").startAt(query).endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Documents> documents = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Documents document = child.getValue(Documents.class);
                            if (document != null) {
                                documents.add(document);
                            }
                        }
                        displaySearchResults(documents); // Hiển thị kết quả Documents
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("SearchError", "Error fetching documents: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBottomNavigationBar() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Nếu đã đăng nhập, hiển thị các mục sau
            bottomMenu.setMenuResource(R.menu.bottom_menu);
        } else {
            // Nếu chưa đăng nhập, hiển thị các mục cơ bản
            bottomMenu.setMenuResource(R.menu.bottom_menu_log_out);
        }

        bottomMenu.setItemSelected(R.id.home, true);

        bottomMenu.setOnItemSelectedListener(i -> {
            if (i == R.id.action_register) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            } else if (i == R.id.action_login) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else if (i == R.id.cart) {
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            } else if (i == R.id.message) {
                // Xử lý khi chọn "Message"
            } else if (i == R.id.profile) {
                // Mở trang Profile và thêm tùy chọn đăng xuất
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }


    private void showLogoutOption() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    updateBottomNavigationBar(); // Cập nhật lại menu
                })
                .setNegativeButton("No", null)
                .show();
    }

//    private boolean isConnected(Context context) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivityManager != null) {
//            Network network = connectivityManager.getActiveNetwork();
//            if (network != null) {
//                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
//                return networkCapabilities != null &&
//                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
//                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
//            }
//        }
//        return false;
//    }

}