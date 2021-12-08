package com.example.DataBaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.file.ExportData;
import com.example.stock.DetailAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyDao {
    private DataBaseHelper helper;
    private SQLiteDatabase db;

    public MyDao(Context context) {
        helper = new DataBaseHelper(context);
    }

    /***********
     CLASS_NAME表单
     **********/

    public List<String> getClassName() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT class_name FROM TOTAL ";
        Cursor cursor = db.rawQuery(sql, null);
        String id = null;
        mList.add("所有仪器");
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

    /******
     导入U盘数据（标签及库存更新）
     *****/
    //标签更新
    public void insertInstru(List<HashMap<String, String>> mList) {
        db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            //批量处理操作
            for (HashMap<String, String> map : mList) {
                ContentValues values = new ContentValues();
                values.put("instrument_id", map.get("instrument_id"));
                values.put("instrument_name", map.get("instrument_name"));
                db.insert("INSTRUMENT", null, values);
            }
            //设置事务标志为成功，当结束事务时就会提交事务
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            //结束事务
            db.endTransaction();
        }
        db.close();
    }

    //库存更新
    public void updateSum(List<HashMap<String, String>> mList) {
        db = helper.getWritableDatabase();
        String name;
        int sum;
        for (HashMap<String, String> map : mList) {
            ContentValues values = new ContentValues();
            name = map.get("instrument_name");
            sum = Integer.parseInt(map.get("sum"));
            values.put("sum", sum);
            db.update("TOTAL", values, "instrument_name=?", new String[]{name});
        }
    }

    //验证STOCK表单是否正确
    public boolean checkSum(List<Integer> mlist) {
        boolean isRight = true;
        db = helper.getReadableDatabase();
        int i = 0;
        int stockSum;
        int inputSum;
        Cursor cursor = db.query("TOTAL", new String[]{"sum"}, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                stockSum = cursor.getInt(0);
                inputSum = mlist.get(i);
                if (stockSum != inputSum) {
                    isRight = false;
                    break;
                }
                i = i + 1;
            }
        }
        cursor.close();
        db.close();
        return isRight;
    }

    //检查是否首次
    public boolean sumIsEmpty() {
        db = helper.getReadableDatabase();
        boolean isEmpty;
        long numRows = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM TOTAL", null);
        if (numRows > 0) isEmpty = false;
        else isEmpty = true;
        return isEmpty;
    }

    //验证LABEL是否重复
    public boolean checkSameLabel(String label) {
        boolean isSame = false;
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("INSTRUMENT", new String[]{"instrument_id"}, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                if (id.equals(label)) {
                    isSame = true;
                    break;
                }
            }
        }
        db.close();
        Log.i("test", " " + isSame);
        return isSame;
    }


    //插入总数
    public void insertTotal(ArrayList<HashMap<String, String>> mList) {
        db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (HashMap<String, String> map : mList) {
                ContentValues values = new ContentValues();
                values.put("instrument_name", map.get("instrument_name"));
                values.put("sum", Integer.parseInt(map.get("sum")));
                values.put("class_name", map.get("class_name"));
                values.put("scrap", 0);
                Log.i("test", "插入" + map.get("instrument_name") + "  " + map.get("sum"));
                db.insert("TOTAL", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            //结束事务
            db.endTransaction();
        }
        db.close();
    }

    //报废更新
    public void updateScrap(String instruName, int scrap) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        int scrap_after = 0;
        int sum_after=0;
        String sql = " SELECT sum,scrap FROM TOTAL WHERE instrument_name='" + instruName + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                sum_after=cursor.getInt(0)-scrap;
                scrap_after = cursor.getInt(1) + scrap;
                values.put("scrap", scrap_after);
                values.put("sum",sum_after);
            }
        }
        cursor.close();
        db.update("TOTAL", values, "instrument_name=?", new String[]{instruName});
        db.close();
    }

    public void updateCupboard(String instruName, List<String> removeList) {
        db = helper.getWritableDatabase();
        for (String instruId : removeList) {
            String sql = " DELETE FROM CUPBOARD WHERE instrument_id='" + instruId + "'";
            db.execSQL(sql);
            sql = " DELETE FROM INSTRUMENT WHERE instrument_id='" + instruId + "'";
            db.execSQL(sql);
        }
        String sql = "UPDATE CUPBOARD SET state=1 WHERE " +
                "instrument_id=( SELECT instrument_id FROM INSTRUMENT WHERE instrument_name='" + instruName + "')";
        db.execSQL(sql);
        db.close();
    }

    /*****
     录入
     *****/
    //获取所有柜子ID
    public List<String> getCupboardID() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT cupboard_id FROM CUPBOARD ";
        Cursor cursor = db.rawQuery(sql, null);
        String id = null;
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

    //获取柜子映射
    public HashMap<String, String> getCupMap() {
        HashMap<String, String> map = new HashMap<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT cupboard_id,cupboard_name FROM CUPBOARD ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id, name;
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

    /**
     * 未录入标签映射
     *
     * @return
     */
    public HashMap<String, String> getInstruMap() {
        HashMap<String, String> map = new HashMap<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id,instrument_name FROM INSTRUMENT WHERE NOT EXISTS " +
                "(SELECT instrument_id FROM CUPBOARD WHERE CUPBOARD.instrument_id=INSTRUMENT.instrument_id)";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id, name;
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

    /**
     * 获取所有标签映射
     *
     * @return
     */
    public HashMap<String, String> getAllInstruMap() {
        HashMap<String, String> map = new HashMap<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id,instrument_name FROM INSTRUMENT";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id, name;
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

    //插入表格数据
    public void insertCupboard(String cupboard_id, String cupboard_name, ArrayList<String> mList) {
        db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            //批量处理操作
            for (String instrument_id : mList) {
                ContentValues values = new ContentValues();
                values.put("cupboard_id", cupboard_id);
                values.put("cupboard_name", cupboard_name);
                values.put("instrument_id", instrument_id);
                values.put("state", 1);
                db.insert("CUPBOARD", null, values);
            }
            //设置事务标志为成功，当结束事务时就会提交事务
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            //结束事务
            db.endTransaction();
        }
        db.close();
    }

    //

    /**
     * 获取中文类别
     **/
    public List<String> getInstruTitle() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = "SELECT instrument_name FROM TOTAL";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String name;
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
                int i = name.length() - 1;
                for (; i > 0; i--) {
                    if ((name.charAt(i) >= 0x4e00) && (name.charAt(i) <= 0x9fbb)) break;
                }
                //String newName = name.replaceAll("[^\u4E00-\u9FA5]", "");
                mList.add(name.substring(0, i + 1));
            }
        }
        cursor.close();
        db.close();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(mList);
        ArrayList<String> newList = new ArrayList<>(hashSet);
        return newList;
    }

    /**
     * 首页
     **/
    //获取主页信息
    public HashMap<String, Integer> getHomeData() {
        HashMap<String, Integer> map = new HashMap<>();
        db = helper.getReadableDatabase();
        int sum = 0, scrap = 0, stock = 0, remain = 0;
        String sql = " SELECT sum(sum),sum(scrap) FROM TOTAL ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                sum = cursor.getInt(0);
                scrap = cursor.getInt(1);

            }
        }
        sql = " SELECT sum(state),count(*) FROM CUPBOARD ";
        cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                remain = cursor.getInt(0);
                stock = cursor.getInt(1);
            }
        }
        cursor.close();
        db.close();
        map.put("sum", sum);
        map.put("scrapped", scrap);
        map.put("borrowed", stock - remain);
        map.put("add_new", sum - stock);
        return map;
    }


    //获取柜子名
    public ArrayList<String> getCupName() {
        ArrayList<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT cupboard_name FROM CUPBOARD ";
        Cursor cursor = db.rawQuery(sql, null);
        String name;
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
                mList.add(name);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //获取指定名称的柜子ID
    public String getCupboardID(String cupName) {
        String cupID = "";
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT cupboard_id FROM CUPBOARD WHERE cupboard_name='" + cupName + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                cupID = cursor.getString(0);
            }
        }
        cursor.close();
        db.close();
        return cupID;
    }

    //获取柜子状态
    public ArrayList<String> getCupState() {
        ArrayList<String> nameList = getCupName();
        ArrayList<String> stateList = new ArrayList<>();
        db = helper.getReadableDatabase();
        for (String name : nameList) {
            int sum = 0, stock = 0;
            String sql = " SELECT sum(state),count(*) FROM CUPBOARD WHERE CUPBOARD.cupboard_name='" + name + "'";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    stock = cursor.getInt(0);
                    sum = cursor.getInt(1);
                }
            }

            if (sum == 0) stateList.add(name + "1");
            else {
                float a = (float) stock / sum;
                Log.i("test", a + "");
                if (a == 1) stateList.add(name + "9");
                else if (a >= 0.75) stateList.add(name + "7");
                else if (a >= 0.5) stateList.add(name + "5");
                else if (a >= 0.25) stateList.add(name + "3");
                else stateList.add(name + "1");
            }
            cursor.close();
        }
        db.close();
        return stateList;
    }

    //获取柜子详情
    public List<HashMap<String, String>> getCupStock(String cupboardName) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,sum(state),count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE cupboard_name='" + cupboardName + "' GROUP BY instrument_name ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String instruName, stock;
                instruName = cursor.getString(0);
                stock = Integer.toString(cursor.getInt(1));
                HashMap<String, String> map = new HashMap<>();
                map.put("instrument_name", instruName);
                map.put("stock", stock);
                map.put("sum", Integer.toString(cursor.getInt(2)));
                Cursor cursor1;
                sql = " SELECT scrap FROM TOTAL WHERE instrument_name='" + instruName + "'";
                cursor1 = db.rawQuery(sql, null);
                if (cursor1.getCount() != 0) {
                    while (cursor1.moveToNext()) {
                        String scrap = Integer.toString(cursor1.getInt(0));
                        map.put("scrap", scrap);
                    }
                }
                cursor1.close();
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public List<HashMap<String, String>> getCupStock(String cupboardName,int i) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE cupboard_name='" + cupboardName + "' GROUP BY instrument_name ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String instruName, stock;
                instruName = cursor.getString(0);
                HashMap<String, String> map = new HashMap<>();
                map.put("instrument_name", instruName);
                map.put("stock", "0");
                map.put("sum", Integer.toString(cursor.getInt(1)));
                Cursor cursor1;
                sql = " SELECT scrap FROM TOTAL WHERE instrument_name='" + instruName + "'";
                cursor1 = db.rawQuery(sql, null);
                if (cursor1.getCount() != 0) {
                    while (cursor1.moveToNext()) {
                        String scrap = Integer.toString(cursor1.getInt(0));
                        map.put("scrap", scrap);
                    }
                }
                cursor1.close();
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //获取柜子内设备ID
    public ArrayList<String> getIdFromCup(String cupName, String instruName) {
        ArrayList<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT CUPBOARD.instrument_id FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE INSTRUMENT.instrument_name='" + instruName + "' AND CUPBOARD.cupboard_name='" + cupName + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id;
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                mList.add(id);
                Log.i("test", id);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    //获取柜子内物品数量
    public Integer getInstruNum(String cupName, String instruName) {
        int number = 0;
        db = helper.getReadableDatabase();
        String sql = " SELECT count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE cupboard_name='" + cupName + "' AND instrument_name='" + instruName + "' GROUP BY instrument_name ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                number = cursor.getInt(0);
            }
        }
        cursor.close();
        db.close();
        return number;
    }

    /**
     * 获取未归还数据
     */
    public List<HashMap<String, String>> getReturnData(long mills) {
        List<HashMap<String, String>> mapList = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name, count(*) FROM LOG INNER JOIN INSTRUMENT ON LOG.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE date<'" + mills * 1000 + "'" + " GROUP BY instrument_name ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                map = new HashMap<>();
                map.put("instrument_name", cursor.getString(0));
                map.put("quantity", Integer.toString(cursor.getInt(1)));
                mapList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mapList;
    }

    /***
     搜索
     ****/
    public List<HashMap<String, String>> getSearchName(String searchName) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT instrument_name FROM TOTAL " +
                "WHERE instrument_name like'%" + searchName + "%'", null);
        if (cursor.getCount() != 0) {
            String name;
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
                String sql1 = " SELECT sum(state),count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                        "WHERE instrument_name='" + name + "' GROUP BY instrument_name ";
                Cursor cursor1 = db.rawQuery(sql1, null);
                int remain = 0, stock = 0;
                if (cursor1.getCount() != 0) {
                    while (cursor1.moveToNext()) {
                        remain = cursor1.getInt(0);
                        stock = cursor1.getInt(1);
                    }
                }
                String nowStock = remain + "/" + stock;
                HashMap<String, String> map = new HashMap<>();
                map.put("name", name);
                map.put("stock", nowStock);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public boolean isInstrumentID(String id) {
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id FROM INSTRUMENT WHERE instrument_id = '" + id + "'";
        boolean isID = false;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                Log.i("test", cursor.getString(0));
                if (id.equals(cursor.getString(0))) {
                    isID = true;
                    break;
                }
            }
        }
        cursor.close();
        db.close();
        return isID;
    }

    public void changeID(String oldID, String newID) {
        db = helper.getWritableDatabase();
        String sql = "UPDATE INSTRUMENT SET instrument_id = '" + newID + "'" + "WHERE instrument_id = '" + oldID + "'";
        db.execSQL(sql);
        sql = "UPDATE CUPBOARD SET instrument_id = '" + newID + "'" + "WHERE instrument_id = '" + oldID + "'";
        db.execSQL(sql);
        sql = "UPDATE LOG SET instrument_id = '" + newID + "'" + "WHERE instrument_id = '" + oldID + "'";
        db.execSQL(sql);
        db.close();
    }

    /***
     库存
     ** */
    public List<String> getCupName(String instrument_name) {
        List<String> cupList = new ArrayList<>();
        String cupName;
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT cupboard_name FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE instrument_name='" + instrument_name + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                cupName = cursor.getString(0);
                cupList.add(cupName);
            }
        }
        cursor.close();
        db.close();
        return cupList;
    }


    public List<HashMap<String, String>> getStock() {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,sum,scrap FROM TOTAL ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String name;
            int stock, scrap, remain, input, sum;
            while (cursor.moveToNext()) {
                stock = 0;
                scrap = 0;
                remain = 0;
                input = 0;
                sum = 0;
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                sum = cursor.getInt(1);
                scrap = cursor.getInt(2);
                String sql1 = " SELECT sum(state),count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                        "WHERE instrument_name='" + name + "' GROUP BY instrument_name ";
                Cursor cursor1 = db.rawQuery(sql1, null);
                if (cursor1.getCount() != 0) {
                    while (cursor1.moveToNext()) {
                        remain = cursor1.getInt(0);
                        stock = cursor1.getInt(1);
                    }
                }
                input = sum - stock;
                cursor1.close();
                map.put("name", name);
                map.put("stock", Integer.toString(stock));
                map.put("scrap", Integer.toString(scrap));
                map.put("stock_in", Integer.toString(remain));
                map.put("input", Integer.toString(input));
                mList.add(map);
            }
        }
        return mList;
    }

    public List<HashMap<String, String>> getStock(String className) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,sum,scrap FROM TOTAL Where class_name='" + className + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String name;
            int stock, scrap, remain, input, sum;
            while (cursor.moveToNext()) {
                stock = 0;
                scrap = 0;
                remain = 0;
                input = 0;
                sum = 0;
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                sum = cursor.getInt(1);
                scrap = cursor.getInt(2);
                String sql1 = " SELECT sum(state),count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                        "WHERE instrument_name='" + name + "' GROUP BY instrument_name ";
                Cursor cursor1 = db.rawQuery(sql1, null);
                if (cursor1.getCount() != 0) {
                    while (cursor1.moveToNext()) {
                        remain = cursor1.getInt(0);
                        stock = cursor1.getInt(1);
                    }
                }
                input = sum - stock;
                cursor1.close();
                map.put("name", name);
                map.put("stock", Integer.toString(stock));
                map.put("scrap", Integer.toString(scrap));
                map.put("stock_in", Integer.toString(remain));
                map.put("input", Integer.toString(input));
                mList.add(map);
            }
        }
        return mList;
    }

    //获取已经录入的设备ID
    public HashMap<String, String> getStockInstruMap() {
        HashMap<String, String> map = new HashMap<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id,instrument_name FROM INSTRUMENT WHERE EXISTS " +
                "(SELECT instrument_id FROM CUPBOARD WHERE CUPBOARD.instrument_id=INSTRUMENT.instrument_id)";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id, name;
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

    /***
     盘点
     ****/
    public List<String> getStockInstruMap(String instrumentName) {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id FROM INSTRUMENT WHERE EXISTS " +
                "(SELECT instrument_id FROM CUPBOARD WHERE CUPBOARD.instrument_id=INSTRUMENT.instrument_id AND instrument_name='" + instrumentName + "')";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id;
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                mList.add(id);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public List<String> getStockIn() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id FROM INSTRUMENT WHERE EXISTS " +
                "(SELECT instrument_id FROM CUPBOARD WHERE CUPBOARD.instrument_id=INSTRUMENT.instrument_id )";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String id;
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                mList.add(id);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public HashMap<String, Integer> getInventoryDetail(String instruName) {
        HashMap<String, Integer> map = new HashMap<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT sum(state),count(*) FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE instrument_name='" + instruName + "' GROUP BY instrument_name ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                int remain, stock;
                remain = cursor.getInt(0);
                stock = cursor.getInt(1);
                map.put("remain", remain);
                map.put("stock", stock);
            }
        }
        cursor.close();
        db.close();
        return map;
    }

    public String getStockSum() {
        db = helper.getReadableDatabase();
        int sum = 0;
        String sql = " SELECT count(*) FROM CUPBOARD";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                sum = cursor.getInt(0);
            }
        }
        cursor.close();
        db.close();
        return Integer.toString(sum);
    }

    /***
     门禁
     ** */
    public String getCupNameById(String id) {
        String cupName = "";
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT cupboard_name FROM CUPBOARD Where instrument_id ='" + id + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                cupName = cursor.getString(0);
            }
        }
        cursor.close();
        db.close();
        return cupName;
    }

    public void updateCupboard(int a, List<String> mList) {
        db = helper.getWritableDatabase();
        String sql;
        if (a == 0) {
            for (String id : mList) {
                sql = "UPDATE CUPBOARD SET state = 0 WHERE instrument_id = '" + id + "'";
                db.execSQL(sql);
            }
        } else {
            for (String id : mList) {
                sql = "UPDATE CUPBOARD SET state = 1 WHERE instrument_id = '" + id + "'";
                db.execSQL(sql);
            }
        }
        db.close();
    }

    public void updateLog(int a, long date, List<String> mList) {
        db = helper.getWritableDatabase();
        if (a == 0) {
            for (String id : mList) {
                ContentValues values = new ContentValues();
                values.put("date", date);
                values.put("instrument_id", id);
                db.insert("LOG", null, values);
            }
        } else {
            for (String id : mList) {
                String sql = " DELETE FROM LOG WHERE instrument_id='" + id + "'";
                db.execSQL(sql);
            }
        }
        db.close();
    }

    public int getStockDetail(String name) {
        String sql = " SELECT sum(state)FROM CUPBOARD INNER JOIN INSTRUMENT ON CUPBOARD.instrument_id = INSTRUMENT.instrument_id " +
                "WHERE instrument_name='" + name + "' GROUP BY instrument_name ";
        Cursor cursor = db.rawQuery(sql, null);
        int remain = 0;
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                remain = cursor.getInt(0);
            }
        }
        cursor.close();
        return remain;
    }

    public void insertSheet(String behave, String name, int number, String admin) {
        long timeseconds = System.currentTimeMillis();
        long oldtimeseconds = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String Date = sdf.format(timeseconds);
        db = helper.getWritableDatabase();
        boolean isNew = true;
        String sql = " SELECT date,number FROM SHEET WHERE behave='" + behave + "' AND instrument_name='" + name + "' AND date('now','localtime')='" + Date + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                number = number + cursor.getInt(1);
                isNew = false;
                oldtimeseconds = cursor.getLong(0);
            }
        }
        cursor.close();
        int stock = getStockDetail(name);
        ContentValues values = new ContentValues();
        values.put("number", number);
        values.put("date", timeseconds);
        values.put("behave", behave);
        values.put("instrument_name", name);
        values.put("stock", stock);
        values.put("admin", admin);


        if (isNew) {
            db.insert("SHEET", null, values);
        } else {
            db.update("SHEET", values, "date = ? AND behave = ? AND instrument_name = ?", new String[]{String.valueOf(oldtimeseconds), behave, name});

        }
        db.close();
    }

    public void insertBorrowSheet(long date, String behave, String name, int number, String admin) {
        db = helper.getWritableDatabase();
        int stock = getStockDetail(name);
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("behave", behave);
        values.put("instrument_name", name);
        values.put("number",number);
        values.put("stock", stock);
        values.put("admin", admin);
        db.insert("SHEET", null, values);
        db.close();
    }


    public List<HashMap<String, String>> getSheet() {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("SHEET", null, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            long datelog;
            String name, number, stock, behave, admin;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                datelog = cursor.getLong(0);
                behave = cursor.getString(1);
                name = cursor.getString(2);
                number = cursor.getString(3);
                stock = cursor.getString(4);
                admin = cursor.getString(5);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                String Date = sdf.format(datelog);
                map.put("datelog", Date);
                map.put("behave", behave);
                map.put("name", name);
                map.put("number",number);
                map.put("stock", stock);
                map.put("admin", admin);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public List<HashMap<String, String>> getScrap() {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,scrap FROM TOTAL WHERE scrap!=0";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String name, scrap;
            while (cursor.moveToNext()) {
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                scrap = cursor.getString(1);
                map.put("name", name);
                map.put("scrap", scrap);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public List<HashMap<String, String>> getScrap(String className) {
        List<HashMap<String, String>> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,scrap FROM TOTAL WHERE scrap!=0 AND class_name='" + className + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String name, scrap;
            while (cursor.moveToNext()) {
                Log.i("test", "faxian");
                HashMap<String, String> map = new HashMap<>();
                name = cursor.getString(0);
                scrap = cursor.getString(1);
                map.put("name", name);
                map.put("scrap", scrap);
                mList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    /***
     * 数据
     */
    public List<String> getInstruName() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT DISTINCT instrument_name FROM INSTRUMENT INNER JOIN CUPBOARD" +
                " WHERE INSTRUMENT.instrument_id = CUPBOARD.instrument_id";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String name;
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
                mList.add(name);
                Log.i("maintain","getinstru"+name);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public void insertMaintain(String name) {
        db = helper.getWritableDatabase();
        boolean isNew = true;
        String sql = " SELECT instrument_name FROM MAINTAIN WHERE instrument_name = '" + name + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                isNew = false;
            }
        }
        cursor.close();
        if (isNew) {
            ContentValues values = new ContentValues();
            values.put("instrument_name", name);
            values.put("maintain_days", 0);
            values.put("maintain_time ", System.currentTimeMillis());
            db.insert("MAINTAIN", null, values);
            Log.i("maintain","insert"+name);
        }
        db.close();
    }

    public HashMap<String, Object> getMaintain(String name) {
        db = helper.getReadableDatabase();
        HashMap<String, Object> map = new HashMap<>();
        String sql = " SELECT maintain_days,maintain_time FROM MAINTAIN WHERE instrument_name = '" + name + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                map.put("maintain_days", cursor.getInt(0));
                map.put("maintain_time", cursor.getLong(1));
            }
        }
        cursor.close();
        db.close();
        return map;
    }

    public void updateMaintain(String name, int days) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("maintain_days", days);
        db.update("MAINTAIN", values, "instrument_name=?", new String[]{name});
    }

    public void updateMaintain(String name){
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        long seconds=System.currentTimeMillis();
        values.put("maintain_time", seconds);
        db.update("MAINTAIN", values, "instrument_name=?", new String[]{name});
    }

    public int getMaintainData(){
        db = helper.getReadableDatabase();
        String sql = " SELECT maintain_days,maintain_time FROM MAINTAIN";
        Cursor cursor = db.rawQuery(sql, null);
        int i=0;
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                long timeSeconds=System.currentTimeMillis();
                long time_last=cursor.getLong(1);
                long duringSeconds=(timeSeconds-time_last)/1000;
                long passSeconds=cursor.getInt(0)*24*3600;
                if(passSeconds!=0 && duringSeconds>passSeconds) i=i+1;
            }
        }
        cursor.close();
        db.close();
        Log.i("maintain",""+i);
        return i;

    }

    public List<Integer> getChartData1(int a, List<String> mList) {
        db = helper.getReadableDatabase();
        List<Integer> numerList = new ArrayList<>();
        if (a == 0) {
            for (String name : mList) {
                String sql = " SELECT sum(sum) FROM TOTAL WHERE class_name='" + name + "'";
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        numerList.add(cursor.getInt(0));
                    }
                }
                cursor.close();
            }
        } else if (a == 1) {
            for (String name : mList) {
                String sql = " SELECT count(*) FROM TOTAL INNER JOIN INSTRUMENT INNER JOIN CUPBOARD ON INSTRUMENT.instrument_name=TOTAL.instrument_name" +
                        " AND INSTRUMENT.instrument_id=CUPBOARD.instrument_id WHERE class_name='" + name + "'";
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        numerList.add(cursor.getInt(0));
                    }
                }
                cursor.close();
            }
        } else {
            for (String name : mList) {
                String sql = " SELECT sum(scrap) FROM TOTAL WHERE class_name='" + name + "'";
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        numerList.add(cursor.getInt(0));
                    }
                }
                cursor.close();
            }
        }
        db.close();
        return numerList;
    }

    public List<Map<String, Object>> getChartData2() {
        List<Map<String, Object>> mapList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT class_name,sum(number) FROM SHEET INNER JOIN TOTAL ON TOTAL.instrument_name = SHEET.instrument_name" +
                " WHERE behave= '借用' GROUP BY class_name ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", cursor.getString(0));
                map.put("count", cursor.getInt(1));
                mapList.add(map);
            }
        }
        cursor.close();
        db.close();
        return mapList;
    }

    public List<Integer> getChartData3(String name) {
        List<Integer> newList = new ArrayList<>();
        db = helper.getReadableDatabase();
        int number = 0;
        String sql = " SELECT count(*) FROM SHEET INNER JOIN TOTAL ON TOTAL.instrument_name=SHEET.instrument_name " +
                "WHERE class_name='" + name + "' AND behave='归还' AND date >= strftime('%s','now','start of year','start of month')*1000 AND" +
                " date <= strftime('%s','now','start of year','start of month','start of month','+1 month')*1000 -10000";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                number = cursor.getInt(0);
            }
        }
        newList.add(number);


        for (int i = 1; i < 12; i++) {
            number = 0;
            sql = " SELECT count(*),date,strftime('%s','now','start of year','start of month','+" + i + " month')FROM SHEET INNER JOIN TOTAL ON TOTAL.instrument_name=SHEET.instrument_name " +
                    "WHERE class_name='" + name + "' AND behave='借用' AND date >= strftime('%s','now','start of year','start of month','+" + i + " month')*1000 AND" +
                    " date <= strftime('%s','now','start of year','start of month','+" + (i + 1) + " month')*1000 -10000";
            cursor = db.rawQuery(sql, null);
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    number = cursor.getInt(0);
                }
            }
            newList.add(number);
        }


        cursor.close();
        db.close();
        return newList;
    }

    /***
     * 文件
     */
    public void insertFile(String fileType, String name) {
        db = helper.getWritableDatabase();
        String sql = " DELETE FROM FILE WHERE file_name='" + name + "'";
        db.execSQL(sql);
        ContentValues values = new ContentValues();
        values.put("file_type", fileType);
        values.put("file_name", name);
        db.insert("FILE", null, values);
        db.close();
    }

    public List<String> getFile() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT file_name FROM FILE ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            String fileName;
            while (cursor.moveToNext()) {
                fileName = "0" + cursor.getString(0);
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
        Cursor cursor = db.query("FILE", null, "file_type=?", new String[]{name}, null, null, null);
        if (cursor.getCount() != 0) {
            String fileName;
            while (cursor.moveToNext()) {
                fileName = "0" + cursor.getString(1);
                mList.add(fileName);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public void removeAll() {
        db = helper.getWritableDatabase();
        String sql = "DROP TABLE if exists FILE";
        db.execSQL(sql);
        sql = "create table if not exists FILE(file_type string,file_name string)";
        db.execSQL(sql);
        db.close();
    }

    public void removeAll(String fileType) {
        db = helper.getWritableDatabase();
        String sql = " DELETE FROM FILE WHERE file_type='" + fileType + "'";
        db.execSQL(sql);
        db.close();
    }

    public void remove(String fileName) {
        db = helper.getWritableDatabase();
        fileName = fileName.substring(1);
        String sql = " DELETE FROM FILE WHERE file_name='" + fileName + "'";
        db.execSQL(sql);
        db.close();
    }

    public List<ExportData> exportStock() {
        List<ExportData> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_name,class_name,sum FROM TOTAL";
        Cursor cursor = db.rawQuery(sql, null);
        String name, class_name, sum;
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
                class_name = cursor.getString(1);
                sum = Integer.toString(cursor.getInt(2));
                ExportData exportData = new ExportData("模板", name, class_name, sum);
                mList.add(exportData);
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    /***
     * 设置
     */
    public void ResetClass(List<String> mlist) {
        db = helper.getWritableDatabase();
        String sql = "DROP TABLE if exists CLASS";
        db.execSQL(sql);
        sql = "create table if not exists CLASS(class_name string)";
        db.execSQL(sql);
        for (String str : mlist) {
            ContentValues values = new ContentValues();
            values.put("Class_Name", str);
            //数据库执行插入命令
            db.insert("CLASS", null, values);
        }
        db.close();
    }

    public List<String> getAdminName() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT name FROM ADMIN ";
        Cursor cursor = db.rawQuery(sql, null);
        String id = null;
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

    public void insertAdmin(String ID, String name) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", ID);
        values.put("name", name);
        db.insert("ADMIN", null, values);
        db.close();
    }

    public void ResetAdmin(List<String> mlist) {
        db = helper.getWritableDatabase();
        for (String name : mlist) {
            db.delete("ADMIN", "name=?", new String[]{name});
        }
        db.close();
    }

    public List<String> getAdminID() {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT id FROM ADMIN ";
        Cursor cursor = db.rawQuery(sql, null);
        String id = null;
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

    public String getAdminName(String ID) {
        db = helper.getReadableDatabase();
        String sql = " SELECT name FROM ADMIN WHERE id='" + ID + "'";
        Cursor cursor = db.rawQuery(sql, null);
        String name = null;
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                name = cursor.getString(0);
            }
        }
        cursor.close();
        db.close();
        return name;
    }


    /***
     * 同步
     */

    /**
     * 插入同步数据
     *
     * @param type:1.插入标签2.删除标签3.插入admin4.删除admin
     * @param mList
     */
    public void insertSync(int type, List<String> mList) {
        db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            //批量处理操作
            for (String id : mList) {
                ContentValues values = new ContentValues();
                values.put("type", type);
                values.put("id", id);
                db.insert("DOCKSYNC", null, values);
            }
            //设置事务标志为成功，当结束事务时就会提交事务
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            //结束事务
            db.endTransaction();
        }
        db.close();
    }

    /**
     * 获取同步数据
     *
     * @param i:1.插入标签2.删除标签3.插入admin4.删除admin
     * @return
     */
    public List<String> getSync(int i) {
        List<String> mList = new ArrayList<>();
        db = helper.getReadableDatabase();
        String sql = " SELECT id FROM DOCKSYNC WHERE type = " + i;
        Cursor cursor = db.rawQuery(sql, null);
        String id = null;
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

    public void resetSync(){
        db = helper.getWritableDatabase();
        String sql =  "DROP TABLE if exists DOCKSYNC";
        db.execSQL(sql);
        //创建数据备份
        sql = "create table if not exists DOCKSYNC(type int,id string)";
        db.execSQL(sql);
        db.close();
    }

    public List<String> printData() {
        //test();
        List<String> mList = new ArrayList<>();
        mList.add("aa");
        /*
        List<HashMap<String,Integer>> mapList=getHomeData();

        for(HashMap<String,Integer> map:mapList){
            mList.add(map.get("sum")+"  "+map.get("scrap"));
        }*/
        /*
        HashMap<String, String> map = getInstruMap();
        for (int s : list) {
            mList.add("sum = " +s);
        }

         */
        db = helper.getReadableDatabase();
        String sql = " SELECT instrument_id FROM INSTRUMENT ";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                mList.add(cursor.getString(0));
            }
        }
        cursor.close();
        db.close();
        return mList;
    }

    public void clearAll() {
        db = helper.getWritableDatabase();
        String sql = "DROP TABLE if exists CUPBOARD";
        db.execSQL(sql);
        sql = "DROP TABLE if exists INSTRUMENT";
        db.execSQL(sql);
        sql = "DROP TABLE if exists LOG";
        db.execSQL(sql);
        sql = "DROP TABLE if exists SHEET";
        db.execSQL(sql);
        sql = "DROP TABLE if exists TOTAL";
        db.execSQL(sql);
        sql = "DROP TABLE if exists ADMIN";
        db.execSQL(sql);
        sql = "DROP TABLE if exists FILE";
        db.execSQL(sql);
        sql = "DROP TABLE if exists DOCKSYNC";
        db.execSQL(sql);
        sql = "DROP TABLE if exists MAINTAIN";
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
        //创建借还日志表
        sql = "create table if not exists LOG(date long,instrument_id string)";
        db.execSQL(sql);
        //创建数据报表
        sql = "create table if not exists SHEET(date long,behave string,instrument_name string,number integer,stock string,admin string)";
        db.execSQL(sql);
        //创建管理员表单
        sql = "create table if not exists ADMIN(id string,name string)";
        db.execSQL(sql);
        //创建文件表单
        sql = "create table if not exists FILE(file_type string,file_name string)";
        db.execSQL(sql);
        //创建数据备份
        sql = "create table if not exists DOCKSYNC(type int,id string)";
        db.execSQL(sql);
        //创建保养时间表
        sql = "create table if not exists MAINTAIN(instrument_name string,maintain_days int,maintain_time long)";
        db.execSQL(sql);
        db.close();
    }

    public void testDate() {
        db = helper.getWritableDatabase();
        long timeSeconds = System.currentTimeMillis();
        String sql = " SELECT strftime('%s','now','start of year','start of month','start of month','+7 month')*1000 < 1628972837574";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                Log.i("datelog", "" + cursor.getString(0));
                Log.i("datelog", timeSeconds + "");
            }
        }

    }
}
