package com.example.file;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cupboard.R;
import com.example.guide.MyAdapter;
import com.example.home.HomeAdapter;

import java.util.ArrayList;
import java.util.List;

public class fileAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList = new ArrayList<>();

    public fileAdapter(Context context, List<String> list) {
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
        fileAdapter.ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new fileAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.file_adapter, null);
            viewHolder.mTextView = (TextView) view.findViewById(R.id.file_icon_name);
            viewHolder.mImageView = (ImageView) view.findViewById(R.id.file_icon_image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (fileAdapter.ViewHolder) view.getTag();
        }
        String text=mList.get(i);
        text=text.substring(1);

        viewHolder.mTextView.setText(text);
        if(mList.get(i).startsWith("0")) viewHolder.mImageView.setSelected(false);
        else viewHolder.mImageView.setSelected(true);
        view.setOnClickListener(new View.OnClickListener() {
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

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    class ViewHolder {
        TextView mTextView;
        ImageView mImageView;
    }

}
