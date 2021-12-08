package com.example.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.spd.mdm.manager.MdmManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_sync#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_sync extends Fragment {

    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnSync;
    private ImageView btnConfirm;
    private MyDao myDao;
    private MainActivity mainActivity;
    private List<ScanResult> getWifiList = new ArrayList<>();
    WifiUtils wifiUtils;
    private ConstraintLayout constraintLayout;
    private ListView listView;
    private SyncAdapter syncAdapter;
    private String wifiSSID = "";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_sync() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_sync.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_sync newInstance(String param1, String param2) {
        fragment_set_sync fragment = new fragment_set_sync();
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
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_sync, container, false);
        btnInfo = view.findViewById(R.id.set_sync_tab_info);
        btnId = view.findViewById(R.id.set_sync_tab_id);
        //btnPermission = view.findViewById(R.id.set_sync_tab_permission);
        btnStock = view.findViewById(R.id.set_sync_tab_stock);
        btnTime = view.findViewById(R.id.set_sync_tab_time);
        btnReset = view.findViewById(R.id.set_sync_tab_reset);
        btnSync = view.findViewById(R.id.settings_sync_tab_sync);
        myDao = new MyDao(this.getContext());
        btnConfirm = view.findViewById(R.id.settings_sync_btn);
        btnSync.setSelected(true);
        wifiUtils = new WifiUtils(this.getContext());
        constraintLayout = view.findViewById(R.id.sync_background);
        listView = view.findViewById(R.id.settings_sync_listview);
        syncAdapter = new SyncAdapter(this.getContext(), getWifiList);
        listView.setAdapter(syncAdapter);
        wifiUtils.openWifi();
        syncAdapter.setOnItemDeleteClickListener(new SyncAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                wifiSSID = getWifiList.get(i).SSID;
                showWifiDialog();
            }
        });

        if (!mainActivity.aerial_state.isSelected())
            constraintLayout.setBackgroundResource(R.mipmap.dock_set_wifi_background_connected);
        else constraintLayout.setBackgroundResource(R.mipmap.dock_set_wifi_background);

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

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_set_reset(), null)
                        .addToBackStack(null)
                        .commit();
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


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintLayout.setBackgroundResource(R.mipmap.dock_set_wifi_background_choice);
                btnConfirm.setImageLevel(11);
                btnConfirm.setEnabled(false);
                listView.setVisibility(View.VISIBLE);
                wifiUtils.startScan();
                List<ScanResult> tempList = wifiUtils.getWifiList();
                getWifiList.clear();
                getWifiList.addAll(tempList);
                Log.i("wifi", "search");
                for (ScanResult result : getWifiList) {
                    Log.i("wifi", result.SSID);
                }
                syncAdapter.notifyDataSetChanged();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnConfirm.setImageLevel(21);
                        btnConfirm.setEnabled(true);
                    }
                }, 3000);//3秒后执行Runnable中的run方法
            }

        });

        return view;
    }

    private void showWifiDialog() {
        Context mContext = fragment_set_sync.this.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.wifi_config_dialog, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).setView(view).create();
        ImageView cancel = (ImageView) view.findViewById(R.id.wifi_dialog_cancel);
        ImageView confirm=view.findViewById(R.id.wifi_dialog_confirm);
        dialog.setCanceledOnTouchOutside(false);
        EditText editText=view.findViewById(R.id.wifi_password);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
       confirm.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String password=editText.getText().toString();
               wifiUtils.connectWifiPws(wifiSSID,password);
               Toast.makeText(mContext,"连接中,请稍后",Toast.LENGTH_SHORT).show();
               handler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       if(mainActivity.aerial_state.isSelected()) Toast.makeText(mContext,"连接失败",Toast.LENGTH_LONG).show();
                       else {constraintLayout.setBackgroundResource(R.mipmap.dock_set_wifi_background_connected);
                       listView.setVisibility(View.INVISIBLE);
                       }
                   }
               }, 6000);//3秒后执行Runnable中的run方法
               dialog.dismiss();
           }
       });
        dialog.show();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
    };

}