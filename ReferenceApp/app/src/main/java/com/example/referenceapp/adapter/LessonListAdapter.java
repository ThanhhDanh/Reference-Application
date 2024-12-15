package com.example.referenceapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.referenceapp.Helper.DateUtils;
import com.example.referenceapp.R;
import com.example.referenceapp.activity.DetailActivity;
import com.example.referenceapp.model.Documents;

import java.util.ArrayList;

public class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.viewholder> {
    ArrayList<Documents> items;
    Context context;

    public LessonListAdapter(ArrayList<Documents> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public LessonListAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new viewholder(LayoutInflater.from(context).inflate(R.layout.viewholder_list_lesson, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull LessonListAdapter.viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.titleTxt.setText(items.get(position).getTitle());

        // Chuyển đổi định dạng ngày trước khi hiển thị
        String formattedDate = DateUtils.formatDate(items.get(position).getCreatedAt());
        holder.timeTxt.setText(formattedDate + " min");
        holder.priceTxt.setText("$" + items.get(position).getPrice());
        holder.rateTxt.setText("" + items.get(position).getStar());

        Glide.with(context)
                .load(items.get(position).getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(50))
                .into(holder.pic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", items.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt,priceTxt,rateTxt,timeTxt;
        ImageView pic;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            rateTxt = itemView.findViewById(R.id.ratingTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.img);
        }
    }
}
