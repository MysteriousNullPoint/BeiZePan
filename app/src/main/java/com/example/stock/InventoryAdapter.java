package com.example.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.R;
import com.tuacy.azlist.AZBaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends AZBaseAdapter<ItemEntity, InventoryAdapter.ItemHolder> {

    private Context mContext;
    public InventoryAdapter(List<ItemEntity> dataList, Context context) {
        super(dataList);
        mContext=context;
    }

    private MyDao myDao;
    @NonNull
    @Override
    public InventoryAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InventoryAdapter.ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_detail_adapter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        String name=mDataList.get(position).getValue();
        int remain=mDataList.get(position).getStock();
        int stock=mDataList.get(position).getSum();
        int inventory=mDataList.get(position).getScrapped();
        holder.tvName.setText(name);
        holder.tvStock.setText(Integer.toString(remain));
        holder.tvSum.setText(Integer.toString(stock));
        holder.tvInventory.setText(Integer.toString(inventory));

        myDao=new MyDao(mContext);
        List<String> mList=myDao.getCupName(name);

        List<String> locationList=new ArrayList<>();

        if(mList.size()<5){
            for(int t=0;t<(5-mList.size())/2;t++){
                locationList.add("");
            }
            for(String str :mList){
                locationList.add(str);
            }
        }
        StockLocationAdapter adapter=new StockLocationAdapter(mContext,locationList);
        holder.gridView.setAdapter(adapter);

        if(stock==inventory) holder.mButton.setImageLevel(31);
        else holder.mButton.setImageLevel(21);
        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stock!=inventory) mOnItemDeleteListener.onDeleteClick(position);
            }
        });
    }

    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }

    private InventoryAdapter.onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(InventoryAdapter.onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        GridView gridView;
        TextView tvStock;
        TextView tvSum;
        TextView tvInventory;
        ImageView mButton;

        ItemHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.stock_detail_instru);
            gridView=itemView.findViewById(R.id.stock_detail_location);
            tvStock=itemView.findViewById(R.id.stock_detail_stock);
            tvSum=itemView.findViewById(R.id.stock_detail_used);
            tvInventory=itemView.findViewById(R.id.stock_detail_scrap);
            mButton=itemView.findViewById(R.id.stock_button);
        }
    }



}
