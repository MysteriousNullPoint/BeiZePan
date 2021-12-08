package com.example.cupboard;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.DataBaseHelper.MyDao;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

public class Admin {
    private Context mContext;
    private MyDao myDao;
    private List<String> tempList;

    public Admin(Context context) {
        mContext = context;
        myDao = new MyDao(mContext);
        tempList = myDao.getAdminID();
    }

    public boolean CheckSameCard(String Label_ID) {
        boolean flag = false;
        for (String id : tempList) {
            if (id.equals(Label_ID)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void insertAdmin(String id, String name) {
        myDao.insertAdmin(id, name);
    }

    public List<String> getAdminName() {
        List<String> mlist = myDao.getAdminName();
        return mlist;
    }

    public void DeleteAdmin(List<String> mList) {
        myDao.ResetAdmin(mList);
    }

    public void removeTemp(int i) {
        tempList.remove(i);
    }

    public void addTemp(String id) {
        tempList.add(id);
        for (String test : tempList) {
            Log.i("id", test);
        }
    }


}
