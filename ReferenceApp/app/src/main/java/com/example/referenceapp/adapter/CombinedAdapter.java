package com.example.referenceapp.adapter;


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
import com.example.referenceapp.activity.DetailActivity;
import com.example.referenceapp.model.CombinedDiffCallback;
import com.example.referenceapp.model.Documents;

import java.util.ArrayList;
import java.util.List;

public class CombinedAdapter extends RecyclerView.Adapter<CombinedAdapter.DocumentViewHolder> {
    private List<Documents> documents;
    private Context context;

    public CombinedAdapter(List<Documents> documents) {
        this.documents = documents;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        holder.bind(documents.get(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", documents.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public void setItems(List<Documents> newDocuments) {
        if (newDocuments == null) {
            newDocuments = new ArrayList<>();
        }


        // Tạo DiffUtil callback
        DiffUtil.Callback diffCallback = new CombinedDiffCallback(this.documents, newDocuments);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // Cập nhật danh sách
        this.documents.clear();
        this.documents.addAll(newDocuments);

        diffResult.dispatchUpdatesTo(this); // Cập nhật RecyclerView
    }


    // DocumentViewHolder class
    public class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView documentTitle, documentDescription;
        ImageView documentImage;

        public DocumentViewHolder(View itemView) {
            super(itemView);
            documentTitle = itemView.findViewById(R.id.document_title);
            documentImage = itemView.findViewById(R.id.document_image);
            documentDescription = itemView.findViewById(R.id.document_description);
        }

        public void bind(Documents document) {
            documentTitle.setText(document.getTitle());
            documentDescription.setText(document.getDescription());
            // Kiểm tra nếu có ảnh thì tải vào ImageView
            if (document.getImagePath() != null && !document.getImagePath().isEmpty()) {
                Glide.with(itemView.getContext()) // Glide là thư viện tải ảnh
                        .load(document.getImagePath())  // Đường dẫn ảnh hoặc URL
                        .into(documentImage);  // Đặt ảnh vào ImageView
            } else {
                documentImage.setVisibility(View.GONE);  // Ẩn ImageView nếu không có ảnh
            }
        }
    }
}
