package com.example.stock;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cupboard.R;

import java.util.ArrayList;
import java.util.List;

public class StockLocationAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList = new ArrayList<>();

    public StockLocationAdapter(Context context, List<String> list) {
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
        TextView tv;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.stock_location_adapter, null);
            tv = (TextView) view.findViewById(R.id.stock_location);
            view.setTag(tv);
        } else {
            tv = (TextView) view.getTag();
        }
        String str=mList.get(i);
        if(str.equals("")) tv.setBackgroundColor(Color.argb(0, 0, 0, 0));
        tv.setText(str);
        return view;
    }
}