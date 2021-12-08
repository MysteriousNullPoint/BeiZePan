package com.example.stock;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.R;
import com.example.file.ExcelUtil;
import com.example.file.ExportData;
import com.example.input.Instrument;
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
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_inventory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_inventory extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int flag = 0;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private AZWaveSideBarView mSideBarView;
    MyDao myDao;
    IUHFService iuhfService;
    private List<ItemEntity> detailList = new ArrayList<>();
    private List<ItemEntity> allList = new ArrayList<>();
    private List<ItemEntity> errorList = new ArrayList<>();
    InventoryAdapter inventoryAdapter;

    List<String> searchedInstru = new ArrayList<>();
    List<String> missingInstru = new ArrayList<>();
    List<String> missedAll = new ArrayList<>();
    HashMap<String, String> instruMap = new HashMap<>();
    List<String> stockInList = new ArrayList<>();


    private ImageView dialog_background;
    private TextView dialog_tv;
    private ImageView dialog_ok;
    private TextView tv_Admin;
    private TextView detail_instru;
    private TextView tv_sum;
    private TextView tv_error;
    private TextView tv_inventory;
    private List<String> adminList = new ArrayList<>();
    String admin_name = "";
    private boolean isOpen = false;

    private String detailInstruName;

    public fragment_inventory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_inventory.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_inventory newInstance(String param1, String param2) {
        fragment_inventory fragment = new fragment_inventory();
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
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        mContext = this.getContext();
        ImageView tab_all = view.findViewById(R.id.inventory_tab_all);
        ImageView tab_error = view.findViewById(R.id.inventory_tab_error);
        ImageView btn_startScan = view.findViewById(R.id.inventory_startScan);
        ImageView btn_cancel = view.findViewById(R.id.inventory_btn_cancel);
        ImageView btn_save = view.findViewById(R.id.inventory_btn_save);
        TextView tv_date = view.findViewById(R.id.inventory_date);
        tv_sum = view.findViewById(R.id.inventory_tv_sum);
        tv_inventory = view.findViewById(R.id.inventory_tv_total);

        tv_error = view.findViewById(R.id.inventory_tv_error);
        tv_Admin = view.findViewById(R.id.inventory_admin);
        myDao = new MyDao(mContext);
        tv_inventory.setText(myDao.getStockSum());
        tv_sum.setText("0");
        showAdminDialog();
        adminList = myDao.getAdminID();
        btn_startScan.setImageLevel(1);
        iuhfService = UHFManager.getUHFService(mContext);
        iuhfService.openDev();
        isOpen=true;
        iuhfService.inventoryStart();
        tab_all.setSelected(true);

        stockInList = myDao.getStockIn();
        instruMap = myDao.getStockInstruMap();
        inventoryAdapter = new InventoryAdapter(detailList, mContext);
        inventoryAdapter.setOnItemDeleteClickListener(new InventoryAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                detailInstruName = detailList.get(i).getValue();
                List<String> instruList = myDao.getStockInstruMap(detailInstruName);
                missingInstru = listRemove(instruList, searchedInstru);
                Message msg = new Message();
                msg.what = 3;
                handler.sendMessage(msg);
            }
        });
        mRecyclerView = view.findViewById(R.id.inventory_recycler_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new AZTitleDecoration(new AZTitleDecoration.TitleAttributes(mContext)));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(inventoryAdapter);
        mSideBarView = view.findViewById(R.id.inventory_bar_list);
        initEvent();

        tab_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab_all.setSelected(true);
                tab_error.setSelected(false);
                tv_inventory.setTextColor(getResources().getColor(R.color.blue));
                tv_error.setTextColor(getResources().getColor(R.color.white));
                detailList.clear();
                detailList.addAll(allList);
                inventoryAdapter.notifyDataSetChanged();
            }
        });

        tab_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab_all.setSelected(false);
                tab_error.setSelected(true);
                tv_inventory.setTextColor(getResources().getColor(R.color.white));
                tv_error.setTextColor(getResources().getColor(R.color.blue));
                updateList();
                detailList.clear();
                detailList.addAll(errorList);
                inventoryAdapter.notifyDataSetChanged();
            }
        });

        btn_startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (flag) {
                    case 0:
                    case 2:
                        btn_startScan.setImageLevel(11);
                        iuhfService.inventoryStart();
                        detailList.clear();
                        searchedInstru.clear();
                        inventoryAdapter.notifyDataSetChanged();
                        long timeSeconds = System.currentTimeMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                        String Date = sdf.format(timeSeconds);
                        tv_date.setText(Date);
                        flag = 1;
                        break;
                    case 1:
                        btn_startScan.setImageLevel(21);
                        missedAll = listRemove(stockInList, searchedInstru);
                        tv_error.setText(Integer.toString(missedAll.size()));
                        completeAdapter(detailList, missedAll);
                        allList.addAll(detailList);
                        iuhfService.inventoryStop();
                        inventoryAdapter.notifyDataSetChanged();
                        flag = 2;
                        break;
                    case 10:
                        Toast.makeText(mContext, "请验证管理员身份", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_stock(), null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeSeconds = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String Date = sdf.format(timeSeconds);
                String name = Date + "\n" + "盘点清单\n";
                String fileTpye = "库存清单";
                exportExcel(mContext);
                Toast.makeText(fragment_inventory.this.getContext(), "保存成功", Toast.LENGTH_SHORT).show();
            }
        });

        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                String label_id = spdInventoryData.epc;
                switch (flag) {
                    case 1:
                        if (label_id.length() == Constants.EPC_LENGTH_LABEL) {
                            if (searchedInstru.isEmpty() && instruMap.get(label_id) != null) {
                                searchedInstru.add(label_id);
                                changeAdapter(detailList, instruMap.get(label_id));
                                Message msg = new Message();
                                msg.what = 0;
                                handler.sendMessage(msg);
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
                                    if (instruMap.get(label_id) != null) {
                                        searchedInstru.add(label_id);
                                        changeAdapter(detailList, instruMap.get(label_id));
                                        Message msg = new Message();
                                        msg.what = 0;
                                        handler.sendMessage(msg);
                                    }
                                }
                            }
                        }
                        break;
                    case 10:
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

    private void changeAdapter(List<ItemEntity> mList, String instruName) {
        if (mList.isEmpty()) {
            String item = instruName;
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
            HashMap<String, Integer> map = myDao.getInventoryDetail(instruName);
            int remain = map.get("stock");
            int stock = map.get("remain");
            itemEntity.setStock(remain);
            itemEntity.setSum(stock);
            itemEntity.setScrapped(1);
            mList.add(itemEntity);
        } else {
            int i;
            boolean isNew = true;
            for (i = 0; i < mList.size(); i++) {
                if (mList.get(i).getValue().equals(instruName)) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                String item = instruName;
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
                HashMap<String, Integer> map = myDao.getInventoryDetail(instruName);
                int remain = map.get("stock");
                int stock = map.get("remain");
                itemEntity.setStock(remain);
                itemEntity.setSum(stock);
                itemEntity.setScrapped(1);
                mList.add(itemEntity);
            } else {
                mList.get(i).addScrapped();
            }
        }
        Collections.sort(mList, new LettersComparator<ItemEntity>());
    }

    private void completeAdapter(List<ItemEntity> mList, List<String> itemList) {
        for (String id : itemList) {
            String instruName = instruMap.get(id);
            if (mList.isEmpty()) {
                String item = instruName;
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
                HashMap<String, Integer> map = myDao.getInventoryDetail(instruName);
                int remain = map.get("stock");
                int stock = map.get("remain");
                itemEntity.setStock(remain);
                itemEntity.setSum(stock);
                itemEntity.setScrapped(0);
                mList.add(itemEntity);
                Log.i("inventory", instruName);
            } else {
                int i;
                boolean isNew = true;
                for (i = 0; i < mList.size(); i++) {
                    if (mList.get(i).getValue().equals(instruName)) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    String item = instruName;
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
                    HashMap<String, Integer> map = myDao.getInventoryDetail(instruName);
                    int remain = map.get("stock");
                    int stock = map.get("remain");
                    itemEntity.setStock(remain);
                    itemEntity.setSum(stock);
                    itemEntity.setScrapped(0);
                    mList.add(itemEntity);
                    Log.i("inventory", instruName);
                }
            }
        }
    }


    private void initEvent() {
        mSideBarView.setOnLetterChangeListener(new AZWaveSideBarView.OnLetterChangeListener() {
            @Override
            public void onLetterChange(String letter) {
                int position = inventoryAdapter.getSortLettersFirstPosition(letter);
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


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 0:
                    inventoryAdapter.notifyDataSetChanged();
                    tv_sum.setText(Integer.toString(searchedInstru.size()));
                    missedAll = listRemove(stockInList, searchedInstru);
                    tv_error.setText(Integer.toString(missedAll.size()));
                    break;
                case 1:
                    dialog_background.setSelected(true);
                    dialog_tv.setText(admin_name);
                    dialog_ok.setVisibility(View.VISIBLE);
                    tv_Admin.setText(admin_name);
                    break;
                case 2:
                    tv_Admin.setText("");
                    break;
                case 3:
                    showDetailDialog();
            }
        }
    };

    private void showAdminDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_checkid, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        flag = 10;
        dialog_background = (ImageView) view.findViewById(R.id.input_dialog_background);
        dialog_tv = (TextView) view.findViewById(R.id.input_dialog_tv);
        ImageView cancel = (ImageView) view.findViewById(R.id.input_dialog_cancel);
        dialog_ok = (ImageView) view.findViewById(R.id.input_dialog_confirm);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin_name = "";
                iuhfService.inventoryStop();
                dialog.dismiss();
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });

        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iuhfService.inventoryStop();
                dialog.dismiss();
                flag = 0;
            }
        });
        dialog.show();
    }

    private void showDetailDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_inventory_result, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        ImageView dialog_lack_in = view.findViewById(R.id.inventory_lack_check);
        ImageView dialog_lack_add = view.findViewById(R.id.inventory_lack_add);
        ImageView dialog_lack_scrap = view.findViewById(R.id.inventory_lack_scrap);
        detail_instru = view.findViewById(R.id.inventory_lack_instru);
        detail_instru.setText("“" + detailInstruName + "”(" + missingInstru.get(0) + ")");
        ImageView cancel = view.findViewById(R.id.inventory_lack_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog_lack_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchedInstru.add(missingInstru.get(0));
                changeAdapter(detailList, detailInstruName);
                inventoryAdapter.notifyDataSetChanged();
                missingInstru.remove(0);
                if (missingInstru.size() > 0) {
                    detail_instru.setText("“" + detailInstruName + "”(" + missingInstru.get(0) + ")");
                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public static List<String> listRemove(List<String> listA, List<String> listB) {
        HashSet hs1 = new HashSet(listA);
        HashSet hs2 = new HashSet(listB);
        hs1.removeAll(hs2);
        List<String> listC = new ArrayList<String>();
        listC.addAll(hs1);
        return listC;
    }

    public void updateList() {
        errorList.clear();
        for (ItemEntity itemEntity : allList) {
            if (itemEntity.getScrapped() != itemEntity.getStock()) {
                errorList.add(itemEntity);
            }
        }
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
        String excelFileName = "盘点清单 " + date + ".xls";
        myDao.insertFile("库存清单", excelFileName);
        String[] title = {"仪器名称", "存放位置", "库存数量", "应盘数量", "实盘数量"};

        List<ExportData> demoBeanList = new ArrayList<>();
        for (ItemEntity itemEntity : detailList) {
            String type = "盘点";
            String name = itemEntity.getValue();
            String location = "";
            List<String> mList = myDao.getCupName(name);
            for (String temp : mList) {
                location = location + "、" + temp;
            }
            if (location.length() > 0) location = location.substring(1);
            String remain = Integer.toString(itemEntity.getStock());
            String stock = Integer.toString(itemEntity.getSum());
            String used = Integer.toString(itemEntity.getScrapped());
            ExportData exportData = new ExportData(type, name, location, remain, stock, used);
            demoBeanList.add(exportData);
        }
        filePath = filePath + excelFileName;
        ExcelUtil.initExcel(filePath, date, title);
        ExcelUtil.writeObjListToExcel(demoBeanList, filePath, context);
    }
}