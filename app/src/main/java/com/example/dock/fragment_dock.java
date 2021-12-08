package com.example.dock;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.home.fragment_home;
import com.example.input.fragment_input;
import com.example.settings.fragment_set_info;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_dock#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_dock extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    HashMap<String, String> TAG_MAP = new HashMap<>();
    GridView gridView;
    dockAdapter adapter;
    private List<DockDetail> detailList = new ArrayList<>();
    private ImageView btnAddItem;
    private ImageView btnConfirm;
    private TextView tvTitle;

    private ImageView dialog_background;
    private ImageView dialog_confim;
    private List<HashMap<String, String>> addList = new ArrayList<>();
    private List<String> mList = new ArrayList<>();
    private ListView addListView;
    private AddAdapter addAdapter;

    private List<HashMap<String, String>> itemList = new ArrayList<>();
    private List<String> removeList = new ArrayList<>();

    String adminName;
    String admin;
    private timeCount timeThread;
    boolean run=true;


    private MainActivity mainActivity;
    MyDao myDao;
    String behave = "";

    public fragment_dock() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_dock.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_dock newInstance(String param1, String param2) {
        fragment_dock fragment = new fragment_dock();
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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setHandler(handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dock, container, false);
        myDao = new MyDao(this.getContext());
        TAG_MAP = myDao.getStockInstruMap();
        tvTitle = view.findViewById(R.id.dock_tv_use);
        gridView = view.findViewById(R.id.dock_gridview);
        mList.addAll(mainActivity.dataList);
        for (String i : mList) {
            Log.i("test", "add" + i);
        }
        if (mList.get(0).equals("yy000002")) behave = "归还";
        else behave = "借用";
        tvTitle.setText("本次共" + behave + "仪器" + (mList.size() - 2) + "件");
        mList.remove(0);
        admin = mList.get(0).toUpperCase();
        adminName = myDao.getAdminName(admin);
        mList.remove(0);
        setDetailList(mList);
        btnAddItem = view.findViewById(R.id.dock_add_item);
        btnConfirm = view.findViewById(R.id.dock_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBtnConfirm();
            }
        });
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> sendMessage = new ArrayList<>();
                sendMessage.add("xx000003");
                sendMessage.add("xx000003");
                sendMessage.add("xx000003");
                try {
                    mainActivity.sendData(sendMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showDialog();
                mainActivity.textToSpeech.speak(Constants.TTS_CAMERA, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        adapter = new dockAdapter(this.getContext(), detailList);
        adapter.setOnItemDeleteClickListener(new dockAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                itemList.clear();
                removeList.clear();
                for (String id : mList) {
                    if (TAG_MAP.get(id).equals(detailList.get(i).getInstruName())) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("name", detailList.get(i).getInstruName());
                        map.put("id", id);
                        itemList.add(map);
                    }
                }
                showDetailDialog();
            }
        });
        gridView.setAdapter(adapter);


        addAdapter = new AddAdapter(this.getContext(), addList);
        addAdapter.setOnItemDeleteClickListener(new AddAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                addList.remove(i);
                addAdapter.notifyDataSetChanged();
            }
        });
        timeThread = new timeCount();
        timeThread.start();
        return view;
    }

    void setDetailList(List<String> mList) {
        detailList.clear();
        for (String id : mList) {
            String name = TAG_MAP.get(id);
            String cupName = myDao.getCupNameById(id);
            boolean isNew = true;
            for (int i = 0; i < detailList.size(); i++) {
                if (detailList.get(i).getInstruName().equals(name)) {
                    isNew = false;
                    detailList.get(i).addSum();
                    detailList.get(i).addCupName(cupName);
                }
            }
            if (isNew) {
                DockDetail dockDetail = new DockDetail();
                dockDetail.setInstruName(name);
                dockDetail.addCupName(cupName);
                dockDetail.setSum();
                detailList.add(dockDetail);
            }
        }
    }

    private void showDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_dock, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        ImageView cancel = view.findViewById(R.id.dock_dialog_cancel);
        dialog_background = view.findViewById(R.id.dock_dialog_background);
        dialog_confim = view.findViewById(R.id.dock_dialog_confirm);
        addListView = view.findViewById(R.id.dock_add_listview);
        addListView.setAdapter(addAdapter);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> sendMessage = new ArrayList<>();
                sendMessage.add("xx000006");
                sendMessage.add("xx000006");
                sendMessage.add("xx000006");
                try {
                    mainActivity.sendData(sendMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog_confim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addList.size() > 0) {
                    List<String> sendMessage = new ArrayList<>();
                    sendMessage.add("xx000006");
                    sendMessage.add("xx000006");
                    sendMessage.add("xx000006");
                    try {
                        mainActivity.sendData(sendMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (HashMap<String, String> map : addList) {
                        mList.add(map.get("id"));
                    }
                    setDetailList(mList);
                    adapter.notifyDataSetChanged();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
        WindowManager.LayoutParams params =
                dialog.getWindow().getAttributes();
        params.width = 882;
        params.height = 560;
        dialog.getWindow().setAttributes(params);
    }


    private void showDetailDialog() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_dock_detail, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this.getContext()).setView(view).create();
        dialog.setCanceledOnTouchOutside(false);
        ImageView add = view.findViewById(R.id.dock_detail_add);
        ImageView confirm = view.findViewById(R.id.dock_detail_confirm);
        ListView listView = view.findViewById(R.id.dock_detail_listview);
        AddAdapter newAdapter = new AddAdapter(this.getContext(), itemList);
        listView.setAdapter(newAdapter);
        newAdapter.setOnItemDeleteClickListener(new AddAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                removeList.add(itemList.get(i).get("id"));
                itemList.remove(i);
                newAdapter.notifyDataSetChanged();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDialog();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (removeList.size() > 0) {
                    mList = listRemove(mList, removeList);
                    setDetailList(mList);
                    adapter.notifyDataSetChanged();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 2:
                    String message = (String) msg.obj;
                    dialog_background.setSelected(true);
                    addListView.setVisibility(View.VISIBLE);
                    dialog_confim.setVisibility(View.VISIBLE);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("id", message);
                    map.put("name", TAG_MAP.get(message));
                    addList.add(map);
                    addAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    tvTitle.setText("本次共" + behave + "仪器" + mList.size() + "件");
                    break;
                case 370:
                    run=false;
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new fragment_home(), null)
                            .addToBackStack(null)
                            .commit();
                    mainActivity.frameFlag = "home";
            }
        }
    };

    public static ArrayList<String> listRemove(List<String> listA, List<String> listB) {
        HashSet hs1 = new HashSet(listA);
        HashSet hs2 = new HashSet(listB);
        hs1.removeAll(hs2);
        ArrayList<String> listC = new ArrayList<>();
        listC.addAll(hs1);
        return listC;
    }

    private void setBtnConfirm() {
        List<String> sendMessage = new ArrayList<>();
        sendMessage.add("xx000002");
        sendMessage.add("xx000002");
        sendMessage.add("xx000002");
        sendMessage.addAll(mList);
        try {
            mainActivity.sendData(sendMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mainActivity.dataList.clear();
        long timeSeconds = System.currentTimeMillis();
        if (behave.equals("借用")) {
            myDao.updateCupboard(0, mList);
            myDao.updateLog(0, timeSeconds, mList);
        } else {
            myDao.updateCupboard(1, mList);
            myDao.updateLog(1, timeSeconds, mList);
        }
        for (DockDetail dockDetail : detailList) {
            myDao.insertBorrowSheet(timeSeconds, behave, dockDetail.getInstruName(), dockDetail.getSum(), adminName);
        }
        mainActivity.textToSpeech.speak(Constants.TTS_FINISH, TextToSpeech.QUEUE_FLUSH, null);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, new fragment_home(), null)
                .addToBackStack(null)
                .commit();
        mainActivity.frameFlag = "home";
    }

    private class timeCount extends Thread {
        public void run() {
            int i = 200;
            while (run) {
                try {
                    Message msg = new Message();
                    msg.what = i;
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
}