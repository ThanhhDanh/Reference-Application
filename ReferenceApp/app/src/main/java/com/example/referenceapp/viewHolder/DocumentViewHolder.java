package com.example.referenceapp.viewHolder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.referenceapp.R;
import com.example.referenceapp.model.Documents;

public class DocumentViewHolder extends RecyclerView.ViewHolder {
    ImageView documentImage;
    TextView documentTitle, documentDescription;

    public DocumentViewHolder(@NonNull View itemView) {
        super(itemView);
        documentImage = itemView.findViewById(R.id.document_image);
        documentTitle = itemView.findViewById(R.id.document_title);
        documentDescription = itemView.findViewById(R.id.document_description);
    }

    public void bind(Documents document) {
        if (document != null) {
            documentTitle.setText(document.getTitle());
            documentDescription.setText(document.getDescription());
            Glide.with(itemView.getContext())
                    .load(document.getImagePath())
                    .into(documentImage);
        } else {
            documentTitle.setText("No document");
        }
    }
}