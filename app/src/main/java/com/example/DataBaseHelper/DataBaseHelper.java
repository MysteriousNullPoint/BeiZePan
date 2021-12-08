package com.example.DataBaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    //构造方法：第1参数为上下文，第2参数库库名，第3参数为游标工厂，第4参数为版本
    public DataBaseHelper(Context context) {
        super(context, "MyDataBase", null, 1);  //创建或打开数据库
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //新建管理员表单
        String sql = "create table if not exists ADMIN(id string,name string)";
        db.execSQL(sql);
        //创建标签ID表单
        sql = "create table if not exists INSTRUMENT(instrument_id string,instrument_name string)";
        db.execSQL(sql);
        //创建柜子表单
        sql = "create table if not exists CUPBOARD(cupboard_id string,cupboard_name string,instrument_id string,state integer)";
        db.execSQL(sql);
        //创建物品总数表单
        sql = "create table if not exists TOTAL(instrument_name string,class_name string,sum integer,scrap integer)";
        db.execSQL(sql);
        //创建日志表
        sql = "create table if not exists LOG(date long,instrument_id string)";
        db.execSQL(sql);
        //创建报表
        sql = "create table if not exists SHEET(date long,behave string,instrument_name string,number integer,stock string,admin string)";
        db.execSQL(sql);
        //创建文件表单
        sql = "create table if not exists FILE(file_type string,file_name string)";
        db.execSQL(sql);
        //创建数据备份
        sql = "create table if not exists DATASYNC(type int,id string)";
        db.execSQL(sql);
        //创建保养时间表
        sql = "create table if not exists MAINTAIN(instrument_name string,maintain_days int,maintain_time long)";
        db.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
