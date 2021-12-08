package com.example.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.R;
import com.example.stock.StockLocationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SearchInstruAdapter extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList;
    private MyDao myDao;

    public SearchInstruAdapter(Context context,List<HashMap<String,String>> list) {
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
        SearchInstruAdapter.ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new SearchInstruAdapter.ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.search_instru_adapter, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.search_name);
            viewHolder.tvStock = (TextView) view.findViewById(R.id.search_detail_stock);
            viewHolder.mButton = (ImageView) view.findViewById(R.id.search_add_address);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        String name=mList.get(i).get("name");
        viewHolder.tvName.setText(name);
        String stock =mList.get(i).get("stock");
        viewHolder.tvStock.setText(stock);

        viewHolder.gridView = view.findViewById(R.id.search_detail_location);
        myDao=new MyDao(mContext);
        List<String> mList=myDao.getCupName(name);
        Collections.sort(mList);
        List<String> locationList=new ArrayList<>();


        if(mList.size()<8){
            for(int t=0;t<(8-mList.size());t++){
                locationList.add("");
            }
            for(String str :mList){
                locationList.add(str);
            }
        }
        else locationList.addAll(mList);

        if(stock.startsWith("0")) viewHolder.mButton.setSelected(false);
        else viewHolder.mButton.setSelected(true);
        StockLocationAdapter adapter=new StockLocationAdapter(mContext,locationList);
        viewHolder.gridView.setAdapter(adapter);

        ViewHolder finalViewHolder = viewHolder;
        viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalViewHolder.mButton.isSelected()) mOnItemDeleteListener.onAddClick(i);
            }
        });
        return view;
    }

    /**
     * 删除按钮的监听接口
     */
    public interface onSearchAddListener {
        void onAddClick(int i);
    }

    private onSearchAddListener mOnItemDeleteListener;

    public void setOnSearchAddClickListener(onSearchAddListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }

    class ViewHolder {
        TextView tvName;
        GridView gridView;
        TextView tvStock;
        ImageView mButton;
    }


}
