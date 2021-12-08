package com.example.dock;

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
import java.util.HashMap;
import java.util.List;

public class AddAdapter extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList = new ArrayList<>();

    public AddAdapter(Context context, List<HashMap<String,String>> list) {
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
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.dock_add_adapter, null);
            viewHolder.mName= (TextView) view.findViewById(R.id.dock_add_item);
            viewHolder.mID=(TextView)view.findViewById(R.id.dock_add_item_id);
            viewHolder.mButton = (ImageView) view.findViewById(R.id.dock_add_delete);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.mName.setText(mList.get(i).get("name"));
        viewHolder.mID.setText("("+mList.get(i).get("id")+")");
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

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    class ViewHolder {
        TextView mName;
        TextView mID;
        ImageView mButton;
    }

}


