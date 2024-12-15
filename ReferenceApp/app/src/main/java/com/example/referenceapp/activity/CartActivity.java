package com.example.referenceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.referenceapp.Helper.ManagmentCart;
import com.example.referenceapp.R;
import com.example.referenceapp.adapter.CartAdapter;
import com.google.firebase.database.FirebaseDatabase;

public class CartActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private ManagmentCart managmentCart;
    private ImageView backBtn, backEmptyBtn;
    private TextView totalFeeTxt, taxTxt, deliveryTxt, totalTxt, emptyTxt;
    private ScrollView scrollViewCart;
    private RecyclerView cartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        database = FirebaseDatabase.getInstance();

        managmentCart = new ManagmentCart(this);

        AnhXa();
        setVariable();
        calculateCart();
        initCartList();
    }


    private void AnhXa() {
        backBtn = findViewById(R.id.backBtn);
        backEmptyBtn = findViewById(R.id.backEmptyBtn);
        totalFeeTxt = findViewById(R.id.totalFeeTxt);
        taxTxt = findViewById(R.id.totalTaxTxt);
        deliveryTxt = findViewById(R.id.deliveryTxt);
        totalTxt = findViewById(R.id.totalTxt);
        emptyTxt = findViewById(R.id.emptyTxt);
        scrollViewCart = findViewById(R.id.scrollViewCart);
        cartView = findViewById(R.id.cartView);
    }

    private void initCartList() {
        if (managmentCart.getListCart().isEmpty()) {
            emptyTxt.setVisibility(View.VISIBLE);
            scrollViewCart.setVisibility(View.GONE);
        } else {
            emptyTxt.setVisibility(View.GONE);
            scrollViewCart.setVisibility(View.VISIBLE);
        }
        cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        cartView.setAdapter(new CartAdapter(managmentCart.getListCart(), managmentCart, () -> calculateCart()));
    }

    private void calculateCart() {
        double percentTax = 0.02; // 2% tax
        double delivery = 10; //10$
        double tax = Math.round(managmentCart.getTotalFee() * percentTax * 100.0) / 100;
        double total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100) / 100;
        double itemTotal = Math.round(managmentCart.getTotalFee() * 100) / 100;

        totalFeeTxt.setText("$" + itemTotal);
        taxTxt.setText("$" + tax);
        deliveryTxt.setText("$" + delivery);
        totalTxt.setText("$" + total);
    }

    private void setVariable() {
        backBtn.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, MainActivity.class)));
        backEmptyBtn.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, MainActivity.class)));
    }
}