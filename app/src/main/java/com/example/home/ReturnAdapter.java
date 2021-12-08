package com.example.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cupboard.R;

import java.util.HashMap;
import java.util.List;

public class ReturnAdapter extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList;

    public ReturnAdapter(Context context, List<HashMap<String,String>> list) {
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
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.home_instru_to_return, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.home_return_instrument);
            viewHolder.tvQuantiy=(TextView)view.findViewById(R.id.home_return_quantity);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvName.setText(mList.get(i).get("instrument_name"));
        viewHolder.tvQuantiy.setText(mList.get(i).get("quantity"));
        return view;
    }

    class ViewHolder {
        TextView tvName;
        TextView tvQuantiy;
    }
}
