package com.example.settings;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Admin;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.guide.MyAdapter;
import com.example.input.fragment_input;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_id#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_id extends Fragment {
    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnSync;

    private GridView mGridView;
    private MyAdapter myAdapter;
    private List<String> mList = new ArrayList<>();
    List<String> removeList = new ArrayList<>();
    List<String> addnameList = new ArrayList<>();
    List<String> addidList = new ArrayList<>();
    private Admin admin;
    private ImageView btnAdd;
    private IUHFService iuhfService;
    private String cardID;
    private TextView tv_cardNO;
    private EditText ed_adminName;
    private ImageView IV_confirm;
    private ImageView background;

    private MyDao myDao;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_id() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_id.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_id newInstance(String param1, String param2) {
        fragment_set_id fragment = new fragment_set_id();
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
        admin = new Admin(this.getContext());
        iuhfService = UHFManager.getUHFService(this.getContext());
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                String label_id = spdInventoryData.epc;
                if (!background.isSelected()) {
                    if (!admin.CheckSameCard(label_id) && label_id.length() == Constants.EPC_LENGTH_ADMIN) {
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

        View view = inflater.inflate(R.layout.fragment_set_id, container, false);
        btnInfo = view.findViewById(R.id.set_id_tab_info);
        btnId = view.findViewById(R.id.set_id_tab_id);
        btnStock = view.findViewById(R.id.set_id_tab_stock);
        btnTime = view.findViewById(R.id.set_id_tab_time);
        btnReset = view.findViewById(R.id.set_id_tab_reset);
        btnSync = view.findViewById(R.id.settings_id_tab_sync);
        ImageView btnConfirm = view.findViewById(R.id.set_id_btn_confirm);
        MainActivity mainActivity = (MainActivity) getActivity();
        myDao = new MyDao(this.getContext());
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin.DeleteAdmin(removeList);
                for (int i = 0; i < addidList.size(); i++) {
                    admin.insertAdmin(addidList.get(i), addnameList.get(i));
                }
                if (!mainActivity.aerial_state.isSelected()) {
                    List<String> sendList = new ArrayList<>();
                    if (addidList.size() > 0) {
                        sendList.add("xx000001");
                        sendList.add("xx000001");
                        sendList.add("xx000001");
                        sendList.addAll(addidList);
                        try {
                            mainActivity.sendData(sendList);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (removeList.size() > 0) {
                        sendList = new ArrayList<>();
                        sendList.add("xx000009");
                        sendList.add("xx000009");
                        sendList.add("xx000009");
                        sendList.addAll(removeList);
                        try {
                            mainActivity.sendData(sendList);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(fragment_set_id.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
                } else {
                    if (removeList.size() > 0) myDao.insertSync(4, removeList);
                    if (addidList.size() > 0) myDao.insertSync(3, addidList);
                    if (removeList.size() > 0 || addidList.size() > 0)
                        Toast.makeText(fragment_set_id.this.getContext(), "修改完成，门禁未连接", Toast.LENGTH_LONG).show();
                }
                removeList.clear();
                addidList.clear();
                addnameList.clear();
            }
        });

        btnId.setSelected(true);
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

        btnAdd = view.findViewById(R.id.set_id_addbutton);
        mGridView = view.findViewById(R.id.set_id_gridview);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminDialog();
                iuhfService.openDev();
                iuhfService.inventoryStart();
            }
        });

        mList = admin.getAdminName();
        Log.i("test", "" + mList.size());
        myAdapter = new MyAdapter(fragment_set_id.this.getContext(), mList);
        mGridView.setAdapter(myAdapter);
        myAdapter.setOnItemDeleteClickListener(new MyAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                if (mList.size() > 1) {
                    removeList.add(mList.get(i));
                    for (int j = 0; j < addnameList.size(); j++) {
                        if (addnameList.get(j).equals(mList.get(i))) {
                            addnameList.remove(j);
                            addidList.remove(j);
                        }
                    }
                    admin.removeTemp(i);
                    mList.remove(i);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
        return view;
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
                if (!name.equals("")) {
                    boolean isNew = true;
                    for (String oldName : mList) {
                        if (oldName.equals(name)) {
                            isNew = false;
                            Toast.makeText(fragment_set_id.this.getContext(), "已存在相同名字", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (isNew) {
                        addnameList.add(name);
                        addidList.add(cardID);
                        for (int i = 0; i < removeList.size(); i++) {
                            if (removeList.get(i).equals(name)) removeList.remove(i);
                        }
                        iuhfService.inventoryStop();
                        iuhfService.closeDev();
                        mList.add(name);
                        admin.addTemp(cardID);
                        myAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                } else
                    Toast.makeText(fragment_set_id.this.getContext(), "请填写管理员姓名", Toast.LENGTH_SHORT).show();
            }

        });
        dialog.show();
    }


}