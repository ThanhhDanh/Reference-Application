package com.example.referenceapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.referenceapp.Helper.ManagmentCart;
import com.example.referenceapp.R;
import com.example.referenceapp.adapter.CartAdapter;
import com.example.referenceapp.model.CreateOrder;
import com.example.referenceapp.model.Orders;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import vn.momo.momo_partner.AppMoMoLib;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CartActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference ordersRef;
    private ManagmentCart managmentCart;
    private ImageView backBtn, backEmptyBtn;
    private TextView totalFeeTxt, taxTxt, deliveryTxt, totalTxt, emptyTxt;
    private ScrollView scrollViewCart;
    private RecyclerView cartView;
    private AppCompatButton checkOutBtn;

    private RadioGroup paymentRadioGroup;
    private RadioButton rbMoMo, rbZaloPay;
    private Orders currentOrder;

    //Môi trường Momo (Phải có tài khoản Business Momo)
    AppMoMoLib.ENVIRONMENT environment = AppMoMoLib.ENVIRONMENT.DEVELOPMENT; // DEV: AppMoMoLib.ENVIRONMENT.PRODUCTION
    private String merchantName = "Demo SDK";
    private String merchantCode = "SCB01";
    private String description = "Thanh toán dịch vụ ABC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("Orders");

        managmentCart = new ManagmentCart(this);

        AppMoMoLib.getInstance().setEnvironment(environment);

        //Zalopay
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2554, Environment.SANDBOX);

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
        checkOutBtn = findViewById(R.id.checkOutBtn);
        paymentRadioGroup = findViewById(R.id.paymentRadioGroup);
        rbMoMo = findViewById(R.id.rbMoMo);
        rbZaloPay = findViewById(R.id.rbZaloPay);
    }

    private void initCartList() {
        if (managmentCart.getListCart().isEmpty()) {
            emptyTxt.setVisibility(View.VISIBLE);
            backEmptyBtn.setVisibility(View.VISIBLE);
            scrollViewCart.setVisibility(View.GONE);
        } else {
            emptyTxt.setVisibility(View.GONE);
            backEmptyBtn.setVisibility(View.GONE);
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

        // Create order object
        currentOrder = new Orders();
        currentOrder.setAmount((int) itemTotal);
        currentOrder.setFeeDelivery(delivery);
        currentOrder.setTotal(total);
        currentOrder.setTotalTax(tax);
        currentOrder.setSubTotal(itemTotal);
    }

    //Get token through MoMo app
    private void requestPayment(String orderId) {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);

        Map<String, Object> eventValue = new HashMap<>();
        //client Required
        eventValue.put("merchantname", merchantName); //Tên đối tác. được đăng ký tại https://business.momo.vn. VD: Google, Apple, Tiki , CGV Cinemas
        eventValue.put("merchantcode", merchantCode); //Mã đối tác, được cung cấp bởi MoMo tại https://business.momo.vn
        eventValue.put("amount", currentOrder.getTotal()); //Kiểu integer
        eventValue.put("orderId", orderId); //uniqueue id cho Bill order, giá trị duy nhất cho mỗi đơn hàng
        eventValue.put("orderLabel", "Hóa đơn #" + orderId); //gán nhãn

        //client Optional - bill info
        eventValue.put("merchantnamelabel", "Dịch vụ");//gán nhãn
        eventValue.put("fee", currentOrder.getFeeDelivery()); //Kiểu integer
        eventValue.put("description", description); //mô tả đơn hàng - short description

        //client extra data
        eventValue.put("requestId", merchantCode + "merchant_billId_" + System.currentTimeMillis());
        eventValue.put("partnerCode", merchantCode);

        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);


    }

    //Get token callback from MoMo app an submit to server side
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if (data != null && data.getIntExtra("status", -1) == 0) {
                // Cập nhật phương thức thanh toán cho đơn hàng
                currentOrder.setPaymentMethod("MoMo");
                currentOrder.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                // Lưu đơn hàng vào cơ sở dữ liệu
                saveOrderToDatabase(currentOrder);

                // Hiển thị thông báo thanh toán thành công
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();

                // Chuyển hướng về MainActivity
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa các activity hiện tại trong stack
                startActivity(intent);
                finish(); // Đóng CartActivity
            } else {
                // Hiển thị thông báo thanh toán thất bại
                Toast.makeText(this, "Payment failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveOrderToDatabase(Orders order) {
        String key = ordersRef.push().getKey();
        if (key != null) {
            order.setId(Integer.parseInt(key));
            ordersRef.child(key).setValue(order).addOnSuccessListener(aVoid ->
                    Log.d("SaveOrder", "Đơn hàng đã được lưu thành công!")
            ).addOnFailureListener(e ->
                    Log.e("SaveOrder", "Lỗi khi lưu đơn hàng: " + e.getMessage())
            );
        }
    }

    private void requestZalo() {
        CreateOrder orderApi = new CreateOrder();

        try {
            JSONObject data = orderApi.createOrder(String.valueOf(currentOrder.getTotal()));
            String code = data.getString("return_code");
            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(CartActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        // Cập nhật phương thức thanh toán cho đơn hàng
                        currentOrder.setPaymentMethod("ZaloPay");
                        currentOrder.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                        // Lưu đơn hàng vào cơ sở dữ liệu
                        saveOrderToDatabase(currentOrder);

                        // Hiển thị thông báo thanh toán thành công
                        Toast.makeText(CartActivity.this, "Payment successful!", Toast.LENGTH_SHORT).show();

                        // Chuyển hướng về MainActivity
                        Intent intent = new Intent(CartActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa các activity hiện tại trong stack
                        startActivity(intent);
                        finish(); // Đóng CartActivity
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Toast.makeText(CartActivity.this, "Payment canceled.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Toast.makeText(CartActivity.this, "Payment error: " + zaloPayError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVariable() {
        backBtn.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, MainActivity.class)));
        backEmptyBtn.setOnClickListener(v -> startActivity(new Intent(CartActivity.this, MainActivity.class)));

        // Xử lý nút CheckOut
        checkOutBtn.setOnClickListener(v -> {
            int selectedId = paymentRadioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.rbMoMo) {
                if (currentOrder != null) {
                    requestPayment(String.valueOf(System.currentTimeMillis()));
                } else {
                    Toast.makeText(this, "Invalid order.", Toast.LENGTH_SHORT).show();
                }
            } else if (selectedId == R.id.rbZaloPay) {
                if (currentOrder != null) {
                    requestZalo();
                } else {
                    Toast.makeText(this, "Invalid order.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please select payment method!!!.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}