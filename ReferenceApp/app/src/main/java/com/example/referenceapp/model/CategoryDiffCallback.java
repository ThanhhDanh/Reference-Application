package com.example.referenceapp.model;

        import android.util.Log;

        import androidx.recyclerview.widget.DiffUtil;

        import java.util.List;

public class CategoryDiffCallback extends DiffUtil.Callback {
    private final List<Category> oldList;
    private final List<Category> newList;

    public CategoryDiffCallback(List<Category> oldList, List<Category> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId(); // So sánh ID hoặc thuộc tính duy nhất
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition)); // So sánh nội dung
    }
}