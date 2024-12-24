package com.example.referenceapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.referenceapp.Helper.DateUtils;
import com.example.referenceapp.Helper.ManagmentCart;
import com.example.referenceapp.R;
import com.example.referenceapp.model.Documents;

public class DetailActivity extends AppCompatActivity {

    private AppCompatButton addBtn;
    private ImageView picDetail, backBtn;
    private TextView priceTxt, titleTxt, timeTxt, descriptionTxt, ratingTxt, totalTxt, minusTxt, plusTxt, numTxt;
    private RatingBar ratingBar;
    private Documents object;
    private int num=1;
    private ManagmentCart managmentCart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        AnhXa();
        getIntentExtra();
        setVariable();
    }

    @SuppressLint("WrongViewCast")
    private void AnhXa() {
        backBtn = findViewById(R.id.backEmptyBtn);
        picDetail = findViewById(R.id.picDetail);
        priceTxt = findViewById(R.id.priceTxt);
        titleTxt = findViewById(R.id.titleTxt);
        timeTxt = findViewById(R.id.timeTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        ratingTxt =findViewById(R.id.ratingTxt);
        ratingBar = findViewById(R.id.ratingBar);
        totalTxt = findViewById(R.id.totalTxt);
        plusTxt = findViewById(R.id.plusBtn);
        minusTxt = findViewById(R.id.minusBtn);
        numTxt = findViewById(R.id.numTxt);
        addBtn = findViewById(R.id.addBtn);
    }

    private void setVariable() {
        managmentCart = new ManagmentCart(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Glide.with(this)
                .load(object.getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(60))
                .into(picDetail);

        priceTxt.setText("$" + object.getPrice());
        titleTxt.setText(object.getTitle());
        descriptionTxt.setText(object.getDescription());
        ratingTxt.setText(object.getStar() + "Rating");
        ratingBar.setRating((float) object.getStar());
        totalTxt.setText(num*object.getPrice() + "$");
        String formattedDate = DateUtils.formatDate(object.getCreatedAt());
        timeTxt.setText(formattedDate + " min");

        plusTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num = num + 1;
                numTxt.setText(num + "");
                totalTxt.setText("$" + (num * object.getPrice()));
            }
        });

        minusTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(num>1){
                    num=num-1;
                    numTxt.setText(num + "");
                    totalTxt.setText("$" + (num * object.getPrice()));
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                object.setNumberInCart(num);
                managmentCart.insertFood(object);
            }
        });
    }

    private void getIntentExtra() {
        object = (Documents) getIntent().getSerializableExtra("object");
    }
}