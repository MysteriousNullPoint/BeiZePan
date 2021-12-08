package com.example.cupboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.stock.StockLocationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FindInstruAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList;
    private MyDao myDao;

    public FindInstruAdapter(Context context,List<String>list) {
        mContext = context;mList=list;
    }

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
        ViewHolder viewHolder ;
        if (view == null) {
            viewHolder =new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.find_instru_adapter, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.find_name);
            viewHolder.mButton = (ImageView) view.findViewById(R.id.find_add_address);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        String name=mList.get(i);
        viewHolder.tvName.setText(name);

        viewHolder.gridView = view.findViewById(R.id.find_detail_location);
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
        viewHolder.gridView.setAdapter(adapter);

        viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemDeleteListener.onDeleteClick(i);
            }
        });
        return view;
    }

    /**
     * 删除按钮的监听接口
     */
    public interface onSearchDeleteListener {
        void onDeleteClick(int i);
    }

    private onSearchDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onSearchDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    class ViewHolder {
        TextView tvName;
        GridView gridView;
        ImageView mButton;
    }


}

