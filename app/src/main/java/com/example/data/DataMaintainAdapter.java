package com.example.data;

import android.content.Context;
import android.media.Image;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.R;
import com.example.guide.MyAdapter;

import java.util.HashMap;
import java.util.List;

public class DataMaintainAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mList;
    private MyDao myDao;
    private int day=0;
    public DataMaintainAdapter(Context context, List<String> list) {
        mContext = context;
        mList = list;
        myDao=new MyDao(context);
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
            view = LayoutInflater.from(mContext).inflate(R.layout.data_maintain_adapter, null);
            viewHolder.tvName = (TextView) view.findViewById(R.id.data_maintain_instru);
            viewHolder.editText=(EditText) view.findViewById(R.id.data_maintain_et);
            viewHolder.btnDate=(ImageView)view.findViewById(R.id.data_maintain_btn);
            viewHolder.state=view.findViewById(R.id.data_maintain_state);
            viewHolder.btnMaintain=view.findViewById(R.id.data_maintain_go);
            view.setTag(viewHolder);
        } else {
            viewHolder = (DataMaintainAdapter.ViewHolder) view.getTag();
        }

        String name=mList.get(i);
        viewHolder.tvName.setText(name);
        HashMap<String,Object> map=myDao.getMaintain(name);
        Log.i("maintain",name);
        day=(Integer)map.get("maintain_days");
        viewHolder.editText.setText(Integer.toString(day));
        //viewHolder.editText.setTag(i);
        viewHolder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newDays=s+"";
                if(!newDays.equals("")){day=Integer.parseInt(newDays);}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        viewHolder.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDao.updateMaintain(name,day);
            }
        });
        long timeSeconds=System.currentTimeMillis();
        long duringSeconds=(timeSeconds- (long)map.get("maintain_time"))/1000;
        long passSeconds=day*24*3600;
        boolean maintain=duringSeconds>passSeconds;
        if(day==0) {
            viewHolder.state.setSelected(false);
        }
        else {
            viewHolder.state.setSelected(maintain);
        }
        viewHolder.btnMaintain.setOnClickListener(new View.OnClickListener() {
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
        EditText editText;
        ImageView btnDate;
        ImageView state;
        ImageView btnMaintain;
    }


}
