package com.example.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.file.fragment_file;
import com.example.guide.MyAdapter;
import com.example.guide.activity_guide_info;
import com.spd.mdm.manager.MdmManager;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_settings extends Fragment {

    private ImageView btnInfo;
    private ImageView btnTag;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnId;
    private ImageView btnExportStock;
    private ImageView IVbackground;
    private ImageView importUSB;
    private ImageView exportUSB;
    private ImageView btnConfirm;
    private EditText EditSchool;
    private EditText EditLab;
    private EditText EditDays;
    private EditText passWord;
    private ImageView background;
    MdmManager manager;
    int flag = 1;
    private MainActivity mainActivity;
    DatePicker datePicker;
    TimePicker timePicker;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GridView mGridView;
    private MyAdapter myAdapter;
    private List<String> mList = new ArrayList<>();
    List<String> removeList = new ArrayList<>();
    private MyDao myDao;
    private ImageView btnAdd;
    private IUHFService iuhfService;
    private String cardID;
    private TextView tv_cardNO;
    private EditText ed_adminName;
    private ImageView IV_confirm;

    public fragment_settings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_settings.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_settings newInstance(String param1, String param2) {
        fragment_settings fragment = new fragment_settings();
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
        iuhfService = UHFManager.getUHFService(this.getContext());
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                String label_id = spdInventoryData.epc;
                if (!background.isSelected()) {
                    if (!CheckSameCard(label_id) && label_id.length() == 12) {
                        background.setSelected(true);
                        cardID = label_id;
                        Log.i("test", label_id);
                        iuhfService.inventoryStop();
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void onInventoryStatus(int i) {
                //TODO 盘点失败回调

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        btnInfo = view.findViewById(R.id.settings_tab_info);
        btnTag = view.findViewById(R.id.settings_tab_tag);
        btnTime = view.findViewById(R.id.settings_tab_time);
        btnReset = view.findViewById(R.id.settings_tab_reset);
        btnId = view.findViewById(R.id.settings_tab_id);
        btnExportStock = view.findViewById(R.id.settings_tab_stock);
        mainActivity = (MainActivity) getActivity();
        manager = MdmManager.getInstance();
        IVbackground = view.findViewById(R.id.settings_background);
        exportUSB = view.findViewById(R.id.settings_btn_export);
        importUSB = view.findViewById(R.id.settings_btn_import);
        EditSchool = view.findViewById(R.id.settings_school);
        EditLab = view.findViewById(R.id.settings_lab);
        EditDays = view.findViewById(R.id.settings_days);
        passWord = view.findViewById(R.id.settings_reset);
        btnInfo.setSelected(true);
        EditSchool.setText(mainActivity.school_name.getText());
        EditLab.setText(mainActivity.lab_name.getText());

        datePicker = (DatePicker) view.findViewById(R.id.settings_datePicker);
        timePicker = (TimePicker) view.findViewById(R.id.settings_timePicker);
        timePicker.setIs24HourView(true);
        setDatePickerDividerColor(datePicker);
        setTimePickerDividerColor(timePicker);

        btnConfirm = view.findViewById(R.id.settings_btn_confirm);

        btnAdd = view.findViewById(R.id.settings_addbutton);
        mGridView = view.findViewById(R.id.settings_gridview);
        importUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readExcel();
            }
        });

        exportUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readExcel();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnAdd.isSelected()) showCustomizeDialog();
                else {
                    showAdminDialog();
                    iuhfService.openDev();
                    iuhfService.inventoryStart();
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (flag) {
                    case 1:
                        if (EditLab.getText() != null && EditSchool.getText() != null && EditDays.getText() != null)
                            setInfo();
                        Toast.makeText(fragment_settings.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        myDao.ResetClass(mList);
                        Toast.makeText(fragment_settings.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        myDao.ResetAdmin(removeList);
                        removeList.clear();
                        Toast.makeText(fragment_settings.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth() + 1;
                        int day = datePicker.getDayOfMonth();
                        int hour = timePicker.getHour();
                        int minute = timePicker.getMinute();
                        String time = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day) + " " + Integer.toString(hour) + ":" + Integer.toString(minute) + ":" + "0";
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //设置要读取的时间字符串格式
                        Date date = null;
                        try {
                            date = format.parse(time);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //转换为Date类
                        Long timestamp = date.getTime();
                        manager.setSysTime(timestamp);
                        Toast.makeText(fragment_settings.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        if (passWord.getText().toString().equals("8889")) {
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment, new fragment_test(), null)
                                    .addToBackStack(null)
                                    .commit();
                        } else if (passWord.getText().toString().equals("9999")) {
                            Intent intent = new Intent(fragment_settings.this.getContext(), activity_guide_info.class);
                            startActivity(intent);
                        } else if (passWord.getText().toString().equals("0000")) {
                            myDao.clearAll();
                            readExcelFromDownload();
                            myDao.insertAdmin("A00000000000", "管理员1");
                        } else
                            Toast.makeText(fragment_settings.this.getContext(), "密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        IVbackground.setImageLevel(1);
        btnInfo.setOnClickListener(l);
        btnTag.setOnClickListener(l);
        btnTime.setOnClickListener(l);
        btnReset.setOnClickListener(l);
        btnId.setOnClickListener(l);
        btnExportStock.setOnClickListener(l);


        return view;
    }


    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnInfo.setSelected(false);
            btnTag.setSelected(false);
            btnTime.setSelected(false);
            btnReset.setSelected(false);
            btnId.setSelected(false);
            btnExportStock.setSelected(false);
            EditSchool.setVisibility(View.INVISIBLE);
            EditLab.setVisibility(View.INVISIBLE);
            EditDays.setVisibility(View.INVISIBLE);
            passWord.setVisibility(View.INVISIBLE);
            timePicker.setVisibility(View.INVISIBLE);
            datePicker.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.INVISIBLE);
            btnAdd.setVisibility(View.INVISIBLE);
            importUSB.setVisibility(View.INVISIBLE);
            exportUSB.setVisibility(View.INVISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
            v.setSelected(true);
            switch (v.getId()) {
                case R.id.settings_tab_info:
                    EditSchool.setText(mainActivity.school_name.getText());
                    EditLab.setText(mainActivity.lab_name.getText());
                    IVbackground.setImageLevel(1);
                    EditSchool.setVisibility(View.VISIBLE);
                    EditLab.setVisibility(View.VISIBLE);
                    EditDays.setVisibility(View.VISIBLE);
                    flag = 1;
                    break;
                case R.id.settings_tab_tag:
                    IVbackground.setImageLevel(11);
                    mGridView.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.VISIBLE);
                    btnAdd.setSelected(true);
                    mList = myDao.getClassName();
                    mList.remove(0);
                    myAdapter = new MyAdapter(fragment_settings.this.getContext(), mList);
                    mGridView.setAdapter(myAdapter);
                    myAdapter.setOnItemDeleteClickListener(new MyAdapter.onItemDeleteListener() {
                        @Override
                        public void onDeleteClick(int i) {
                            mList.remove(i);
                            myAdapter.notifyDataSetChanged();
                        }
                    });
                    flag = 2;
                    break;
                case R.id.settings_tab_time:
                    IVbackground.setImageLevel(21);
                    timePicker.setVisibility(View.VISIBLE);
                    datePicker.setVisibility(View.VISIBLE);
                    flag = 5;
                    break;
                case R.id.settings_tab_reset:
                    IVbackground.setImageLevel(31);
                    passWord.setVisibility(View.VISIBLE);
                    flag = 6;
                    break;
                case R.id.settings_tab_id:
                    removeList.clear();
                    IVbackground.setImageLevel(11);
                    mGridView.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.VISIBLE);
                    btnAdd.setSelected(false);
                    mList = myDao.getAdminName();
                    myAdapter = new MyAdapter(fragment_settings.this.getContext(), mList);
                    mGridView.setAdapter(myAdapter);
                    myAdapter.setOnItemDeleteClickListener(new MyAdapter.onItemDeleteListener() {
                        @Override
                        public void onDeleteClick(int i) {
                            removeList.add(mList.get(i));
                            mList.remove(i);
                            myAdapter.notifyDataSetChanged();
                        }
                    });
                    flag = 3;
                    break;
                case R.id.settings_tab_stock:
                    flag = 4;
                    IVbackground.setImageLevel(41);
                    importUSB.setVisibility(View.VISIBLE);
                    exportUSB.setVisibility(View.VISIBLE);
                    btnConfirm.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    private void setInfo() {
        String school_name = EditSchool.getText().toString();
        String lab_name = EditLab.getText().toString();
        String day = EditDays.getText().toString();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("FirstRun", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("school", school_name);
        editor.putString("lab", lab_name);
        editor.putString("days", day);
        editor.apply();
        mainActivity.school_name.setText(school_name);
        mainActivity.lab_name.setText(lab_name);
    }

    private void setDatePickerDividerColor(DatePicker datePicker) {
        // Divider changing:

        // 获取 mSpinners
        LinearLayout llFirst = (LinearLayout) datePicker.getChildAt(0);

        // 获取 NumberPicker
        LinearLayout mSpinners = (LinearLayout) llFirst.getChildAt(0);
        for (int i = 0; i < mSpinners.getChildCount(); i++) {
            NumberPicker picker = (NumberPicker) mSpinners.getChildAt(i);

            Field[] pickerFields = NumberPicker.class.getDeclaredFields();
            for (Field pf : pickerFields) {
                if (pf.getName().equals("mSelectionDivider")) {
                    pf.setAccessible(true);
                    try {
                        pf.set(picker, new ColorDrawable(Color.parseColor("#000000")));//设置分割线颜色
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private void setTimePickerDividerColor(TimePicker timePicker) {
        Resources systemResources = Resources.getSystem();
        int hourNumberPickerId = systemResources.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = systemResources.getIdentifier("minute", "id", "android");
        NumberPicker hourNumberPicker = (NumberPicker) timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = (NumberPicker) timePicker.findViewById(minuteNumberPickerId);
        setNumberPickerDivider(hourNumberPicker);
        setNumberPickerDivider(minuteNumberPicker);

    }

    private void setNumberPickerDivider(NumberPicker numberPicker) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            try {
                Field dividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(
                        ContextCompat.getColor(this.getContext(), android.R.color.black));
                dividerField.set(numberPicker, colorDrawable);
                numberPicker.invalidate();
            } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                Log.w("setNumberPickerTxtClr", e);
            }
        }
    }

    private void showCustomizeDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.add_class_dialog, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        ImageView cancel = (ImageView) view.findViewById(R.id.cancel);
        ImageView ok = (ImageView) view.findViewById(R.id.ok);
        EditText text = (EditText) view.findViewById(R.id.edit_class);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String class_name = text.getText().toString();
                mList.add(class_name);
                myAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showAdminDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.add_admin_dialog, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        ImageView cancel = (ImageView) view.findViewById(R.id.settings_cancel);
        background = (ImageView) view.findViewById(R.id.settings_addid);
        tv_cardNO = (TextView) view.findViewById(R.id.settings_cardNO);
        ed_adminName = (EditText) view.findViewById(R.id.settings_admin);
        IV_confirm = (ImageView) view.findViewById(R.id.settings_dialog_confirm);
        dialog.setCanceledOnTouchOutside(false);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iuhfService.inventoryStop();
                iuhfService.closeDev();
                dialog.dismiss();
            }
        });
        IV_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ed_adminName.getText().toString();
                myDao.insertAdmin(cardID, name);
                iuhfService.inventoryStop();
                iuhfService.closeDev();
                mList.add(name);
                myAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean CheckSameCard(String Label_ID) {
        boolean flag = false;
        List<String> idList = myDao.getAdminID();
        for (String id : idList) {
            if (id.equals(Label_ID)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 0:
                    tv_cardNO.setVisibility(View.VISIBLE);
                    tv_cardNO.setText(Integer.toString(mList.size() + 1));
                    ed_adminName.setVisibility(View.VISIBLE);
                    IV_confirm.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    public void readExcel() {
        //String labelPath = "/storage/emulated/0/Download/data.xls";
        //String stockPath = "/storage/emulated/0/Download/stock.xls";
        String labelPath = "/mnt/media_rw/usbotg/data.xls";
        String stockPath = "/mnt/media_rw/usbotg/stock.xls";
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
        } else {
            Toast.makeText(this.getContext(), "请插入U盘", Toast.LENGTH_SHORT).show();
        }
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
}