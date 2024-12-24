package com.example.referenceapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.referenceapp.R;
import com.example.referenceapp.activity.ListLessonActivity;
import com.example.referenceapp.model.Category;
import com.example.referenceapp.model.CategoryDiffCallback;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewholder> {
    ArrayList<Category> items;
    Context context;

    public CategoryAdapter(ArrayList<Category> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CategoryAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_category, parent, false);
        return new viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.titleTxt.setText(items.get(position).getName());

        Glide.with(context)
                .load(items.get(position).getImagePath())
                .into(holder.pic);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListLessonActivity.class);
                intent.putExtra("CategoryId", items.get(position).getId());
                intent.putExtra("CategoryName", items.get(position).getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Phương thức để cập nhật danh sách sử dụng DiffUtil
    public void setItems(ArrayList<Category> newItems) {
        Log.d("CategoryAdapter", "New items size: " + newItems.size());
        // Tạo DiffUtil callback
        CategoryDiffCallback diffCallback = new CategoryDiffCallback(this.items, newItems);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // Cập nhật danh sách
        this.items.clear();
        this.items.addAll(newItems);

        // Thông báo cho RecyclerView cập nhật
        diffResult.dispatchUpdatesTo(this);
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.categoryNameTxt);
            pic = itemView.findViewById(R.id.imgCategory);
        }
    }
}
