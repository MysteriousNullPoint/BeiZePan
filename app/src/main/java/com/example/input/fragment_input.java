package com.example.input;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.bean.SpdWriteData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.interfaces.OnSpdWriteListener;
import com.speedata.libuhf.utils.ErrorStatus;
import com.speedata.libuhf.utils.StringUtils;

import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class fragment_input extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    MyDao myDao;
    //UI
    private ConstraintLayout constraintLayout;
    private TextView tv_cupboard_name;
    private ImageView start_scan;
    private ImageView btn_confirm;
    private ImageView btn_rescan;
    private TextView tv_sum;
    private MyPicker linePicker;
    private MyPicker rowPicker;
    private String cupboard_name = "";
    private String admin_name = "";
    private IUHFService iuhfService;
    private GridView gridView;
    int instru_sum;
    private TextView tv_time;
    private TextView tv_number;

    private ImageView dialog_background;
    private TextView dialog_tv;
    private ImageView dialog_ok;
    //数据
    private ArrayList<Map<String, Object>> listMap = new ArrayList<>();
    ; //仪器名称与统计次数
    private ArrayList<Instrument> listInstru = new ArrayList<>(); //Instrument(ID、NAME、COUNT)
    private HashMap<String, String> instrumentMap = new HashMap<>(); //ID映射name
    private HashMap<String, String> cupboardMap = new HashMap<>();   //柜子ID映射name
    ArrayList<String> searchedinstru = new ArrayList<>(); //已扫描ID
    List<String> cupboardList = new ArrayList<>();  //柜子ID列表
    List<String> adminList = new ArrayList<>();//管理员ID列表
    Boolean run = false;
    private timeCount timeThread;
    private MainActivity mainActivity;
    private boolean dialogShow = true;
    private boolean isOpen = false;


    String cupboard_id = "";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int flag;
    private Boolean dialog_out = false;
    String[] lines = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    String[] rows = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16"};

    public fragment_input() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_input.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_input newInstance(String param1, String param2) {
        fragment_input fragment = new fragment_input();
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
        myDao = new MyDao(this.getContext());
        listInstru = new ArrayList<Instrument>();
        instru_sum = 0;
        flag = Constants.INIT_INPUT;
        instrumentMap = myDao.getInstruMap();
        cupboardList = myDao.getCupboardID();
        cupboardMap = myDao.getCupMap();
        adminList = myDao.getAdminID();
        mainActivity = (MainActivity) getActivity();
        //获取实例对象
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_input, container, false);
        constraintLayout = view.findViewById(R.id.input_background);
        tv_cupboard_name = view.findViewById(R.id.input_cup_name);
        start_scan = view.findViewById(R.id.input_start_scan);
        btn_confirm = view.findViewById(R.id.input_btn_confirm);
        btn_rescan = view.findViewById(R.id.input_btn_rescan);
        tv_sum = view.findViewById(R.id.input_sum);
        tv_time = view.findViewById(R.id.input_time);
        tv_number = view.findViewById(R.id.input_number);
        gridView = (GridView) view.findViewById(R.id.input_gridview);
        iuhfService = UHFManager.getUHFService(this.getContext());
        btn_confirm.setSelected(true);
        btn_rescan.setSelected(true);
        start_scan.setImageLevel(1);
        btn_rescan.setOnClickListener(l);
        btn_confirm.setOnClickListener(l);
        start_scan.setOnClickListener(l);
        flag = Constants.INIT_INPUT;
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                String label_id = spdInventoryData.epc;
                switch (flag) {
                    case Constants.START_INPUT:
                        if (label_id.length() == Constants.ECP_LENGTH_CUPBOARD) {
                            if (!isSameCupboard(label_id)) {
                                if (!dialog_out) {
                                    Log.i("test", label_id);
                                    cupboard_id = label_id;
                                    iuhfService.inventoryStop();
                                    showCustomizeDialog();
                                    dialog_out = true;
                                    Message msg = new Message();
                                    msg.obj=label_id;
                                    msg.what = 22;
                                    handler.sendMessage(msg);
                                }
                            } else {
                                cupboard_id = label_id;
                                cupboard_name = cupboardMap.get(cupboard_id);
                                setCupboardText(cupboard_name);
                            }
                        }
                        if (!cupboard_name.equals("")) flag = Constants.READY_TO_SCAN;
                        break;

                    case Constants.READY_TO_SCAN:
                        break;
                    case Constants.SCANNING:
                        if (label_id.length() == Constants.EPC_LENGTH_LABEL) {
                            if (searchedinstru.isEmpty() && instrumentMap.get(label_id) != null) {
                                searchedinstru.add(label_id);
                                addToList(listInstru, instrumentMap.get(label_id));
                                instru_sum++;
                            } else {
                                boolean isnew = true;
                                for (int i = 0; i < searchedinstru.size(); i++) {
                                    String mEPC = searchedinstru.get(i);
                                    //list中有此EPC
                                    if (label_id.equals(mEPC)) {
                                        isnew = false;
                                        break;
                                    }
                                }
                                if (isnew) {
                                    //list中没有此epc
                                    if (instrumentMap.get(label_id) != null) {
                                        searchedinstru.add(label_id);
                                        addToList(listInstru, instrumentMap.get(label_id));
                                        instru_sum++;
                                    }
                                }
                            }
                            Message msg = new Message();
                            msg.what = 2;
                            handler.sendMessage(msg);
                        }
                        break;
                    case Constants.INIT_INPUT:
                        if (label_id.length() == Constants.EPC_LENGTH_ADMIN) {
                            for (String ID : adminList) {
                                if (label_id.equals(ID)) {
                                    admin_name = myDao.getAdminName(ID);
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = admin_name;
                                    handler.sendMessage(msg);
                                    iuhfService.inventoryStop();
                                }
                            }
                        }
                        break;
                }

            }

            @Override
            public void onInventoryStatus(int i) {
                //TODO 盘点失败回调
            }
        });
        return view;
    }

    public void onDestroy() {
        if (isOpen) {
            iuhfService.inventoryStop();
            iuhfService.closeDev();
            iuhfService = null;
            UHFManager.closeUHFService();
        }
        Log.i("test", "退出录入");
        super.onDestroy();
    }

    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.input_btn_rescan:
                    if (flag == Constants.END_SCAN) {
                        searchedinstru.clear();
                        listInstru.clear();
                        listMap.clear();
                        instru_sum = 0;
                        start_scan.setImageLevel(31);
                        tv_sum.setVisibility(View.INVISIBLE);
                        flag = Constants.SCANNING;
                        setAdapter();
                        iuhfService.inventoryStart();
                        btn_confirm.setSelected(false);
                        btn_rescan.setSelected(false);
                        tv_number.setText("0");
                        openThread();
                    }
                    break;
                case R.id.input_btn_confirm:
                    if (flag == Constants.END_SCAN) {
                        myDao.insertCupboard(cupboard_id, cupboard_name, searchedinstru);
                        Log.i("inputlabel",cupboard_id);
                        if (!mainActivity.aerial_state.isSelected()) {
                            try {
                                sendInstrumentData();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            myDao.insertSync(1, searchedinstru);
                            Toast.makeText(fragment_input.this.getContext(), "请连接门禁同步数据", Toast.LENGTH_SHORT).show();
                        }
                        insertLog();
                        constraintLayout.setBackgroundResource(R.mipmap.input_backgroud_connected);
                        tv_sum.setVisibility(View.INVISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        tv_time.setVisibility(View.INVISIBLE);
                        tv_number.setVisibility(View.INVISIBLE);
                        tv_time.setText("0");
                        tv_number.setText("0");
                        btn_rescan.setSelected(true);
                        btn_confirm.setSelected(true);
                        start_scan.setImageLevel(1);
                        cupboard_name = "";
                        cupboardList = myDao.getCupboardID();
                        cupboardMap = myDao.getCupMap();
                        instrumentMap = myDao.getInstruMap();
                        listMap.clear();
                        searchedinstru.clear();
                        listInstru.clear();
                        instru_sum = 0;
                        tv_cupboard_name.setSelected(false);
                        tv_cupboard_name.setText(cupboard_name);
                        setAdapter();
                        flag = Constants.INIT_INPUT;
                    }
                    break;
                case R.id.input_start_scan:
                    switch (flag) {
                        case Constants.INIT_INPUT:
                            //模块上电
                            Toast.makeText(fragment_input.this.getContext(), "请识别身份卡", Toast.LENGTH_SHORT).show();
                            iuhfService.openDev();
                            isOpen = true;
                            iuhfService.inventoryStart();
                            if (dialogShow) {
                                dialogShow = false;
                                showAdminDialog();
                            }
                            break;
                        case Constants.START_INPUT:
                            start_scan.setImageLevel(11);
                            iuhfService.inventoryStart();
                            Toast.makeText(fragment_input.this.getContext(), "请扫描录入柜体", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.READY_TO_SCAN:
                            Log.i("beep","start");
                            flag = Constants.SCANNING;
                            start_scan.setImageLevel(31);
                            constraintLayout.setBackgroundResource(R.mipmap.input_background_scanning);
                            gridView.setVisibility(View.VISIBLE);
                            tv_time.setVisibility(View.VISIBLE);
                            tv_number.setVisibility(View.VISIBLE);
                            openThread();
                            break;
                        case Constants.SCANNING:
                            flag = Constants.END_SCAN;
                            start_scan.setImageLevel(41);
                            btn_confirm.setSelected(false);
                            btn_rescan.setSelected(false);
                            tv_sum.setVisibility(View.VISIBLE);
                            tv_sum.setText(Integer.toString(instru_sum));
                            iuhfService.inventoryStop();
                            stopThread();
                            break;
                    }
                    break;
            }
        }
    };

    //region 设置柜子
    private void showCustomizeDialog() {
        View view = LayoutInflater.from(this.getActivity()).inflate(R.layout.dialog_cupboard_picker, null, false);
        fragment_input.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog dialog = new AlertDialog.Builder(fragment_input.this.getActivity()).setView(view).create();
                ImageView cancel = (ImageView) view.findViewById(R.id.input_cup_cancel);
                ImageView ok = (ImageView) view.findViewById(R.id.input_cup_ok);
                linePicker = view.findViewById(R.id.input_linepicker);
                rowPicker = view.findViewById(R.id.input_rowpicker);
                dialog.setCanceledOnTouchOutside(false);
                initPicker();
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_out = false;
                        dialog.dismiss();
                        iuhfService.inventoryStop();
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int l = linePicker.getValue() - 1;
                        int r = rowPicker.getValue() - 1;
                        String s = lines[l] + rows[r];
                        boolean notSameCup = true;
                        for (String value : cupboardMap.values()) {
                            if (value.equals(s)) {
                                notSameCup = false;
                                break;
                            }
                        }
                        if (notSameCup) {
                            cupboard_name = s;
                            tv_cupboard_name.setSelected(true);
                            tv_cupboard_name.setText("智能柜" + cupboard_name);
                            start_scan.setImageLevel(21);
                            flag = Constants.READY_TO_SCAN;
                            dialog_out = false;
                            dialog.dismiss();
                            iuhfService.inventoryStart();
                        } else {
                            Toast.makeText(fragment_input.this.getContext(), "已存在重复柜子名", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });

    }

    public void initPicker() {
        //设置需要显示的内容数组
        linePicker.setDisplayedValues(lines);
        rowPicker.setDisplayedValues(rows);
        //设置最大最小值
        linePicker.setMinValue(1);
        rowPicker.setMinValue(1);
        linePicker.setMaxValue(lines.length);
        rowPicker.setMaxValue(rows.length);
        //设置默认的位置
        linePicker.setValue(1);
        rowPicker.setValue(1);
        linePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        rowPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setNumberPickerDividerColor(linePicker);
        setNumberPickerDividerColor(rowPicker);
    }

    private void setNumberPickerDividerColor(NumberPicker numberPicker) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(numberPicker, new ColorDrawable(getResources().getColor(R.color.blue)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    //endregion


    //插入库存
    private void insertLog() {
        for (int i = 0; i < listMap.size(); i++) {
            String name = (String) listMap.get(i).get("EPC");
            int count = (int) listMap.get(i).get("COUNT");
            myDao.insertSheet("补充", name, count, admin_name);
            myDao.insertMaintain(name);
        }
    }

    //region 检查标签
    //检查是否是已录入的柜子标签
    private boolean isSameCupboard(String Label_ID) {
        boolean flag = false;
        for (String id : cupboardList) {
            if (id.equals(Label_ID)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


    //endregion

    //将读取的EPC添加到LISTVIEW
    private void addToList(final List<Instrument> list, final String instrument_name) {
        //第一次读入数据
        if (list.isEmpty()) {
            Instrument instrumentTag = new Instrument();
            instrumentTag.setEpc(instrument_name);
            instrumentTag.setCount(1);
            list.add(instrumentTag);
        } else {
            boolean isnew = true;
            for (int i = 0; i < list.size(); i++) {
                Instrument mEPC = list.get(i);
                //list中有此EPC
                if (instrument_name.equals(mEPC.getName())) {
                    mEPC.setCount(mEPC.getCount() + 1);
                    list.set(i, mEPC);
                    isnew = false;
                    break;
                }
            }
            if (isnew) {
                //list中没有此epc
                Instrument newEPC = new Instrument();
                newEPC.setEpc(instrument_name);
                newEPC.setCount(1);
                list.add(newEPC);
            }
        }
        //将数据添加到ListView
        listMap = new ArrayList<>();
        for (Instrument instrudata : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("EPC", instrudata.getName());
            map.put("COUNT", instrudata.getCount());
            listMap.add(map);
        }
        setAdapter();
    }

    private void setAdapter() {
        fragment_input.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gridView.setAdapter(new SimpleAdapter(fragment_input.this.getActivity(),
                        listMap, R.layout.input_gridview,
                        new String[]{"EPC", "COUNT"},
                        new int[]{R.id.input_item_instrument, R.id.input_item_sum}));
            }
        });
    }

    private void setCupboardText(String name) {
        fragment_input.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_cupboard_name.setSelected(true);
                tv_cupboard_name.setText("智能柜" + cupboard_name);
                start_scan.setImageLevel(21);
            }
        });
    }

    private class timeCount extends Thread {
        public void run() {
            int i = 0;
            while (run) {
                try {
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = Integer.toString(i);
                    handler.sendMessage(msg);
                    Thread.sleep(1000);
                    i = i + 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 0:
                    String time = (String) msg.obj;
                    tv_time.setText(time);
                    break;
                case 1:
                    dialog_background.setSelected(true);
                    dialog_tv.setText(admin_name);
                    dialog_ok.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    tv_number.setText(Integer.toString(instru_sum));
                    break;
                case 22:
                    String id=(String)msg.obj;
                    startEpcWrite(id);
                    Log.i("beep",id);
                    break;
            }
        }
    };

    private void openThread() {
        if (timeThread == null) {
            timeThread = new timeCount();
            run = true;
            timeThread.start();
        }
    }

    private void stopThread() {
        if (timeThread != null) {
            run = false;
            timeThread = null;
        }
    }

    private void showAdminDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_checkid, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog_background = (ImageView) view.findViewById(R.id.input_dialog_background);
        dialog_tv = (TextView) view.findViewById(R.id.input_dialog_tv);
        ImageView cancel = (ImageView) view.findViewById(R.id.input_dialog_cancel);
        dialog_ok = (ImageView) view.findViewById(R.id.input_dialog_confirm);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin_name = "";
                if(mainActivity.tab_input.isSelected()){
                    iuhfService.inventoryStop();
                }
                dialogShow = true;
                dialog.dismiss();
            }
        });

        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainActivity.tab_input.isSelected()) {
                    iuhfService.inventoryStop();
                }
                dialogShow = true;
                dialog.dismiss();
                flag = Constants.START_INPUT;
                start_scan.setImageLevel(11);
            }
        });
        dialog.show();
    }

    private void sendInstrumentData() throws InterruptedException {
        List<String> sendList = new ArrayList<>();
        sendList.add("xx000000");
        sendList.add("xx000000");
        sendList.add("xx000000");
        sendList.addAll(searchedinstru);
        mainActivity.sendData(sendList);
    }

    //写卡操作
    private void startEpcWrite(String id){
        Log.i("beep","开始写卡");
        iuhfService.selectCard(1, id, true);
        int writeArea = iuhfService.writeArea(0,2,2,"00000000",new byte[4]);
        if(writeArea!=0){
            Log.i("beep","写卡错误");
        }
        iuhfService.selectCard(1, "", false);
    }
}