package com.example.cupboard;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author wangzhao
 * @date 12/5/21 10:06 PM
 * description 描述
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "0c33eb6e73", false);
    }
}
