package com.example.dock;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cupboard.R;
import com.example.guide.MyAdapter;
import com.example.home.HomeAdapter;
import com.example.stock.StockLocationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class dockAdapter extends BaseAdapter {
    private Context mContext;
    private List<DockDetail> mList = new ArrayList<>();

    public dockAdapter(Context context, List<DockDetail> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        dockAdapter.ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new dockAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.dock_adapter, null);
            viewHolder.mName = (TextView) view.findViewById(R.id.dock_tv_name);
            viewHolder.mCount = (TextView) view.findViewById(R.id.dock_tv_count);
            viewHolder.gridView= view.findViewById(R.id.dock_detail_location);
            viewHolder.mBtn=view.findViewById(R.id.dock_btn_detail);
            view.setTag(viewHolder);
        } else {
            viewHolder = (dockAdapter.ViewHolder) view.getTag();
        }
        String instruName=mList.get(i).getInstruName();
        String sum=Integer.toString(mList.get(i).getSum());
        viewHolder.mName.setText(instruName);
        viewHolder.mCount.setText(sum);
        List<String> tempList=mList.get(i).getCupName();
        List<String> locationList=new ArrayList<>();
        Collections.sort(tempList);
        if(tempList.size()<5){
            for(int t=0;t<(5-tempList.size())/2;t++){
                locationList.add("");
            }
            for(String str :tempList){
                locationList.add(str);
            }
        }
        StockLocationAdapter adapter=new StockLocationAdapter(mContext,locationList);
        viewHolder.gridView.setAdapter(adapter);

        viewHolder.mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemDeleteListener.onDeleteClick(i);
            }
        });
        return view;
    }

    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    class ViewHolder {
        TextView mName;
        TextView mCount;
        GridView gridView;
        ImageView mBtn;
    }

}
