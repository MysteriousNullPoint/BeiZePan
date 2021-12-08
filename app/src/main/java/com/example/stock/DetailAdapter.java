package com.example.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.R;
import com.example.guide.MyAdapter;
import com.example.home.HomeAdapter;
import com.tuacy.azlist.AZBaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DetailAdapter extends AZBaseAdapter<ItemEntity, DetailAdapter.ItemHolder> {

    private Context mContext;
    public DetailAdapter(List<ItemEntity> dataList,Context context) {
        super(dataList);
        mContext=context;
    }

    private MyDao myDao;
    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_detail_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        String name=mDataList.get(position).getValue();
        int stock=mDataList.get(position).getStock();
        int sum=mDataList.get(position).getSum();
        String setStock= stock +" / "+ sum;
        holder.tvName.setText(name);
        holder.tvStock.setText(setStock);
        holder.tvScrap.setText(Integer.toString(mDataList.get(position).getScrapped()));
        holder.tvUsed.setText(Integer.toString(mDataList.get(position).getUsed()));

        myDao=new MyDao(mContext);
        List<String> mList=myDao.getCupName(name);
        Collections.sort(mList);
        List<String> locationList=new ArrayList<>();

        if(mList.size()<5){
            for(int t=0;t<(5-mList.size())/2;t++){
                locationList.add("");
            }
            for(String str :mList){
                locationList.add(str);
            }
        }
        else locationList.addAll(mList);
        StockLocationAdapter adapter=new StockLocationAdapter(mContext,locationList);
        holder.gridView.setAdapter(adapter);

        if(sum==0) holder.mButton.setImageLevel(1);
        else holder.mButton.setImageLevel(11);
        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemDeleteListener.onDeleteClick(position);
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

        TextView tvName;
        GridView gridView;
        TextView tvStock;
        TextView tvScrap;
        TextView tvUsed;
        ImageView mButton;

        ItemHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.stock_detail_instru);
            gridView=itemView.findViewById(R.id.stock_detail_location);
            tvStock=itemView.findViewById(R.id.stock_detail_stock);
            tvScrap=itemView.findViewById(R.id.stock_detail_scrap);
            tvUsed=itemView.findViewById(R.id.stock_detail_used);
            mButton=itemView.findViewById(R.id.stock_button);
        }
    }

}

