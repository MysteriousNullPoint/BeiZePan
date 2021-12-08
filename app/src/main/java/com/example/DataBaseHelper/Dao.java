package com.example.DataBaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dao {
    private DbHelper helper;
    private SQLiteDatabase db;

    public Dao(Context context) {
        helper = new DbHelper(context);

    }

    //插入INSTRUMENT_ID表单
    public void insertInstruId(String id, String name) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("instrument_id", id);
        values.put("instrument_name", name);
        db.insert("INSTRUMENT_ID", null, values);

        db.close();
    }

    //插入INSTRUMENT_NAME表单
    public void insertInstruName(String name, String class_name, String abb) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("class_name", class_name);
        values.put("abbreviation", abb);
        db.insert("INSTRUMENT_NAME", null, values);
        db.close();
    }

    //验证STOCK表单是否正确
    public boolean checkStock(List<HashMap<String, String>> mlist) {
        boolean isRight = true;
        db = helper.getReadableDatabase();
        int i = 0;
        int stockSum;
        int inputSum;
        Cursor cursor = db.query("STOCK", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                stockSum = cursor.getInt(1);
                inputSum = Integer.parseInt(mlist.get(i).get("sum"));
                if (stockSum != inputSum) {
                    isRight = false;
                    break;
                }
                i = i + 1;
            }
        }
        db.close();
        return isRight;
    }

    //插入STOCK表单
    public void insertStock(List<HashMap<String, String>> mlist) {
        db = helper.getWritableDatabase();
        String sql = "DROP TABLE STOCK";
        db.execSQL(sql);
        sql = "create table STOCK(name string,sum integer,stock integer,scrap integer,stock_in integer,used integer)";
        db.execSQL(sql);
        String instrumentName;
        int sum, stock, scrap, stock_in, used, add_new;
        for (HashMap<String, String> map : mlist) {
            ContentValues values = new ContentValues();
            instrumentName = map.get("instrument_name");
            sum = Integer.parseInt(map.get("sum"));
            stock = Integer.parseInt(map.get("stock"));
            scrap = Integer.parseInt(map.get("scrap"));
            stock_in = Integer.parseInt(map.get("stock_in"));
            used = Integer.parseInt(map.get("used"));
            add_new = Integer.parseInt(map.get("add_new"));
            sum = sum + add_new;
            values.put("name", instrumentName);
            values.put("sum", sum);
            values.put("stock", stock);
            values.put("scrap", scrap);
            values.put("stock_in", stock_in);
            values.put("used", used);
            db.insert("STOCK", null, values);
        }
        db.close();
    }

    //插入CUPBOARD_ID表单
    public void insertCupboardId(String id, String name) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cupboard_id", id);
        values.put("cupboard_name", name);
        db.insert("CUPBOARD_ID", null, values);
        db.close();
    }

    public void insertLog(String date, String behave, String instrument_name, int number) {
        db = helper.getWritableDatabase();
        int stock = 0, sum = 0;
        Cursor cursor = db.query("STOCK", null, "name=?", new String[]{instrument_name}, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                stock = cursor.getInt(4);
                sum = cursor.getInt(2);
            }
        }
        ContentValues values = new ContentValues();
        values.put("datelog", date);
        values.put("behave", behave);
        values.put("instrument_name", instrument_name);
        cursor = db.query("DATELOG", null, "datelog=? AND behave=? AND instrument_name=?", new String[]{date, behave, instrument_name}, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                number = number + cursor.getInt(3);
            }
        }
        values.put("number", number);
        String setStock = Integer.toString(stock) + " / " + Integer.toString(sum);
        values.put("stock", setStock);
        db.delete("DATELOG", "datelog=? AND behave=? AND instrument_name=?", new String[]{date, behave, instrument_name});
        db.insert("DATELOG", null, values);
        cursor.close();
        db.close();
    }

    //读取instrument_id id列表
    public List<String> getIdFromInstru() {
        List<String> mList = new ArrayList<>();
        String id;
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("INSTRUMENT_ID", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                mList.add(id);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //读取instrument_id 映射表
    public HashMap<String, String> getMapFromInstru() {
        HashMap<String, String> map = new HashMap<>();
        String id, name;
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("INSTRUMENT_ID", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                name = cursor.getString(1);
                map.put(id, name);
            }
        }
        cursor.close();
        db.close();
        return map;
    }

    public List<String> getNameFromInstru(String abbreviation) {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT INSTRUMENT_NAME.name FROM INSTRUMENT_NAME " +
                "WHERE INSTRUMENT_NAME.abbreviation like'%" + abbreviation + "%' OR INSTRUMENT_NAME.name like'%" + abbreviation + "%'", null);
        if (cursor.getCount() != 0) {
            String name;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                mList.add(name);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //读取Cupboard_Stock表单


    public List<HashMap<String, String>> getCupStock(String cupboardName,int i) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("CUPBOARD_STOCK", null, "cupboard_name=?", new String[]{cupboardName}, null, null, null);
        if (cursor.getCount() != 0) {
            String instrument_name, sum, stock, scrap;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                instrument_name = cursor.getString(1);
                stock = Integer.toString(0);
                sum = Integer.toString(cursor.getInt(3));
                scrap = Integer.toString(cursor.getInt(4));
                map.put("instrument_name", instrument_name);
                map.put("sum", sum);
                map.put("stock", stock);
                map.put("scrap", scrap);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //读取stock表单
    public List<HashMap<String, String>> getStock() {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("STOCK", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            String name, sum, stock, scrap, stock_in, used;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                sum = Integer.toString(cursor.getInt(1));
                stock = Integer.toString(cursor.getInt(2));
                scrap = Integer.toString(cursor.getInt(3));
                stock_in = Integer.toString(cursor.getInt(4));
                used = Integer.toString(cursor.getInt(5));
                map.put("name", name);
                map.put("sum", sum);
                map.put("stock", stock);
                map.put("scrap", scrap);
                map.put("stock_in", stock_in);
                map.put("used", used);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public List<String> getCupName(String instrument_name) {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("CUPBOARD_STOCK", new String[]{"cupboard_name"}, "instrument_name=?", new String[]{instrument_name}, null, null, null);
        if (cursor.getCount() != 0) {
            String name, sum, stock, scrap;
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
                mList.add(name);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public String getCupNameById(String instrument_id){
        String cupName=null;
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("CUPBOARD_STATE", new String[]{"cupboard_name"}, "instrument_id=?", new String[]{instrument_id}, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                cupName= cursor.getString(0);
            }
        }
        cursor.close();
        db.close();
        return cupName;
    }


    //读取stock表单
    public List<HashMap<String, String>> getStock(String className) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT STOCK.* FROM STOCK, INSTRUMENT_NAME " +
                "WHERE STOCK.name = INSTRUMENT_NAME.name AND INSTRUMENT_NAME.class_name = '" + className + "'", null);
        if (cursor.getCount() != 0) {
            String name, sum, stock, scrap, stock_in, used;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                sum = Integer.toString(cursor.getInt(1));
                stock = Integer.toString(cursor.getInt(2));
                scrap = Integer.toString(cursor.getInt(3));
                stock_in = Integer.toString(cursor.getInt(4));
                used = Integer.toString(cursor.getInt(5));
                map.put("name", name);
                map.put("sum", sum);
                map.put("stock", stock);
                map.put("scrap", scrap);
                map.put("stock_in", stock_in);
                map.put("used", used);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //读取stock表单
    public List<HashMap<String, String>> getStock(List<String> searchName) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        for (String instruName : searchName) {
            Cursor cursor = db.query("STOCK", null, "name=?", new String[]{instruName}, null, null, null);
            if (cursor.getCount() != 0) {
                String name, sum, stock, stock_in;
                while (cursor.moveToNext()) {
                    HashMap<String, String> map = new HashMap<>();
                    name = instruName;
                    sum = Integer.toString(cursor.getInt(1));
                    stock = Integer.toString(cursor.getInt(2));
                    stock_in = Integer.toString(cursor.getInt(4));
                    map.put("name", name);
                    map.put("sum", sum);
                    map.put("stock", stock);
                    map.put("stock_in", stock_in);
                    mList.add(map);
                }
            }
            cursor.close();
        }
        db.close();
        return mList;
    }

    public List<HashMap<String, String>> getLog() {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("DATELOG", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            String name, datelog, number, stock, behave;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                datelog = cursor.getString(0);
                behave = cursor.getString(1);
                name = cursor.getString(2);
                number = cursor.getString(3);
                stock = cursor.getString(4);
                map.put("datelog", datelog);
                map.put("behave", behave);
                map.put("name", name);
                map.put("number", number);
                map.put("stock", stock);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public void insertFile(String fileType, String name) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("filetype", fileType);
        values.put("name", name);
        db.insert("FILE", null, values);
        db.close();
    }

    public List<String> getFile() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("FILE", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            String fileName;
            while (cursor.moveToNext()) {
                fileName = cursor.getString(1) + "0";
                mList.add(fileName);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public List<String> getFile(String name) {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("FILE", null, "filetype=?", new String[]{name}, null, null, null);
        if (cursor.getCount() != 0) {
            String fileName;
            while (cursor.moveToNext()) {
                fileName = cursor.getString(1) + "0";
                mList.add(fileName);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public void clearData() {
        db = helper.getWritableDatabase();
        String sql = "DROP TABLE FILE";
        db.execSQL(sql);
        sql = "create table if not exists FILE(filetype string,name string)";
        db.execSQL(sql);
        db.close();
    }

    public void removeData(String name) {
        db = helper.getWritableDatabase();
        db.delete("FILE", "name=?", new String[]{name});
        db.close();
    }

    public void clearAll() {
        db = helper.getWritableDatabase();
        String sql = "DROP TABLE CUPBOARD_ID";
        db.execSQL(sql);
        sql = "DROP TABLE INSTRUMENT_ID";
        db.execSQL(sql);
        sql = "DROP TABLE INSTRUMENT_NAME";
        db.execSQL(sql);
        sql = "DROP TABLE STOCK";
        db.execSQL(sql);
        sql = "DROP TABLE CUPBOARD_STOCK";
        db.execSQL(sql);
        sql = "DROP TABLE CUPBOARD_STATE";
        db.execSQL(sql);
        sql = "DROP TABLE DATELOG";
        db.execSQL(sql);
        sql = "DROP TABLE FILE";
        db.execSQL(sql);
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
        db.close();
    }
}
