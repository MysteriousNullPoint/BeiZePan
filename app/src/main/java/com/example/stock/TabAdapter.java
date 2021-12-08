package com.example.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.cupboard.R;

import java.util.ArrayList;
import java.util.List;

public class TabAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList = new ArrayList<>();

    public TabAdapter(Context context, List<String> list) {
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
            view = LayoutInflater.from(mContext).inflate(R.layout.stock_tab_adapter, null);
            tv = (TextView) view.findViewById(R.id.stock_tab);
            view.setTag(tv);
        } else {
            tv = (TextView) view.getTag();
        }
        if (mList.get(i).length() > 5) tv.setText(mList.get(i).substring(1, 5));
        else tv.setText(mList.get(i).substring(1));
        if (mList.get(i).substring(0, 1).equals("1")) {
            tv.setTextColor(Color.parseColor("#3399FE"));
            if (i == 0) tv.setBackgroundResource(R.drawable.stock_tab_first);
            else tv.setBackgroundResource(R.drawable.stock_tab_other);
            tv.setSelected(true);
        } else {
            tv.setTextColor(Color.parseColor("#ffffff"));
            if (i == 0) tv.setBackgroundResource(R.drawable.stock_tab_first);
            else tv.setBackgroundResource(R.drawable.stock_tab_other);
            tv.setSelected(false);
        }
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mOnItemDeleteListener.onDeleteClick(i);
            }
        });
        return view;
    }

    /**
     * 删除按钮的监听接口
     */
    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }


}

