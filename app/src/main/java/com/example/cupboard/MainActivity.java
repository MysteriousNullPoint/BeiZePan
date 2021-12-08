package com.example.cupboard;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.dock.fragment_dock;
import com.example.dock.fragment_dockFake;
import com.example.guide.activity_guide_info;
import com.example.data.fragment_data;
import com.example.file.fragment_file;
import com.example.home.fragment_home;
import com.example.search.fragment_changeID;
import com.example.search.fragment_search;
import com.example.settings.ExcelManager;
import com.example.settings.fragment_set_info;
import com.example.stock.ItemEntity;
import com.example.stock.fragment_stock;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.spd.mdm.manager.MdmManager;
import com.tuacy.azlist.LettersComparator;
import com.tuacy.fuzzysearchlibrary.PinyinUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import tech.gusavila92.websocketclient.WebSocketClient;


public class MainActivity extends AppCompatActivity {

    private MyDao myDao;
    private String set_school_name = "";
    private String set_lab_name = "";
    public ImageView tab_home;
    public ImageView tab_stock;
    public ImageView tab_data;
    public ImageView tab_input;
    public ImageView tab_settings;
    public ImageView tab_file;
    public ImageView aerial_state;
    private ImageView sta_power;
    private ImageView sta_charge;
    public TextView school_name;
    public TextView lab_name;
    public EditText editText;
    public ImageView searchBtn;
    private List<String> listInstruName;
    MdmManager manager;
    public String frameFlag = "";
    public TextToSpeech textToSpeech;
    private ExcelManager excelManager;
    ServerSocket serverSocket = null;

    private ViewGroup mLayoutFuzzySearch;
    private FuzzySearchAdapter mFuzzySearchAdapter;
    private RecyclerView mRecyclerSearch;
    private List<ItemEntity> searchList;
    public boolean isReject = false;
    public WebSocketClient webSocketClient;
    private boolean isStartReceive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDao = new MyDao(this);
        excelManager = new ExcelManager(this);
        listInstruName = new ArrayList<>();
        InitView();
        firstRun();
        manager = MdmManager.getInstance();
        manager.setNavigationBarEnable(false);
        manager.setStatusBarPullEnable(false);
        manager.setWifiEnable(true);
        school_name.setText(set_school_name);
        lab_name.setText(set_lab_name);
        //切换状态
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment, new fragment_home());
        ft.commit();
        //注册广播
        registerReceiver();
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //启动UDP服务器监听线程
        createWebSocketClient();
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        //初始化tts引擎
                        textToSpeech.setLanguage(Locale.CHINA);
                        //设置参数
                        textToSpeech.setPitch(1.0f);// 设置音调，,1.0是常规
                        textToSpeech.setSpeechRate(1.0f);//设定语速，1.0正常语速

                    }
                }
            });
        }
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
        set_school_name = sharedPreferences.getString("school", "");
        set_lab_name = sharedPreferences.getString("lab", "");
        school_name.setText(set_school_name);
        lab_name.setText(set_lab_name);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.i("test", "退出");
    }

    protected void onStop() {
        super.onStop();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_HOME == keyCode)
            android.os.Process.killProcess(android.os.Process.myPid());
        return super.onKeyDown(keyCode, event);
    }


    //初始化控件
    private void InitView() {
        //菜单栏
        tab_home = findViewById(R.id.tab_home);
        tab_stock = findViewById(R.id.tab_stock);
        tab_data = findViewById(R.id.tab_data);
        tab_input = findViewById(R.id.tab_input);
        tab_settings = findViewById(R.id.tab_settings);
        tab_file = findViewById(R.id.tab_file);
        searchBtn = findViewById(R.id.main_btn_search);
        searchBtn.setOnClickListener(l);
        //菜单栏监听
        tab_home.setSelected(true);
        tab_home.setOnClickListener(l);
        tab_stock.setOnClickListener(l);
        tab_input.setOnClickListener(l);
        tab_data.setOnClickListener(l);
        tab_settings.setOnClickListener(l);
        tab_file.setOnClickListener(l);
        //状态栏
        aerial_state = findViewById(R.id.aerial_state);
        aerial_state.setSelected(true);
        sta_power = findViewById(R.id.sta_power);
        sta_charge = findViewById(R.id.main_sta_charge);
        //学校、实验室
        school_name = (TextView) findViewById(R.id.school_name);
        lab_name = (TextView) findViewById(R.id.lab_name);
        //搜索栏
        editText = (EditText) findViewById(R.id.edit_main);

        mLayoutFuzzySearch = findViewById(R.id.layout_fuzzy_search);
        mRecyclerSearch = findViewById(R.id.recycler_fuzzy_search_list);
        mRecyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        initEvent();
        initData();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            editText.clearFocus();
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }


    //菜单栏切换
    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            tab_home.setSelected(false);
            tab_stock.setSelected(false);
            tab_data.setSelected(false);
            tab_input.setSelected(false);
            tab_settings.setSelected(false);
            tab_file.setSelected(false);
            if(frameFlag!="dock") {
                Fragment f = new fragment_home();
                switch (v.getId()) {
                    case R.id.tab_home:
                        frameFlag = "home";
                        f = new fragment_home();
                        break;
                    case R.id.tab_stock:
                        frameFlag = "NONE";
                        f = new fragment_stock();
                        break;
                    case R.id.tab_data:
                        f = new fragment_data();
                        frameFlag = "NONE";
                        break;
                    case R.id.tab_input:
                        f = new com.example.input.fragment_input();
                        frameFlag = "NONE";
                        break;
                    case R.id.tab_settings:
                        f = new fragment_set_info();
                        frameFlag = "NONE";
                        break;
                    case R.id.tab_file:
                        f = new fragment_file();
                        frameFlag = "NONE";
                        break;
                    case R.id.main_btn_search:
                        if (!editText.getText().toString().equals("")) {
                            if (myDao.isInstrumentID(editText.getText().toString())) {
                                f = new fragment_changeID();
                                frameFlag = "NONE";
                            } else {
                                if (!frameFlag.equals("SEARCH")) {
                                    f = new fragment_search();
                                    frameFlag = "SEARCH";
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                Log.i("test", frameFlag);
                ft.replace(R.id.fragment, f);
                ft.commit();
                v.setSelected(true);
            }
        }
    };

    //初始化设定
    private void firstRun() {
        SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
        Boolean first_run = sharedPreferences.getBoolean("First", true);
        set_school_name = sharedPreferences.getString("school", "");
        set_lab_name = sharedPreferences.getString("lab", "");
        if (first_run) {
            Intent intent = new Intent(MainActivity.this, activity_guide_info.class);
            startActivity(intent);
        }
    }


    //region USB函数
    //USB插拔监听
    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED: // 插入USB设备
                    Toast.makeText(MainActivity.this, "插入U盘", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    showCustomizeDialog();
                    isReject = true;
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED: // 拔出USB设备
                    Toast.makeText(MainActivity.this, "拔出U盘", Toast.LENGTH_SHORT).show();
                    isReject = false;
                    break;
                default:
                    break;
            }
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    sta_charge.setVisibility(View.VISIBLE);
                } else {
                    int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    sta_power.setImageLevel(battery);
                    sta_charge.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private class ApReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.example.cupboard.connected")) aerial_state.setSelected(false);
            else if (action.equals("com.example.cupboard.disconnected"))
                aerial_state.setSelected(true);
        }
    }


    //USB跳出对话框
    private void showCustomizeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.usb_attached_dialog, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        ImageView cancel = (ImageView) view.findViewById(R.id.cancel);
        ImageView ok = (ImageView) view.findViewById(R.id.ok);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                excelManager.readExcel();
                if (excelManager.isImport()) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    Fragment f = new fragment_stock();
                    ft.replace(R.id.fragment, f);
                    ft.commit();
                    tab_home.setSelected(false);
                    tab_stock.setSelected(true);
                    tab_data.setSelected(false);
                    tab_input.setSelected(false);
                    tab_settings.setSelected(false);
                    tab_file.setSelected(false);
                    updateData();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //注册广播
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.setPriority(1000);// 设置最高优先级
        filter.addDataScheme("file");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        USBReceiver mUsbReceiver = new USBReceiver();
        this.registerReceiver(mUsbReceiver, filter);
        this.registerReceiver(mUsbReceiver, intentFilter);

        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        BatteryReceiver batteryReceiver = new BatteryReceiver();
        this.registerReceiver(batteryReceiver, mfilter);

        IntentFilter apFilter = new IntentFilter();
        apFilter.addAction("com.example.cupboard.disconnected");
        apFilter.addAction("com.example.cupboard.connected");
        ApReceiver apReceiver = new ApReceiver();
        this.registerReceiver(apReceiver, apFilter);


    }
    //endregion


    //检查重复



/*
    class UdpReceive extends Thread {
        @Override
        public void run() {
            //消息循环
            try {
                //实例化一个服务器套接字,30003为端口号
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true); //设置 ServerSocket 的选项
                serverSocket.bind(new InetSocketAddress(7000)); //与 8080 端口绑定
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                Socket socket = null; //获取套接字
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStream inputStream = null; //服务器socket输入流，从socket中读取数据
                try {
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); //读取客户端传输过来的数据
                String str = "";
                List<String> receiveList = new ArrayList<>();
                try {
                    str = str + bufferedReader.readLine();
                    for (int index = 0; index < str.length(); index = index + 8) {
                        receiveList.add(str.substring(index, index + 8));
                    }
                    if (receiveList.get(0).equals("yy000000")) {
                        PowerOn();
                        dataList.clear();
                        startReceive = true;
                        textToSpeech.speak(Constants.TTS_START, TextToSpeech.QUEUE_FLUSH, null);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        Fragment f = new fragment_dockFake();
                        ft.replace(R.id.fragment, f);
                        ft.commitAllowingStateLoss();
                    } else if (receiveList.get(0).equals("yy000001")) {
                        textToSpeech.speak(Constants.TTS_ALERT, TextToSpeech.QUEUE_FLUSH, null);
                        Message msg = new Message();
                        msg.what = 3;
                        mHandler.sendMessage(msg);
                    } else if (receiveList.get(0).equals("yy000002") || receiveList.get(0).equals("yy000003")) {
                        dataList.add(receiveList.get(0));
                        dataList.add(receiveList.get(1) + receiveList.get(2));
                        for (int i = 3; i < receiveList.size(); i++) {
                            dataList.add(receiveList.get(i));
                        }
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        Fragment f = new fragment_dock();
                        ft.replace(R.id.fragment, f);
                        ft.commitAllowingStateLoss();
                        if (receiveList.get(0).equals("yy000002"))
                            textToSpeech.speak(Constants.TTS_RESULT + "归还仪器" + Integer.toString(dataList.size() - 2) + "件,请确认", TextToSpeech.QUEUE_FLUSH, null);
                        else
                            textToSpeech.speak(Constants.TTS_RESULT + "借用仪器" + Integer.toString(dataList.size() - 2) + "件，请确认", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (receiveList.get(0).equals("yy000004")) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = receiveList.get(1);
                        mHandler.sendMessage(msg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


 */

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }


    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 999:
                    aerial_state.setSelected(((String) msg.obj).equals("F"));
            }
        }
    };


    public void PowerOn() {
        PowerManager mPowerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        mWakeLock.acquire(60 * 1000L /*1 minutes*/);
    }

    private void initEvent() {
        editText.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mLayoutFuzzySearch.setVisibility(View.VISIBLE);
                    mRecyclerSearch.setVisibility(View.VISIBLE);
                } else {
                    mLayoutFuzzySearch.setVisibility(View.GONE);
                    mRecyclerSearch.setVisibility(View.GONE);
                }
            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFuzzySearchAdapter.getFilter().filter(s);
                mFuzzySearchAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void initData() {
        mLayoutFuzzySearch.setVisibility(View.GONE);
        mRecyclerSearch.setVisibility(View.GONE);
        List<String> classList = myDao.getInstruTitle();
        String[] autotext = new String[classList.size()];
        classList.toArray(autotext);
        searchList = fillData(autotext);
        // 这里我们先排序
        Collections.sort(searchList, new LettersComparator<ItemEntity>());
        mRecyclerSearch.setAdapter(mFuzzySearchAdapter = new FuzzySearchAdapter(searchList));
        mFuzzySearchAdapter.setOnItemDeleteClickListener(new FuzzySearchAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                editText.setText(((TextView) mRecyclerSearch.getLayoutManager().getChildAt(i).findViewById(R.id.text_item_name)).getText());
                editText.clearFocus();
            }
        });
    }

    private List<ItemEntity> fillData(String[] date) {
        List<ItemEntity> sortList = new ArrayList<>();
        for (String item : date) {
            String letter;
            //汉字转换成拼音
            List<String> pinyinList = PinyinUtil.getPinYinList(item);
            if (pinyinList != null && !pinyinList.isEmpty()) {
                // A-Z导航
                String letters = pinyinList.get(0).substring(0, 1).toUpperCase();
                // 正则表达式，判断首字母是否是英文字母
                if (letters.matches("[A-Z]")) {
                    letter = letters.toUpperCase();
                } else {
                    letter = "#";
                }
            } else {
                letter = "#";
            }
            sortList.add(new ItemEntity(item, letter, pinyinList));
        }
        return sortList;
    }

    public void updateData() {
        List<ItemEntity> tempList;
        searchList.clear();
        List<String> classList = myDao.getInstruTitle();
        String[] autotext = new String[classList.size()];
        classList.toArray(autotext);
        tempList = fillData(autotext);
        // 这里我们先排序
        Collections.sort(tempList, new LettersComparator<ItemEntity>());
        searchList.addAll(tempList);
        mFuzzySearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(resultCode, data);
            if (scanResult.getContents() != null) {
                final String qrContent = scanResult.getContents();
                Message msg = new Message();
                msg.what = 10;
                msg.obj = qrContent;
                mHandler.sendMessage(msg);
                Log.i("datelog", qrContent);
            }
        }
    }


    public List<String> dataList = new ArrayList<>();
    private List<String> receiveList = new ArrayList<>();
    boolean startReceive = false;

    private void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI("ws://192.168.12.1:8000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.e("Java", "onOpen");
                aerial_state.post(new Runnable(){
                    @Override
                    public void run() {
                        aerial_state.setSelected(false);
                    }
                });
                List<String> mList = myDao.getSync(1);
                List<String> sendList=new ArrayList<>();
                if(mList.size()>0){
                    sendList.add("xx000000");
                    sendList.add("xx000000");
                    sendList.add("xx000000");
                    sendList.addAll(mList);
                    try {
                        sendData(sendList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mList=myDao.getSync(2);
                sendList.clear();
                if(mList.size()>0){
                    sendList.add("xx000008");
                    sendList.add("xx000008");
                    sendList.add("xx000008");
                    sendList.addAll(mList);
                    try {
                        sendData(sendList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mList=myDao.getSync(3);
                sendList.clear();
                if(mList.size()>0){
                    sendList.add("xx000001");
                    sendList.add("xx000001");
                    sendList.add("xx000001");
                    sendList.addAll(mList);
                    try {
                        sendData(sendList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mList=myDao.getSync(4);
                sendList.clear();
                if(mList.size()>0){
                    sendList.add("xx000009");
                    sendList.add("xx000009");
                    sendList.add("xx000009");
                    sendList.addAll(mList);
                    try {
                        sendData(sendList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                myDao.resetSync();
            }

            @Override
            public void onTextReceived(String message) {
                if (message.equals("START")) {
                    isStartReceive = true;
                    receiveList.clear();
                    dataList.clear();
                    Log.i("test", "start");
                } else {
                    if (isStartReceive) {
                        if (!message.equals("END")) {
                            receiveList.add(message);
                            Log.i("test", message);
                        } else {
                            Log.i("test", "end");
                            isStartReceive = false;
                            if (receiveList.get(0).equals("yy000000")) {
                                PowerOn();
                                dataList.clear();
                                startReceive = true;
                                textToSpeech.speak(Constants.TTS_START, TextToSpeech.QUEUE_FLUSH, null);
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                Fragment f = new fragment_dockFake();
                                ft.replace(R.id.fragment, f);
                                ft.commitAllowingStateLoss();
                                frameFlag="dock";
                            } else if (receiveList.get(0).equals("yy000001")) {
                                textToSpeech.speak(Constants.TTS_ALERT, TextToSpeech.QUEUE_FLUSH, null);
                                Message msg = new Message();
                                msg.what = 3;
                                mHandler.sendMessage(msg);
                            } else if (receiveList.get(0).equals("yy000002") || receiveList.get(0).equals("yy000003")) {
                                dataList.add(receiveList.get(0));
                                dataList.add(receiveList.get(1));
                                for (int i = 2; i < receiveList.size(); i++) {
                                    dataList.add(receiveList.get(i));
                                }
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                Fragment f = new fragment_dock();
                                ft.replace(R.id.fragment, f);
                                ft.commitAllowingStateLoss();
                                if (receiveList.get(0).equals("yy000002"))
                                    textToSpeech.speak(Constants.TTS_RESULT + "归还仪器" + Integer.toString(dataList.size() - 2) + "件,请确认", TextToSpeech.QUEUE_FLUSH, null);
                                else
                                    textToSpeech.speak(Constants.TTS_RESULT + "借用仪器" + Integer.toString(dataList.size() - 2) + "件，请确认", TextToSpeech.QUEUE_FLUSH, null);
                            } else if (receiveList.get(0).equals("yy000004")) {
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = receiveList.get(1);
                                mHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                Log.e("J", "onBinaryReceived");
            }

            @Override
            public void onPingReceived(byte[] data) {
                Log.e("J", "onPingReceived");
            }

            @Override
            public void onPongReceived(byte[] data) {
                Log.e("J", "onPongReceived");
            }

            @Override
            public void onException(Exception e) {
                Log.e("Java", "onClosed");
                aerial_state.post(new Runnable(){
                    @Override
                    public void run() {
                        aerial_state.setSelected(true);
                    }
                });
            }

            @Override
            public void onCloseReceived() {
                Log.e("J", "onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        //webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    /**
     * 发送消息给门禁
     * xx000000 录入标签ID
     * xx000001 录入管理ID
     * xx000002 确认门禁标签
     * xx000003 打开门禁摄像头
     * xx000004 取消本次门禁操作
     * xx000005 初始化门禁数据
     * xx000006 关闭门禁摄像头
     * xx000007 发送门禁ADMIN ID
     * xx000008 删除标签ID
     * xx000009 删除管理员ID
     */
    public void sendData(List<String> mList) throws InterruptedException {
        webSocketClient.send("START");
        Thread.sleep(15);
        for (String a : mList) {
            webSocketClient.send(a);
        }
        webSocketClient.send("END");
    }
}

