package com.example.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cupboard.R;

import java.util.HashMap;
import java.util.List;

public class DataScrapAdapter extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList;
    public DataScrapAdapter(Context context, List<HashMap<String,String>> list) {
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
        DataScrapAdapter.ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new DataScrapAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.data_scrap_adapter, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.data_scrap_instru);
            viewHolder.tvScrap=(TextView)view.findViewById(R.id.data_scrap_count);
            viewHolder.tvDate=(TextView)view.findViewById(R.id.data_scrap_date);
            viewHolder.tvBehave=(TextView)view.findViewById(R.id.data_scrap_behave);

            view.setTag(viewHolder);
        } else {
            viewHolder = (DataScrapAdapter.ViewHolder) view.getTag();
        }
        viewHolder.tvName.setText(mList.get(i).get("name"));
        viewHolder.tvDate.setText(mList.get(i).get("datelog"));
        viewHolder.tvBehave.setText(mList.get(i).get("behave"));
        if(mList.get(i).get("stock")==null){
            viewHolder.tvScrap.setText(mList.get(i).get("scrap"));
        }
        else{
            viewHolder.tvScrap.setText(mList.get(i).get("stock"));
        }
        return view;
    }


    class ViewHolder {
        TextView tvName;
        TextView tvScrap;
        TextView tvDate;
        TextView tvBehave;
    }


}
