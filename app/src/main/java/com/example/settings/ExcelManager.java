package com.example.settings;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

public class ExcelManager {
    Context mContext;
    MyDao myDao;

    private boolean isSuccess=false;

    public ExcelManager(Context context){
        mContext=context;
        myDao=new MyDao(mContext);
    }

    public void readExcel(){
        String labelPath = "/mnt/media_rw/usbotg/映射模板.xls";
        String stockPath = "/mnt/media_rw/usbotg/库存模板.xls";
        File label_file = new File(labelPath);
        File stock_file = new File(stockPath);
        if (label_file.canRead() && stock_file.canRead()) {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding("GB2312");
            try {
                InputStream is = new FileInputStream(label_file);
                Workbook book = Workbook.getWorkbook(is, workbookSettings);
                Sheet sheet = book.getSheet(0);
                int Rows = sheet.getRows();
                List<HashMap<String, String>> mapList = new ArrayList<>();
                if (Rows > 1) {
                    if (!myDao.checkSameLabel(sheet.getCell(0, 1).getContents())) {
                        for (int i = 1; i < Rows; i++) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("instrument_id", sheet.getCell(0, i).getContents());
                            map.put("instrument_name", sheet.getCell(1, i).getContents());
                            mapList.add(map);
                        }
                        myDao.insertInstru(mapList);
                    }
                }
                book.close();
                is.close();
            } catch (Exception e) {
                Log.e("test", "e" + e);
            }
            try {
                InputStream is = new FileInputStream(stock_file);
                Workbook book = Workbook.getWorkbook(is, workbookSettings);
                Sheet sheet = book.getSheet(0);
                int Rows = sheet.getRows();
                ArrayList<Integer> mList = new ArrayList<>();
                ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
                if (Rows > 1) {
                    for (int i = 1; i < Rows; i++) {
                        String instrument_name = sheet.getCell(0, i).getContents();
                        String class_name = sheet.getCell(1, i).getContents();
                        int sum = Integer.parseInt(sheet.getCell(2, i).getContents());
                        int add_new = Integer.parseInt(sheet.getCell(3, i).getContents());
                        int sum_update = sum + add_new;
                        mList.add(sum);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("instrument_name", instrument_name);
                        map.put("class_name", class_name);
                        map.put("sum", Integer.toString(sum_update));
                        mapList.add(map);
                    }
                }
                Log.i("test", mapList.size() + "");
                if (myDao.sumIsEmpty()) {
                    isSuccess=true;
                    myDao.insertTotal(mapList);
                    Toast.makeText(mContext,"导入成功",Toast.LENGTH_SHORT).show();
                } else {
                    if (myDao.checkSum(mList)) {
                        isSuccess=true;
                        myDao.updateSum(mapList);
                        Toast.makeText(mContext,"导入成功",Toast.LENGTH_SHORT).show();
                    } else {
                        isSuccess=false;
                        Toast.makeText(mContext, "库存总数不匹配", Toast.LENGTH_SHORT).show();
                    }
                }
                book.close();
                is.close();
            } catch (Exception e) {
                Log.e("test", "e" + e);
            }
        } else {
            Toast.makeText(mContext, "文件缺失，请检查", Toast.LENGTH_SHORT).show();
            isSuccess=false;
        }
    }

    public boolean isImport(){
        return isSuccess;
    }
}
