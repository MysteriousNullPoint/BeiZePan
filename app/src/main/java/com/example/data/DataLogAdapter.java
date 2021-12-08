package com.example.data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cupboard.R;

import java.util.HashMap;
import java.util.List;

public class DataLogAdapter extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList;
    public DataLogAdapter(Context context, List<HashMap<String,String>> list) {
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
        DataLogAdapter.ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new DataLogAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.data_log_adapter, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.data_log_instru);
            viewHolder.tvstock=(TextView)view.findViewById(R.id.data_log_stock);
            viewHolder.tvDate=(TextView)view.findViewById(R.id.data_log_date);
            viewHolder.tvBehave=(TextView)view.findViewById(R.id.data_log_behave);
            viewHolder.tvAdmin=(TextView)view.findViewById(R.id.data_log_admin);
            view.setTag(viewHolder);
        } else {
            viewHolder = (DataLogAdapter.ViewHolder) view.getTag();
        }
        viewHolder.tvName.setText(mList.get(i).get("name"));
        viewHolder.tvDate.setText(mList.get(i).get("datelog"));
        viewHolder.tvBehave.setText(mList.get(i).get("behave"));
        viewHolder.tvstock.setText(mList.get(i).get("number")+"/"+mList.get(i).get("stock"));
        viewHolder.tvAdmin.setText(mList.get(i).get("admin"));
        if(mList.get(i).get("behave").equals("借用")) viewHolder.tvBehave.setTextColor(Color.RED);
        else if(mList.get(i).get("behave").equals("归还")) viewHolder.tvBehave.setTextColor(Color.parseColor("#3399FE"));
        else if(mList.get(i).get("behave").equals("报废")) viewHolder.tvBehave.setTextColor(Color.RED);
        else viewHolder.tvBehave.setTextColor(Color.parseColor("#006633"));
        return view;
    }


    class ViewHolder {
        TextView tvName;
        TextView tvstock;
        TextView tvDate;
        TextView tvBehave;
        TextView tvAdmin;
    }


}
