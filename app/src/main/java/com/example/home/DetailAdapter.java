package com.example.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cupboard.R;
import com.example.guide.MyAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailAdapter extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList;

    public DetailAdapter(Context context, List<HashMap<String,String>> list) {
        mContext = context;
        mList = list;
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
        DetailAdapter.ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.home_cupboard_detail, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.home_detail_instru);
            viewHolder.tvStock=(TextView)view.findViewById(R.id.home_detail_stock);
            viewHolder.tvSum=(TextView)view.findViewById(R.id.home_detail_sum);
            viewHolder.tvScrap=(TextView)view.findViewById(R.id.home_detail_scrap);
            viewHolder.mButton = (ImageView) view.findViewById(R.id.home_button_scrap);
            view.setTag(viewHolder);
        } else {
            viewHolder = (DetailAdapter.ViewHolder) view.getTag();
        }
        viewHolder.tvName.setText(mList.get(i).get("instrument_name"));
        viewHolder.tvStock.setText(mList.get(i).get("stock"));
        viewHolder.tvSum.setText(mList.get(i).get("sum"));
        viewHolder.tvScrap.setText(mList.get(i).get("scrap"));
        viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
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
        TextView tvName;
        TextView tvStock;
        TextView tvSum;
        TextView tvScrap;
        ImageView mButton;
    }
}
