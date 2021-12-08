package com.example.home;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.guide.MyAdapter;
import com.example.settings.fragment_set_id;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Dao dao;
    MyDao myDao;
    private String select_cupboard_name;
    private int select_cupboard_level;
    TextView tv_sum;
    TextView tv_input;
    TextView tv_borrowed;
    TextView tv_scrapped;
    private GridView gridView;
    private ListView listView;
    private List<String> cupboard_name = new ArrayList<>();
    ;
    private List<String> cupboard_state = new ArrayList<>();
    private List<HashMap<String, String>> detailList;
    private HomeAdapter adapter;
    private DetailAdapter detailAdapter;

    ImageView imageView;
    TextView tvCup;
    ImageView imageViewState;
    ImageView btnScanning;
    ImageView btnConfirm;
    ImageView imageViewCup;
    TextView tvExplain;

    String instrument_scrap;
    List<String> searchedInstru = new ArrayList<>(); //已扫描ID
    ArrayList<String> removeList = new ArrayList<>();
    HashMap<String, String> instrumentMap = new HashMap<>(); //ID映射name
    ArrayList<String> instrumentIdList = new ArrayList<>();
    List<String> adminList = new ArrayList<>();//管理员ID列表

    List<HashMap<String, String>> tempList = new ArrayList<>();

    //长期未归还
    List<HashMap<String, String>> returnList;
    ReturnAdapter returnAdapter;


    private IUHFService iuhfService;
    boolean isRun = false;
    boolean finish = false;
    String label;
    int numScrapped;
    boolean startSearch = false;
    boolean startPanDian = false;
    boolean startCheck = false;
    int imageLevel = 0;
    int numInCup = 0;
    boolean isOpen = false;

    private ImageView dialog_background;
    private TextView dialog_tv;
    private ImageView dialog_ok;

    private String admin_name;
    private MainActivity mainActivity;
    private String returnDays;

    public fragment_home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_home.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_home newInstance(String param1, String param2) {
        fragment_home fragment = new fragment_home();
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
        dao = new Dao(this.getContext());
        myDao = new MyDao(this.getContext());
        cupboard_name = myDao.getCupName();
        cupboard_state = myDao.getCupState();
        adminList = myDao.getAdminID();
        iuhfService = UHFManager.getUHFService(this.getContext());
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                label = spdInventoryData.epc;
                if (startSearch) {
                    String cupId = myDao.getCupboardID(select_cupboard_name);
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
                if (startPanDian) {
                    if (label.length() == Constants.EPC_LENGTH_LABEL) {
                        if (instrumentMap.get(label) != null) {
                            if (searchedInstru.isEmpty()) {
                                searchedInstru.add(label);
                                updateStock(instrumentMap.get(label));
                            } else {
                                boolean isnew = true;
                                for (int i = 0; i < searchedInstru.size(); i++) {
                                    String mEPC = searchedInstru.get(i);
                                    //list中有此EPC
                                    if (label.equals(mEPC)) {
                                        isnew = false;
                                        break;
                                    }
                                }
                                if (isnew) {
                                    //list中没有此epc
                                    searchedInstru.add(label);
                                    updateStock(instrumentMap.get(label));
                                }
                            }
                        }
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
            public void onInventoryStatus(int i) {
                //TODO 盘点失败回调

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        this.gridView = view.findViewById(R.id.home_gridview);
        this.listView = view.findViewById(R.id.home_listview);
        adapter = new HomeAdapter(this.getContext(), cupboard_state);
        mainActivity = (MainActivity) getActivity();
        ImageView cupboard_background = view.findViewById(R.id.home_cupboard_background);
        ImageView IV_cupboard = view.findViewById(R.id.home_state);
        ImageView IV_synchronization = view.findViewById(R.id.home_synchronization);
        ImageView IV_back = view.findViewById(R.id.home_back);
        TextView TV_cupboard = view.findViewById(R.id.home_name);
        TextView TV_maintain=view.findViewById(R.id.home_maintain);
        int maintainData=myDao.getMaintainData();
        TV_maintain.setText("("+maintainData+")待保养");

        tv_sum = view.findViewById(R.id.home_tv_sum);
        tv_input = view.findViewById(R.id.home_tv_input);
        tv_borrowed = view.findViewById(R.id.home_tv_borrowed);
        tv_scrapped = view.findViewById(R.id.home_tv_scrap);

        Init_State();

        gridView.setAdapter(adapter);

        IV_synchronization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!IV_synchronization.isSelected()) {
                    clearStock();
                    IV_synchronization.setSelected(true);
                    searchedInstru.clear();
                    instrumentMap = myDao.getStockInstruMap();
                    iuhfService.inventoryStart();
                    isRun = true;
                    startPanDian = true;
                } else {
                    startPanDian = false;
                    IV_synchronization.setSelected(false);
                    iuhfService.inventoryStop();
                }
            }
        });

        adapter.setOnItemDeleteClickListener(new MyAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                select_cupboard_name = cupboard_name.get(i);
                select_cupboard_level = Integer.parseInt(cupboard_state.get(i).substring(3));
                gridView.setVisibility(View.INVISIBLE);
                cupboard_background.setVisibility(View.VISIBLE);
                IV_cupboard.setVisibility(View.VISIBLE);
                IV_cupboard.setImageLevel(select_cupboard_level);
                TV_cupboard.setVisibility(View.VISIBLE);
                TV_cupboard.setText(select_cupboard_name);
                IV_synchronization.setVisibility(View.VISIBLE);
                IV_synchronization.setSelected(false);
                IV_back.setVisibility(View.VISIBLE);
                iuhfService.openDev();
                isOpen=true;
                detailList = myDao.getCupStock(select_cupboard_name);
                setAdapter(detailList);
                listView.setAdapter(detailAdapter);
                listView.setVisibility(View.VISIBLE);


                IV_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gridView.setVisibility(View.VISIBLE);
                        cupboard_background.setVisibility(View.INVISIBLE);
                        IV_cupboard.setVisibility(View.INVISIBLE);
                        TV_cupboard.setVisibility(View.INVISIBLE);
                        IV_synchronization.setVisibility(View.INVISIBLE);
                        IV_back.setVisibility(View.INVISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        iuhfService.inventoryStop();
                        iuhfService.closeDev();
                    }
                });
            }
        });

        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("FirstRun", 0);
        returnDays = sharedPreferences.getString("days", "30");
        TextView tv_return = view.findViewById(R.id.home_instru_to_return);
        tv_return.setText("长期未归还>" + returnDays + "天");
        long timeSeconds = System.currentTimeMillis();
        long borrowSeconds = timeSeconds / 1000 - Integer.parseInt(returnDays) * 24 * 3600;
        returnList = myDao.getReturnData(borrowSeconds);
        returnAdapter = new ReturnAdapter(this.getContext(), returnList);
        ListView listViewReturn = view.findViewById(R.id.home_return_listview);
        listViewReturn.setAdapter(returnAdapter);
        new Thread(new checkReturnThread()).start();
        return view;
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

    private void Init_State() {
        HashMap<String, Integer> map = myDao.getHomeData();
        tv_sum.setText(Integer.toString(map.get("sum")));
        tv_input.setText(Integer.toString(map.get("add_new")));
        tv_borrowed.setText(Integer.toString(map.get("borrowed")));
        tv_scrapped.setText(Integer.toString(map.get("scrapped")));
    }

    private void setAdapter(List<HashMap<String, String>> list) {
        detailAdapter = new DetailAdapter(this.getContext(), list);
        detailAdapter.setOnItemDeleteClickListener(new DetailAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                instrument_scrap = detailList.get(i).get("instrument_name");
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });
    }


    private void showScrapDialog(String instruName) {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_scrap, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
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
        numInCup = myDao.getInstruNum(select_cupboard_name, instrument_scrap);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRun) {
                    startSearch = false;
                    iuhfService.inventoryStop();
                }
                searchedInstru.clear();
                instrumentIdList.clear();
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
                if (imageLevel == 3) {
                    numScrapped = 0;
                    imageView.setSelected(true);
                    imageLevel = 7;
                    btnScanning.setImageLevel(imageLevel);
                } else if (imageLevel == 7) {
                    iuhfService.inventoryStop();
                    numScrapped = numInCup - searchedInstru.size();
                    imageView.setSelected(false);
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                    finish = true;
                    startSearch = false;
                }
            }
        });
        dialog.show();
    }


    public void reSearch(String label_id) {
        if (instrumentIdList.contains(label_id)) {
            if (searchedInstru.isEmpty()) {
                searchedInstru.add(label_id);
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
                }
            }
        }
    }

    private void insertScrapLog(String name, int number) {
        myDao.insertSheet("报废", name, number, admin_name);
    }

    private void showNumScrapping() {
        fragment_home.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvExplain.setText("柜内原有" + instrument_scrap + numInCup + "个," + "扫描到" + searchedInstru.size() + "个");
            }
        });
    }

    private void changeAdapter() {
        fragment_home.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailList = myDao.getCupStock(select_cupboard_name);
                setAdapter(detailList);
                listView.setAdapter(detailAdapter);
                finish = false;
            }
        });
    }

    private void clearStock() {
        fragment_home.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailList.clear();
                //tempList = dao.getCupStock(select_cupboard_name, 1);
                tempList = myDao.getCupStock(select_cupboard_name, 1);
                detailAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateStock(String name) {
        fragment_home.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailList.clear();
                for (HashMap<String, String> a : tempList) {
                    if (name.equals(a.get("instrument_name"))) {
                        int stock = Integer.parseInt(a.get("stock")) + 1;
                        a.put("stock", Integer.toString(stock));
                    }
                    if (!a.get("stock").equals("0")) detailList.add(a);
                }
                detailAdapter.notifyDataSetChanged();
            }
        });
    }


    public static ArrayList<String> listRemove(List<String> listA, List<String> listB) {
        HashSet hs1 = new HashSet(listA);
        HashSet hs2 = new HashSet(listB);
        hs1.removeAll(hs2);
        ArrayList<String> listC = new ArrayList<>();
        listC.addAll(hs1);
        return listC;
    }

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
                            Toast.makeText(fragment_home.this.getContext(), "报废完成", Toast.LENGTH_SHORT).show();
                        } else {
                            if (removeList.size() > 0) {
                                myDao.insertSync(2, removeList);
                                Toast.makeText(fragment_home.this.getContext(), "报废完成，门禁未连接，请同步", Toast.LENGTH_LONG).show();
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
                    showScrapDialog(instrument_scrap);
                    break;
                case 99:
                    long timeSeconds = System.currentTimeMillis();
                    long borrowSeconds = timeSeconds / 1000 - Integer.parseInt(returnDays) * 24 * 3600;
                    returnList.clear();
                    List<HashMap<String, String>> testList = myDao.getReturnData(borrowSeconds);
                    returnList.addAll(testList);
                    returnAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

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
                iuhfService.inventoryStop();
                dialog.dismiss();
            }
        });

        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheck = false;
                iuhfService.inventoryStop();
                dialog.dismiss();
                Message msg = new Message();
                msg.what = 3;
                handler.sendMessage(msg);
            }
        });
        dialog.show();
    }

    public class checkReturnThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    Thread.sleep(1000 * 3600 * 24);// 线程暂停10秒，单位毫秒
                    Message message = new Message();
                    message.what = 99;
                    handler.sendMessage(message);// 发送消息
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}