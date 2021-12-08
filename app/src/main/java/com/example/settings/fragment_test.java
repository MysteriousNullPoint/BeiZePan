package com.example.settings;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.DbHelper;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.file.fragment_file;
import com.spd.mdm.manager.MdmManager;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_test#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_test extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private IUHFService iuhfService;
    boolean isRun = false;
    MdmManager manager;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String path = "";
    private MainActivity mainActivity = (MainActivity) getActivity();
    TextView tv;
    TextView tv2;
    TextView tv3;
    Dao dao;
    MyDao myDao;
    private List<String> listInstruName;

    public fragment_test() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_file.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_file newInstance(String param1, String param2) {
        fragment_file fragment = new fragment_file();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        tv = view.findViewById(R.id.textview1);
        dao = new Dao(this.getContext());
        myDao = new MyDao(this.getContext());
        Button button = view.findViewById(R.id.insert);
        Button button2 = view.findViewById(R.id.print);
        Button button7 = view.findViewById(R.id.button7);
        Button button_clear = view.findViewById(R.id.button10);
        Button button_default=view.findViewById(R.id.test_button_default);
        Button button_app=view.findViewById(R.id.test_button_app);
        manager = MdmManager.getInstance();
        button_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setDefaultHome("com.android.launcher3/.Launcher");
            }
        });

        button_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setDefaultHome("com.example.cupboard/.WelcomeActivity");
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printData();
            }
        });

        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readExcel();
            }
        });
        button_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTable();
            }
        });
        return view;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public void onDestroy() {
        if (iuhfService != null) {
            iuhfService.closeDev();
            iuhfService = null;
            UHFManager.closeUHFService();
        }
        Log.i("test", "退出");
        super.onDestroy();
    }

    private void clearTable() {
        myDao.clearAll();
    }

    private void insertData() {
        Log.i("test", "开始测试");
    }

    private void printData() {
        List<String> name = myDao.printData();
        for (String str : name) {
            appendPrintPanel(str);
        }
    }


    private void appendPrintPanel(String msg) {
        tv.append(msg + '\n');
        int offset = tv.getLineCount() * tv.getLineHeight();
        if (offset > tv.getHeight()) {
            tv.scrollTo(0, offset - tv.getHeight());
        }
    }


    private void ReadLabelCsv() {
        String labelPath = "/storage/emulated/0/Download/data.csv";
        String stockPath = "/storage/emulated/0/Download/stock.csv";
        File label_file = new File(labelPath);
        File stock_file = new File(stockPath);
        if (label_file.canRead() && stock_file.canRead()) {
            Toast.makeText(this.getActivity(), "发现文件", Toast.LENGTH_SHORT).show();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(label_file), "GB2312"));
                br.readLine();
                listInstruName.clear();
                String line = "";
                //读第一行
                if ((line = br.readLine()) != null) {
                    String buffer1[] = line.split(",");
                    if (!CheckSameLabel(buffer1[0])) {
                        if (listInstruName.size() == 0) {
                            listInstruName.add(buffer1[1]);
                            dao.insertInstruName(buffer1[1], buffer1[2], buffer1[3]);
                        } else {
                            boolean isin = false;
                            for (int i = 0; i < listInstruName.size(); i++) {
                                if (buffer1[1].equals(listInstruName.get(i))) {
                                    isin = true;
                                    break;
                                }
                            }
                            if (!isin) {
                                listInstruName.add(buffer1[1]);
                                dao.insertInstruName(buffer1[1], buffer1[2], buffer1[3]);
                            }
                        }
                        dao.insertInstruId(buffer1[0], buffer1[1]);
                        while ((line = br.readLine()) != null) {
                            String buffer[] = line.split(",");
                            boolean notsame = true;
                            for (int i = 0; i < listInstruName.size(); i++) {
                                if (buffer[1].equals(listInstruName.get(i))) {
                                    notsame = false;
                                    break;
                                }
                            }
                            if (notsame) {
                                listInstruName.add(buffer[1]);
                                dao.insertInstruName(buffer[1], buffer[2], buffer[3]);
                            }
                            dao.insertInstruId(buffer[0], buffer[1]);
                        }
                    }
                }
                br.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stockPath), "GB2312"));
                br.readLine();
                String line = "";
                List<HashMap<String, String>> mapList = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    HashMap<String, String> map = new HashMap<>();
                    String buffer1[] = line.split(",");
                    String instru_name = buffer1[0];
                    String sum = buffer1[1];
                    String stock = buffer1[2];
                    String scrap = buffer1[3];
                    String stock_in = buffer1[4];
                    String used = buffer1[5];
                    String add_new = buffer1[6];
                    map.put("instrument_name", instru_name);
                    map.put("sum", sum);
                    map.put("stock", stock);
                    map.put("scrap", scrap);
                    map.put("stock_in", stock_in);
                    map.put("used", used);
                    map.put("add_new", add_new);
                    mapList.add(map);
                }
                if (dao.checkStock(mapList)) {
                    dao.insertStock(mapList);
                } else Toast.makeText(this.getActivity(), "库存总数不匹配", Toast.LENGTH_LONG).show();
                br.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else Toast.makeText(this.getActivity(), "遗失文件", Toast.LENGTH_LONG).show();
    }


    //检查重复
    private boolean CheckSameLabel(String Label_ID) {
        boolean flag = false;
        List<String> mList = new ArrayList<>();
        mList = dao.getIdFromInstru();
        for (String str : mList) {
            if (str.equals(Label_ID)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void readExcel() {
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
                Workbook book = Workbook.getWorkbook(is,workbookSettings);
                Sheet sheet = book.getSheet(0);
                int Rows = sheet.getRows();
                List<HashMap<String, String>> mapList = new ArrayList<>();
                if (Rows > 1) {
                    if (!myDao.checkSameLabel(sheet.getCell(0, 1).getContents())) {
                        for (int i = 1; i < Rows; i++) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("instrument_id", sheet.getCell(0, i).getContents());
                            map.put("instrument_name", sheet.getCell(1, i).getContents());
                            appendPrintPanel(map.get("instrument_id")+map.get("instrument_name"));
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
                Workbook book = Workbook.getWorkbook(is,workbookSettings);
                Sheet sheet = book.getSheet(0);
                int Rows = sheet.getRows();
                ArrayList<Integer> mList = new ArrayList<>();
                ArrayList<HashMap<String, String>> mapList = new ArrayList<>();
                if (Rows > 1) {
                    for (int i = 1; i < Rows; i++) {
                        String instrument_name = sheet.getCell(0, i).getContents();
                        String class_name=sheet.getCell(1, i).getContents();
                        int sum = Integer.parseInt(sheet.getCell(2, i).getContents());
                        int add_new = Integer.parseInt(sheet.getCell(3, i).getContents());
                        int sum_update = sum + add_new;
                        mList.add(sum);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("instrument_name", instrument_name);
                        map.put("class_name",class_name);
                        map.put("sum", Integer.toString(sum_update));
                        mapList.add(map);
                        appendPrintPanel(map.get("instrument_name")+map.get("class_name")+map.get("sum"));
                    }
                }
                if (myDao.sumIsEmpty()) {
                    myDao.insertTotal(mapList);
                    Toast.makeText(this.getActivity(), "导入完成", Toast.LENGTH_SHORT).show();
                } else {
                    if (myDao.checkSum(mList)) {
                        Log.i("test", "数据正确");
                        myDao.updateSum(mapList);
                    } else Toast.makeText(this.getActivity(), "库存总数不匹配", Toast.LENGTH_SHORT).show();
                }
                book.close();
                is.close();
            } catch (Exception e) {
                Log.e("test", "e" + e);
            }
        }
    }

}

