package com.example.referenceapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.referenceapp.R;
import com.example.referenceapp.model.Documents;
import com.example.referenceapp.viewHolder.DocumentViewHolder;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentViewHolder> {
    List<Documents> documentList;
    Context context;

    public DocumentAdapter(List<Documents> documentList) {
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.bind(documentList.get(position));
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }
}
