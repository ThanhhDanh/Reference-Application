package com.example.referenceapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.referenceapp.R;
import com.example.referenceapp.adapter.CategoryAdapter;
import com.example.referenceapp.adapter.SliderAdapter;
import com.example.referenceapp.model.Category;
import com.example.referenceapp.model.SliderItems;
import com.google.android.material.navigation.NavigationView;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int ACTION_REGISTER = R.id.action_register;
    private static final int ACTION_LOGIN = R.id.action_login;
    private static final int ACTION_LOGOUT = R.id.action_logout;
    private Toolbar toolbar;
    private RecyclerView recyclerViewMain;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ViewPager2 viewPager2;
    private ChipNavigationBar bottomMenu;
    private ProgressBar progressBarCategory;
    private ProgressBar progressBarBanner;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomMenu.setItemSelected(R.id.home, true);
    }

    private void AnhXa() {
        toolbar = findViewById(R.id.toolBarMain);
        recyclerViewMain = findViewById(R.id.recycleView);
        drawerLayout = findViewById(R.id.drawerLayout);
        bottomMenu = findViewById(R.id.bottomMenu);
        progressBarCategory = findViewById(R.id.progressBarCategory);
        viewPager2 = findViewById(R.id.viewpager2);
        progressBarBanner = findViewById(R.id.progressBarBanner);
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
        ArrayList<Category> list = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Category category = issue.getValue(Category.class);
                        if (category != null) {
                            list.add(category);
                        }
                    }
                    if (!list.isEmpty()) {
                        recyclerViewMain.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                        recyclerViewMain.setAdapter(new CategoryAdapter(list));
                    }
                    progressBarCategory.setVisibility(View.GONE);
                } else {
                    // Log hoặc xử lý nếu dữ liệu không tồn tại
                    Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                    progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBarCategory.setVisibility(View.GONE);
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
                showLogoutOption();
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

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                return networkCapabilities != null &&
                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        }
        return false;
    }

}