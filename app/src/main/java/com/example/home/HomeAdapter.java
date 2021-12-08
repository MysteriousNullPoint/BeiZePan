package com.example.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cupboard.R;
import com.example.guide.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList = new ArrayList<>();

    public HomeAdapter(Context context, List<String> list) {
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
        HomeAdapter.ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new HomeAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.home_cupboard, null);
            viewHolder.mTextView = (TextView) view.findViewById(R.id.home_cupboard_name);
            viewHolder.mButton = (ImageView) view.findViewById(R.id.home_cupboard_state);
            view.setTag(viewHolder);
        } else {
            viewHolder = (HomeAdapter.ViewHolder) view.getTag();
        }
        viewHolder.mTextView.setText(mList.get(i).substring(0,3));
        int level=Integer.parseInt(mList.get(i).substring(3));
        viewHolder.mButton.setImageLevel(level);
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
    public interface onItemDeleteListener {
        void onDeleteClick(int i);
    }

    private MyAdapter.onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(MyAdapter.onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    class ViewHolder {
        TextView mTextView;
        ImageView mButton;
    }

}