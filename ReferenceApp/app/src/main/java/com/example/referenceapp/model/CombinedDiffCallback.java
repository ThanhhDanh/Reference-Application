package com.example.referenceapp.model;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class CombinedDiffCallback extends DiffUtil.Callback {
    private final List<Documents> oldList;
    private final List<Documents> newList;

    public CombinedDiffCallback(List<Documents> oldList, List<Documents> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // So sánh dựa trên ID hoặc thuộc tính duy nhất
        return oldList.get(oldItemPosition).getTitle().equals(newList.get(newItemPosition).getTitle());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // So sánh dựa trên equals để kiểm tra nội dung
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}