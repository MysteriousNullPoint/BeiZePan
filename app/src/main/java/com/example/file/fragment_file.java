package com.example.file;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.stock.TabAdapter;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;
import com.speedata.libuhf.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_file#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_file extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    List<String> mList = new ArrayList<>();
    List<String> detailList = new ArrayList<>();
    MyDao myDao;
    private GridView gridView;
    private TabAdapter adapter;
    private GridView detailGridView;
    private fileAdapter fileAdapter;

    private ImageView btnClearAll;
    private ImageView btnDelete;
    private ImageView btnExport;
    private String tabTag;

    private ImageView dialog_background;
    private TextView dialog_tv;
    private ImageView dialog_ok;
    private String admin_name = "";
    List<String> adminList = new ArrayList<>();

    private IUHFService iuhfService;
    private boolean isExport=false;

    private MainActivity mainActivity;
    private int flag=0;

    public fragment_file() {
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
        myDao = new MyDao(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file, container, false);
        gridView = view.findViewById(R.id.file_title_gridview);
        detailGridView = view.findViewById(R.id.file_detail_gridview);
        InitList();
        detailList = myDao.getFile();
        setTabAdapter();
        adminList = myDao.getAdminID();
        mainActivity=(MainActivity)getActivity();
        iuhfService = UHFManager.getUHFService(this.getContext());
        iuhfService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData spdInventoryData) {
                //TODO 盘点成功回调
                String label_id = spdInventoryData.epc;
                if (label_id.length() == Constants.EPC_LENGTH_ADMIN) {
                    for (String ID : adminList) {
                        if (label_id.equals(ID)) {
                            admin_name = myDao.getAdminName(ID);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = admin_name;
                            handler.sendMessage(msg);
                            iuhfService.inventoryStop();
                            dialog_ok.setSelected(true);
                        }
                    }
                }
            }

            @Override
            public void onInventoryStatus(int status) {

            }
        });

        detailGridView.post(new Runnable() {
            @Override
            public void run() {

                fileAdapter = new fileAdapter(fragment_file.this.getContext(), detailList);
                fileAdapter.setOnItemDeleteClickListener(new fileAdapter.onItemDeleteListener() {
                    @Override
                    public void onDeleteClick(int i) {
                        String name = detailList.get(i).substring(1);
                        if (detailList.get(i).startsWith("0")) {
                            detailList.set(i, "1" + name);
                            btnExport.setSelected(true);
                            btnDelete.setSelected(true);
                        }
                        else detailList.set(i, "0" + name);
                        fileAdapter.notifyDataSetChanged();
                        boolean isSelected=false;
                        for(int j=0;j<detailList.size();j++){
                            if(detailList.get(j).startsWith("1")){
                                isSelected=true;
                                break;
                            }
                        }
                        if(!isSelected){
                            btnExport.setSelected(false);
                            btnDelete.setSelected(false);
                        }
                    }
                });
                detailGridView.setAdapter(fileAdapter);
            }
        });

        btnClearAll = view.findViewById(R.id.file_btn_clearAll);
        if(detailList.size()>0) btnClearAll.setSelected(true);
        else btnClearAll.setSelected(false);
        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnClearAll.isSelected()) {
                    iuhfService.openDev();
                    iuhfService.inventoryStart();
                    showAdminDialog();
                    flag=0;
                }
            }
        });

        btnDelete = view.findViewById(R.id.file_btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnDelete.isSelected()) {
                    iuhfService.openDev();
                    iuhfService.inventoryStart();
                    showAdminDialog();
                    flag=1;
                }
                else Toast.makeText(fragment_file.this.getContext(),"请选择文件进行操作",Toast.LENGTH_SHORT).show();
            }
        });

        btnExport = view.findViewById(R.id.file_btn_export);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnExport.isSelected()) {
                    if(mainActivity.isReject) {
                        if (!isExport) {
                            Toast.makeText(fragment_file.this.getContext(), "开始导出，请稍后", Toast.LENGTH_LONG).show();
                            new exportThread().start();
                            isExport = true;
                        } else
                            Toast.makeText(fragment_file.this.getContext(), "导出中，请稍后", Toast.LENGTH_LONG).show();
                    }
                    else Toast.makeText(fragment_file.this.getContext(), "请插入U盘", Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(fragment_file.this.getContext(), "请选择文件进行操作", Toast.LENGTH_SHORT).show();
            }
        });
        return view;


    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void InitList() {
        mList.add("1所有文件");
        mList.add("0库存清单");
        mList.add("0报废清单");
        mList.add("0保养记录");
        mList.add("0借还台账");
        tabTag = "所有文件";
    }

    private void setTabAdapter() {
        adapter = new TabAdapter(this.getContext(), mList);
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
                tabTag = mList.get(i).substring(1);
                detailList.clear();
                List<String> newList = new ArrayList<>();
                if (i == 0) newList = myDao.getFile();
                else newList = myDao.getFile(tabTag);
                detailList.addAll(newList);
                adapter.notifyDataSetChanged();
                Log.i("test", i + "  " + tabTag);
                fileAdapter.notifyDataSetChanged();
                if(detailList.size()>0) btnClearAll.setSelected(true);
                else btnClearAll.setSelected(false);
                btnDelete.setSelected(false);
                btnExport.setSelected(false);
            }
        });
    }

    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }
            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
                iuhfService.inventoryStop();
                iuhfService.closeDev();
                dialog.dismiss();
            }
        });

        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iuhfService.inventoryStop();
                iuhfService.closeDev();
                if(flag==0){
                if (tabTag == "所有文件") myDao.removeAll();
                else myDao.removeAll(tabTag);
                for (String name : detailList) {
                    name = name.substring(1);
                    File file1 = new File("/storage/emulated/0/Rfid/" + name);
                    file1.delete();
                }
                detailList.clear();
                fileAdapter.notifyDataSetChanged();
                btnClearAll.setSelected(false);
                }
                else deleteFile();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 1:
                    dialog_background.setSelected(true);
                    dialog_tv.setText(admin_name);
                    dialog_ok.setVisibility(View.VISIBLE);
                    break;
                case 0:
                    Toast.makeText(fragment_file.this.getContext(),"导出成功",Toast.LENGTH_LONG).show();
            }
        }
    };

    class exportThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < detailList.size(); i++) {
                String name = detailList.get(i);
                if (name.startsWith("1")) {
                    name = name.substring(1);
                    String oldFilePath = "/storage/emulated/0/Rfid/" + name;
                    String newFilePath = "/mnt/media_rw/usbotg/" + name;
                    File usbFile = new File(newFilePath);
                    if (usbFile.isFile() && usbFile.exists()) {
                        usbFile.delete();
                    }
                    copyFile(oldFilePath, newFilePath);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg=new Message();
            msg.what=0;
            handler.sendMessage(msg);
        }
    }

    private void deleteFile()
    {
        List<Integer> removeTag = new ArrayList<>();
        for (int i = 0; i < detailList.size(); i++) {
            String name = detailList.get(i);
            if (name.startsWith("1")) {
                myDao.remove(name);
                name = name.substring(1);
                File file1 = new File("/storage/emulated/0/Rfid/" + name);
                file1.delete();
                removeTag.add(i);
            }
        }
        for (int a : removeTag) {
            detailList.remove(a);
        }
        for (int i = 0; i < detailList.size(); i++) {
            String name = detailList.get(i);

        }
        fileAdapter.notifyDataSetChanged();
        for (int i = 0; i < detailList.size(); i++) {
            String name = detailList.get(i);
            detailList.set(i, "0" + name.substring(1));
        }
        List<String> temp = new ArrayList<>(detailList);
        detailList.clear();
        detailList.addAll(temp);
        btnExport.setSelected(false);
        btnDelete.setSelected(false);
        if(detailList.size()==0) btnClearAll.setSelected(false);
        fileAdapter.notifyDataSetChanged();
    }
}

