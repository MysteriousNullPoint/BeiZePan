package com.example.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.guide.activity_guide_info;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jxl.Image;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_reset#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_reset extends Fragment {

    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnSync;

    private ImageView btnConfirm;
    private EditText passWord;
    private MyDao myDao;
    private MainActivity mainActivity;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_reset() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_reset.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_reset newInstance(String param1, String param2) {
        fragment_set_reset fragment = new fragment_set_reset();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        myDao = new MyDao(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_reset, container, false);
        btnInfo = view.findViewById(R.id.set_reset_tab_info);
        btnId = view.findViewById(R.id.set_reset_tab_id);
        //btnPermission=view.findViewById(R.id.set_reset_tab_permission);
        btnStock = view.findViewById(R.id.set_reset_tab_stock);
        btnTime = view.findViewById(R.id.set_reset_tab_time);
        btnReset = view.findViewById(R.id.set_reset_tab_reset);
        passWord = view.findViewById(R.id.set_reset_edit);
        btnConfirm = view.findViewById(R.id.set_reset_btn_confirm);
        btnSync = view.findViewById(R.id.settings_reset_tab_sync);
        btnReset.setSelected(true);
        mainActivity = (MainActivity) getActivity();
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_info(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_id(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        /*
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_permission(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });

         */

        btnStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_stock(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_time(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passWord.getText().toString().equals("3721")) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new fragment_test(), null)
                            .addToBackStack(null)
                            .commit();
                } else if (passWord.getText().toString().equals("9999")) {
                    myDao.clearAll();
                    myDao.insertAdmin("A000000000000001", "管理员");
                    mainActivity.updateData();
                    btnConfirm.setEnabled(false);
                    SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("FirstRun", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("First", true);
                    editor.apply();
                    try {
                        sendInstrumentData();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(fragment_set_reset.this.getContext(), activity_guide_info.class);
                    startActivity(intent);
                } else if (passWord.getText().toString().equals("0000")) {
                    myDao.clearAll();
                    readExcelFromDownload();
                    mainActivity.updateData();
                    myDao.insertAdmin("A000000000000001", "管理员");
                    try {
                        sendInstrumentData();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (passWord.getText().toString().equals("0")) {
                    testDateLOG();
                } else if (passWord.getText().toString().equals("1")) {
                    testInputLog();
                } else if (passWord.getText().toString().equals("2")) {
                    myDao.testDate();
                } else if (passWord.getText().toString().equals("11110")) {
                    myDao.clearAll();
                    readInventoryFromDownload();
                    /*
                    myDao.insertTestSheet("2021/06/20","借用","大疆机甲EP",1,0,"学生社长");
                    myDao.insertTestSheet("2021/06/20","借用","大疆机甲S1",2,0,"学生社长");
                    myDao.insertTestSheet("2021/06/25","借用","VR眼镜",5,5,"姚老师");
                    myDao.insertTestSheet("2021/06/25","归还","VR眼镜",5,10,"姚老师");
                    myDao.insertTestSheet("2021/06/26","借用","大疆无人机",1,0,"沈老师");
                    myDao.insertTestSheet("2021/06/26","借用","无人机遥控器",1,0,"沈老师");
                    myDao.insertTestSheet("2021/06/26","归还","大疆无人机",1,1,"沈老师");
                    myDao.insertTestSheet("2021/06/26","归还","无人机遥控器",1,1,"沈老师");
                    myDao.insertTestSheet("2021/06/27","借用","百度语音",6,4,"沈老师");
                    myDao.insertTestSheet("2021/06/27","归还","百度语音",6,10,"沈老师");
                    myDao.insertTestSheet("2021/06/28","归还","大疆机甲S1",2,2,"学生社长");
                    myDao.insertTestSheet("2021/06/28","归还","大疆机甲EP",1,1,"学生社长");

                     */
                    myDao.insertAdmin("A000000000000001", "管理员");
                } else
                    Toast.makeText(fragment_set_reset.this.getContext(), "密码错误", Toast.LENGTH_SHORT).show();
            }
        });
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_sync(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }

    public void readExcelFromDownload() {
        String labelPath = "/storage/emulated/0/Download/data.xls";
        String stockPath = "/storage/emulated/0/Download/stock.xls";
        // String labelPath = "/mnt/media_rw/usbotg/data.xls";
        // String stockPath = "/mnt/media_rw/usbotg/stock.xls";
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
                if (myDao.sumIsEmpty()) {
                    myDao.insertTotal(mapList);
                    Toast.makeText(this.getActivity(), "导入完成", Toast.LENGTH_SHORT).show();
                }
                book.close();
                is.close();
            } catch (Exception e) {
                Log.e("test", "e" + e);
            }
        }
    }

    public void readInventoryFromDownload() {
        String labelPath = "/storage/emulated/0/Download/data_inventory.xls";
        String stockPath = "/storage/emulated/0/Download/stock_inventory.xls";
        // String labelPath = "/mnt/media_rw/usbotg/data.xls";
        // String stockPath = "/mnt/media_rw/usbotg/stock.xls";
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
                if (myDao.sumIsEmpty()) {
                    myDao.insertTotal(mapList);
                    Toast.makeText(this.getActivity(), "导入完成", Toast.LENGTH_SHORT).show();
                }
                book.close();
                is.close();
            } catch (Exception e) {
                Log.e("test", "e" + e);
            }
        }
    }

    private void sendInstrumentData() throws InterruptedException {
        Log.i("test", "开始发送");
        List<String> sendMessage = new ArrayList<>();
        sendMessage.add("xx000005");
        sendMessage.add("xx000005");
        sendMessage.add("xx000005");
        mainActivity.sendData(sendMessage);
        Toast.makeText(fragment_set_reset.this.getContext(), "重置完成", Toast.LENGTH_SHORT).show();
    }

    private void testDateLOG() {
        List<String> mList = new ArrayList<>();
        int a = 94;
        while (a > 30) {
            mList.add("B00000" + a);
            a = a - 1;
            long date = System.currentTimeMillis() / 1000 - 3600 * 24 * 60;
            date = date * 1000;
            myDao.insertBorrowSheet(date, "借用", "电烙铁", a, "admin");
        }
        myDao.updateLog(0, System.currentTimeMillis() - 19 * 24 * 3600 * 1000 + 20000, mList);
        mList.clear();
        a = 94;
        while (a > 88) {
            mList.add("B00000" + a);
            a = a - 1;
            long date = System.currentTimeMillis() / 1000 - 3600 * 24 * 60;
            date = date * 1000;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            String Date = sdf.format(date);
            Log.i("datelog", Date);
            myDao.insertBorrowSheet(date, "归还", "万用表", a, "admin");
        }
        a = 90;
        while (a > 80) {
            mList.add("B00000" + a);
            a = a - 1;
            myDao.insertBorrowSheet(System.currentTimeMillis(), "补充", "电烙铁", a, "admin");
        }
        myDao.updateLog(1, System.currentTimeMillis() - 17 * 24 * 3600 * 1000, mList);
    }

    private void testInputLog() {
        myDao.insertSheet("补充", "电脑", 2, "admin_name");
    }


}