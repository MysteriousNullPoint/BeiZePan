package com.example.DataBaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    //构造方法：第1参数为上下文，第2参数库库名，第3参数为游标工厂，第4参数为版本
    public DbHelper(Context context) {
        super(context, "DataBase", null, 1);  //创建或打开数据库
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //新建类别表单
        String sql = "create table if not exists CLASS_NAME(class_name string)";
        db.execSQL(sql);
        //创建标签ID表单
        sql = "create table if not exists INSTRUMENT_ID(instrument_id string,instrument_name string)";
        db.execSQL(sql);
        //创建物品属性表单
        sql = "create table if not exists INSTRUMENT_NAME(name string,class_name string,abbreviation string)";
        db.execSQL(sql);
        //创建库存表单
        sql = "create table if not exists STOCK(name string,sum integer,stock integer,scrap integer,stock_in integer,used integer)";
        db.execSQL(sql);
        //创建柜子ID表单
        sql = "create table if not exists CUPBOARD_ID(cupboard_id string,cupboard_name string)";
        db.execSQL(sql);
        //创建柜子库存表单
        sql = "create table if not exists CUPBOARD_STATE(cupboard_name string,instrument_id string,instrument_name string,stock_state string)";
        db.execSQL(sql);
        //创建柜子
        sql = "create table if not exists CUPBOARD_STOCK(cupboard_name string,instrument_name string,stock integer,sum integer,scrap integer)";
        db.execSQL(sql);
        sql = "create table if not exists DATELOG(datelog string,behave string,instrument_name string,number integer,stock integer)";
        db.execSQL(sql);
        sql = "create table if not exists FILE(filetype string,name string)";
        db.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}