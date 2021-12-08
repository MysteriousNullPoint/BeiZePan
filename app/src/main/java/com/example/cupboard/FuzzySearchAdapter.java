package com.example.cupboard;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stock.ItemEntity;
import com.tuacy.fuzzysearchlibrary.FuzzySearchBaseAdapter;

import java.util.List;

public class FuzzySearchAdapter extends FuzzySearchBaseAdapter<ItemEntity, FuzzySearchAdapter.ItemHolder> {

    public FuzzySearchAdapter(List<ItemEntity> dataList) {
        super(null, dataList);
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FuzzySearchAdapter.ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemHolder holder, int position) {
        holder.mTextName.setText(mDataList.get(position).getValue());
        holder.mTextName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemDeleteListener.onDeleteClick(holder.getAdapterPosition());
            }
        });
    }

    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        TextView mTextName;

        ItemHolder(View itemView) {
            super(itemView);
            mTextName = itemView.findViewById(R.id.text_item_name);
        }
    }

}

