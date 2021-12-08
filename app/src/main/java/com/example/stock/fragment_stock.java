package com.example.stock;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.file.ExcelUtil;
import com.example.file.ExportData;
import com.example.home.fragment_home;
import com.example.input.fragment_input;
import com.example.scrap.fragment_scrap;
import com.example.settings.fragment_test;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.tuacy.azlist.AZTitleDecoration;
import com.tuacy.azlist.AZWaveSideBarView;
import com.tuacy.azlist.LettersComparator;
import com.tuacy.fuzzysearchlibrary.PinyinUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_stock#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_stock extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private AZWaveSideBarView mSideBarView;
    private boolean isOpen=false;


    //报废参数
    boolean startCheck = false;
    private List<String> cupList = new ArrayList<>();
    private ImageView dialog_background;
    private TextView dialog_tv;
    private ImageView dialog_ok;

    ImageView imageView;
    TextView tvCup;
    ImageView imageViewState;
    ImageView btnScanning;
    ImageView btnConfirm;
    ImageView imageViewCup;
    TextView tvExplain;
    private String admin_name;
    String instrument_scrap;

    int imageLevel = 0;
    int numInCup = 0;

    //文件参数
    String tabTag = null;
    List<String> searchedInstru = new ArrayList<>(); //已扫描ID
    List<String> searchedCup = new ArrayList<>();
    HashMap<String, String> instrumentMap; //ID映射name

    //UI参数
    private GridView gridView;
    private ImageView btnExport;
    private ImageView btnInventory;
    private List<String> mList = new ArrayList<>();
    List<String> adminList = new ArrayList<>();//管理员ID列表
    private List<ItemEntity> detailList = new ArrayList<>();
    private TabAdapter adapter;
    private DetailAdapter detailAdapter;
    private IUHFService iuhfService;
    private TextView instru_sum;
    private MainActivity mainActivity;
    private int cupboardNumber;
    private int instruAll;
    private String cupId;

    MyDao myDao;

    public fragment_stock() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_stock.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_stock newInstance(String param1, String param2) {
        fragment_stock fragment = new fragment_stock();
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
        iuhfService = UHFManager.getUHFService(this.getContext());
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                String label = spdInventoryData.epc;
                if (startSearch) {
                    if (!imageViewState.isSelected()) {
                        if (label.equals(cupId)) {
                            imageViewState.setSelected(true);
                            imageLevel = 3;
                            btnScanning.setImageLevel(imageLevel);
                            instrumentIdList = myDao.getIdFromCup(select_cupboard_name, instrument_scrap);
                        }
                    }
                    if (imageView.isSelected()) {
                        reSearch(label);
                        showNumScrapping();
                    }

                }

                if (startCheck) {
                    if (label.length() == Constants.EPC_LENGTH_ADMIN) {
                        for (String ID : adminList) {
                            if (label.equals(ID)) {
                                admin_name = myDao.getAdminName(ID);
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = admin_name;
                                handler.sendMessage(msg);
                                iuhfService.inventoryStop();
                            }
                        }
                    }
                }
            }

            @Override
            public void onInventoryStatus(int status) {

            }
        });
    }

    public void onDestroy() {
        if (iuhfService != null) {
            if (isOpen) {
                iuhfService.inventoryStop();
                iuhfService.closeDev();
            }
            iuhfService = null;
            UHFManager.closeUHFService();
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        mContext = this.getContext();
        gridView = view.findViewById(R.id.stock_title_gridview);
        mRecyclerView = view.findViewById(R.id.stock_recycler_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new AZTitleDecoration(new AZTitleDecoration.TitleAttributes(mContext)));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        mSideBarView = view.findViewById(R.id.stock_bar_list);
        myDao = new MyDao(mContext);
        adminList = myDao.getAdminID();
        btnExport = view.findViewById(R.id.stock_btn_export);
        btnInventory = view.findViewById(R.id.stock_inventory);
        instru_sum = view.findViewById(R.id.stock_instru_sum);
        instru_sum.setText(myDao.getStockSum());
        mainActivity = (MainActivity) getActivity();
        InitList();
        setTabAdapter();
        initEvent();
        tabTag = "所有仪器";
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeSeconds = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String Date = sdf.format(timeSeconds);
                String name = Date + "\n" + "库存清单\n" + tabTag;
                String fileTpye = "库存清单";
                exportExcel(mContext);
                Toast.makeText(mContext, "导出成功", Toast.LENGTH_LONG).show();
            }
        });

        btnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnInventory.setEnabled(false);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_inventory(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });
        gridView.post(new Runnable() {
            @Override
            public void run() {
                detailAdapter = new DetailAdapter(detailList, mContext);
                getDetailData();
                detailAdapter.setOnItemDeleteClickListener(new DetailAdapter.onItemDeleteListener() {

                    @Override
                    public void onDeleteClick(int i) {
                        if (detailList.get(i).getSum() != 0) {
                            iuhfService.openDev();
                            isOpen=true;
                            instrument_scrap = detailList.get(i).getValue();
                            cupList = myDao.getCupName(detailList.get(i).getValue());
                            select_cupboard_name = cupList.get(0);
                            showAdminDialog();
                        }
                    }
                });
                mRecyclerView.setAdapter(detailAdapter);
            }
        });
        return view;
    }


    private void InitList() {
        mList = myDao.getClassName();
        String str = "1" + mList.get(0);
        mList.set(0, str);
        for (int i = 1; i < mList.size(); i++) {
            str = "0" + mList.get(i);
            mList.set(i, str);
        }
    }

    private void setTabAdapter() {
        adapter = new TabAdapter(mContext, mList);
        gridView.setAdapter(adapter);
        adapter.setOnItemDeleteClickListener(new TabAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                String str;
                for (int j = 0; j < mList.size(); j++) {
                    str = "0" + mList.get(j).substring(1);
                    mList.set(j, str);
                }
                str = "1" + mList.get(i).substring(1);
                mList.set(i, str);
                String class_name = mList.get(i).substring(1);
                if (i == 0) {
                    getDetailData();
                    tabTag = "所有仪器";
                } else {
                    getDetailData(class_name);
                    tabTag = class_name;
                }
                adapter.notifyDataSetChanged();
                detailAdapter.notifyDataSetChanged();
            }
        });
    }


    private void exportExcel(Context context) {
        String filePath = "/storage/emulated/0/Rfid/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        long timeSeconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = sdf.format(timeSeconds);
        String excelFileName = "库存清单 " + tabTag + date + ".xls";
        myDao.insertFile("库存清单", excelFileName);
        String[] title = {"仪器名称", "存放位置", "现有库存", "总计库存", "利用次数", "累计报废"};

        List<ExportData> demoBeanList = new ArrayList<>();
        for (ItemEntity itemEntity : detailList) {
            String type = "库存";
            String name = itemEntity.getValue();
            String location = "";
            List<String> mList = myDao.getCupName(name);
            for (String temp : mList) {
                location = location + "、" + temp;
            }
            if (location.length() > 0) location = location.substring(1);
            String remain = Integer.toString(itemEntity.getStock());
            String stock = Integer.toString(itemEntity.getSum());
            String used = Integer.toString(itemEntity.getUsed());
            String scrapped = Integer.toString(itemEntity.getScrapped());
            ExportData exportData = new ExportData(type, name, location, remain, stock, used, scrapped);
            demoBeanList.add(exportData);
        }
        filePath = filePath + excelFileName;
        ExcelUtil.initExcel(filePath, date, title);
        ExcelUtil.writeObjListToExcel(demoBeanList, filePath, context);
    }


    private void initEvent() {
        mSideBarView.setOnLetterChangeListener(new AZWaveSideBarView.OnLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                int position = detailAdapter.getSortLettersFirstPosition(letter);
                if (position != -1) {
                    if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                        manager.scrollToPositionWithOffset(position, 0);
                    } else {
                        mRecyclerView.getLayoutManager().scrollToPosition(position);
                    }
                }
            }
        });

    }

    private void getDetailData() {
        List<HashMap<String, String>> mapList = myDao.getStock();
        detailList.clear();
        for (HashMap<String, String> map : mapList) {
            String item = map.get("name");
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
            ItemEntity itemEntity = new ItemEntity(item, letter, pinyinList);
            itemEntity.setStock(Integer.parseInt(map.get("stock_in")));
            itemEntity.setSum(Integer.parseInt((map.get("stock"))));
            itemEntity.setUsed(Integer.parseInt(map.get("input")));
            itemEntity.setScrapped(Integer.parseInt(map.get("scrap")));
            detailList.add(itemEntity);
        }
        // 这里我们先排序
        Collections.sort(detailList, new LettersComparator<ItemEntity>());

    }

    private void getDetailData(String class_name) {
        List<HashMap<String, String>> mapList = myDao.getStock(class_name);
        detailList.clear();
        for (HashMap<String, String> map : mapList) {
            String item = map.get("name");
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
            ItemEntity itemEntity = new ItemEntity(item, letter, pinyinList);
            itemEntity.setStock(Integer.parseInt(map.get("stock_in")));
            itemEntity.setSum(Integer.parseInt((map.get("stock"))));
            itemEntity.setUsed(Integer.parseInt(map.get("input")));
            itemEntity.setScrapped(Integer.parseInt(map.get("scrap")));
            detailList.add(itemEntity);
        }
        // 这里我们先排序
        Collections.sort(detailList, new LettersComparator<ItemEntity>());
    }

    private void showAdminDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_checkid, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        startCheck = true;
        dialog_background = (ImageView) view.findViewById(R.id.input_dialog_background);
        dialog_tv = (TextView) view.findViewById(R.id.input_dialog_tv);
        ImageView cancel = (ImageView) view.findViewById(R.id.input_dialog_cancel);
        dialog_ok = (ImageView) view.findViewById(R.id.input_dialog_confirm);
        iuhfService.inventoryStart();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin_name = "";
                startCheck = false;
                if(mainActivity.tab_stock.isSelected()){
                    iuhfService.inventoryStop();
                }
                dialog.dismiss();
            }
        });

        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheck = false;
                if(mainActivity.tab_stock.isSelected()){
                    iuhfService.inventoryStop();
                }
                dialog.dismiss();
                Message msg = new Message();
                msg.what = 3;
                handler.sendMessage(msg);
            }
        });
        dialog.show();
    }

    int numScrapped;
    ArrayList<String> removeList = new ArrayList<>();
    ArrayList<String> instrumentIdList = new ArrayList<>();
    boolean isRun = false;
    boolean startSearch = false;
    boolean finish = false;
    private String select_cupboard_name;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 0:
                    btnScanning.setVisibility(View.INVISIBLE);
                    btnConfirm.setVisibility(View.VISIBLE);
                    btnConfirm.setSelected(true);
                    imageViewCup.setVisibility(View.INVISIBLE);
                    imageViewState.setVisibility(View.INVISIBLE);
                    tvCup.setVisibility(View.INVISIBLE);
                    tvExplain.setText("\n" + "本次共报废“" + instrument_scrap + "”" + "      " + numScrapped + "\n"
                            + "剩余库存           " + searchedInstru.size());
                    if (numScrapped != 0) {
                        insertScrapLog(instrument_scrap, numScrapped);
                        myDao.updateScrap(instrument_scrap, numScrapped);
                        removeList = listRemove(instrumentIdList, searchedInstru);
                        myDao.updateCupboard(instrument_scrap, removeList);

                        if (!mainActivity.aerial_state.isSelected()) {
                            List<String> sendList = new ArrayList<>();
                            if (removeList.size() > 0) {
                                sendList.add("xx000008");
                                sendList.add("xx000008");
                                sendList.add("xx000008");
                                sendList.addAll(removeList);
                                try {
                                    mainActivity.sendData(sendList);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            Toast.makeText(fragment_stock.this.getContext(), "报废完成", Toast.LENGTH_SHORT).show();
                        } else {
                            if (removeList.size() > 0) {
                                myDao.insertSync(2, removeList);
                                Toast.makeText(fragment_stock.this.getContext(), "报废完成，门禁未连接，请同步", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    searchedInstru.clear();
                    instrumentIdList.clear();
                    break;
                case 1:
                    dialog_background.setSelected(true);
                    dialog_tv.setText(admin_name);
                    dialog_ok.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    showAdminDialog();
                    break;
                case 3:
                    if(mainActivity.tab_stock.isSelected()) {
                        showScrapDialog(instrument_scrap);
                    }
                    break;
                case 10:
                    btnScanning.setImageLevel(3);
                    imageView.setSelected(false);
                    select_cupboard_name = cupList.get(cupboardNumber);
                    tvCup.setText(select_cupboard_name);
                    Log.i("scrap", select_cupboard_name + "  " + cupboardNumber);
                    numInCup = myDao.getInstruNum(select_cupboard_name, instrument_scrap);
                    cupId = myDao.getCupboardID(select_cupboard_name);
                    instruAll = instruAll + numInCup;
                    imageViewState.setSelected(false);
                    searchedCup.clear();
                    tvExplain.setText("请扫描下方智能柜");
                    break;
            }
        }
    };


    private void showScrapDialog(String instruName) {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_scrap, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        cupboardNumber = 0;
        TextView tvInstru = view.findViewById(R.id.dialog_scrap_instru);
        tvExplain = view.findViewById(R.id.dialog_scrap_explain);
        ImageView btnCancel = view.findViewById(R.id.dialog_scrap_cancel);
        btnConfirm = view.findViewById(R.id.dialog_scrap_btn);
        btnScanning = view.findViewById(R.id.dialog_scanning);
        imageView = view.findViewById(R.id.dialog_scrap_title);
        imageViewCup = view.findViewById(R.id.dialog_cup);
        imageViewCup.setImageLevel(9);
        tvCup = view.findViewById(R.id.dialog_cupName);
        imageViewState = view.findViewById(R.id.dialog_cupState);
        String explain = "1.确认需报废的“ " + instruName + " ”已远离智能柜\n" + "\n" + "2.确认非报废的“ " + instruName + " ”已归还智能柜";
        tvInstru.setText(instruName);
        tvExplain.setText(explain);
        instruAll = 0;
        cupId = myDao.getCupboardID(select_cupboard_name);
        numInCup = myDao.getInstruNum(select_cupboard_name, instrument_scrap);
        instruAll = instruAll + numInCup;
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRun) {
                    startSearch = false;
                    iuhfService.inventoryStop();
                }
                dialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finish) {
                    btnScanning.setVisibility(View.VISIBLE);
                    btnScanning.setImageLevel(0);
                    btnConfirm.setVisibility(View.INVISIBLE);
                    imageViewCup.setVisibility(View.VISIBLE);
                    imageViewState.setVisibility(View.VISIBLE);
                    tvCup.setText(select_cupboard_name);
                    tvCup.setVisibility(View.VISIBLE);
                    imageView.setSelected(false);
                    startSearch = true;
                    isRun = true;
                    tvExplain.setText("请分别扫描下方智能柜");
                    iuhfService.inventoryStart();
                } else {
                    iuhfService.inventoryStop();
                    changeAdapter();
                    dialog.dismiss();

                }
            }
        });

        btnScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (imageLevel){
                    case 3:
                        numScrapped = 0;
                        imageView.setSelected(true);
                        if (cupboardNumber < cupList.size()-1) imageLevel = 5;
                        else imageLevel = 7;
                        btnScanning.setImageLevel(imageLevel);
                        break;
                    case 5:
                        Log.i("scrap", "start");
                        cupboardNumber = cupboardNumber + 1;
                        Message msg = new Message();
                        msg.what = 10;
                        handler.sendMessage(msg);
                        break;
                    case 7:
                        Log.i("scrap", "stop");
                        iuhfService.inventoryStop();
                        numScrapped = instruAll - searchedInstru.size();
                        imageView.setSelected(false);
                        Message msg1 = new Message();
                        msg1.what = 0;
                        handler.sendMessage(msg1);
                        finish = true;
                        startSearch = false;
                        break;
                }
            }
        });
        dialog.show();
    }

    private void insertScrapLog(String name, int number) {
        myDao.insertSheet("报废", name, number, admin_name);
    }

    public static ArrayList<String> listRemove(List<String> listA, List<String> listB) {
        HashSet hs1 = new HashSet(listA);
        HashSet hs2 = new HashSet(listB);
        hs1.removeAll(hs2);
        ArrayList<String> listC = new ArrayList<>();
        listC.addAll(hs1);
        return listC;
    }

    public void reSearch(String label_id) {
        if (instrumentIdList.contains(label_id)) {
            if (searchedInstru.isEmpty()) {
                searchedInstru.add(label_id);
                searchedCup.add(label_id);
            } else {
                boolean isnew = true;
                for (int i = 0; i < searchedInstru.size(); i++) {
                    String mEPC = searchedInstru.get(i);
                    //list中有此EPC
                    if (label_id.equals(mEPC)) {
                        isnew = false;
                        break;
                    }
                }
                if (isnew) {
                    //list中没有此epc
                    searchedInstru.add(label_id);
                    searchedCup.add(label_id);
                }
            }
        }
    }

    private void showNumScrapping() {
        fragment_stock.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvExplain.setText("原有" + instrument_scrap + numInCup + "个," + "扫描到" + searchedCup.size() + "个");
            }
        });
    }

    private void changeAdapter() {
        fragment_stock.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getDetailData();
                finish = false;
                detailAdapter.notifyDataSetChanged();
                iuhfService.closeDev();
            }
        });
    }
}
