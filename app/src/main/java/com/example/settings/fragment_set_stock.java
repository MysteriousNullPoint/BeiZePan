package com.example.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.dock.fragment_dock;
import com.example.dock.fragment_dockFake;
import com.example.file.ExcelUtil;
import com.example.file.ExportData;
import com.example.stock.ItemEntity;
import com.example.stock.fragment_stock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_stock#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_stock extends Fragment {

    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnSync;

    private ImageView importUSB;
    private ImageView exportUSB;
    ExcelManager excelManager;
    MainActivity mainActivity;
    private MyDao myDao;
    private Boolean isExport=false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_stock() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_stock.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_stock newInstance(String param1, String param2) {
        fragment_set_stock fragment = new fragment_set_stock();
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
        View view = inflater.inflate(R.layout.fragment_set_stock, container, false);
        btnInfo = view.findViewById(R.id.set_stock_tab_info);
        btnId = view.findViewById(R.id.set_stock_tab_id);
      //  btnPermission = view.findViewById(R.id.set_stock_tab_permission);
        btnStock = view.findViewById(R.id.set_stock_tab_stock);
        btnTime = view.findViewById(R.id.set_stock_tab_time);
        btnReset = view.findViewById(R.id.set_stock_tab_reset);
        btnSync = view.findViewById(R.id.settings_stock_tab_sync);
        exportUSB = view.findViewById(R.id.set_stock_btn_export);
        importUSB = view.findViewById(R.id.set_stock_btn_import);
        btnStock.setSelected(true);
        excelManager = new ExcelManager(this.getContext());
        mainActivity = (MainActivity) getActivity();
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

        importUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                excelManager.readExcel();
                mainActivity.updateData();
                if (excelManager.isImport()) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new fragment_stock(), null)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        exportUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.isReject) {
                    if (!isExport) {
                        Toast.makeText(fragment_set_stock.this.getContext(), "正在导出，请稍后", Toast.LENGTH_LONG).show();
                        isExport = true;
                        new exportThread().start();

                    } else
                        Toast.makeText(fragment_set_stock.this.getContext(), "导出中，请稍后", Toast.LENGTH_LONG).show();
                }
                else  Toast.makeText(fragment_set_stock.this.getContext(), "请插入U盘", Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }


    public void exportExcel() throws InterruptedException {
        Context context = fragment_set_stock.this.getContext();
        String filePath = "/mnt/media_rw/usbotg";

        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File("/mnt/media_rw/usbotg/库存模板.xls");
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        file = new File("/mnt/media_rw/usbotg/映射模板.xls");
        if (file.isFile() && file.exists()) {
            file.delete();
        }

        String excelFileName = "stock.xls";
        String[] title = {"物品", "分类", "库存总数", "新增总数"};
        List<ExportData> demoBeanList = myDao.exportStock();
        filePath = "/mnt/media_rw/usbotg/库存模板.xls";
        ExcelUtil.initExcel(filePath, excelFileName, title);
        ExcelUtil.writeObjListToExcel(demoBeanList, filePath, context);

        excelFileName = "instrument.xls";
        String[] newtitle = {"映射ID", "设备名称"};
        demoBeanList = new ArrayList<>();
        filePath = "/mnt/media_rw/usbotg/映射模板.xls";
        ExcelUtil.initExcel(filePath, excelFileName, newtitle);
        ExcelUtil.writeObjListToExcel(demoBeanList, filePath, context);

        Thread.sleep(5000);
        Message msg=new Message();
        msg.what=0;
        handler.sendMessage(msg);
        isExport=false;
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


    class exportThread extends Thread {
        @Override
        public void run() {
            try {
                exportExcel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 0:
                    Toast.makeText(fragment_set_stock.this.getContext(), "导出成功", Toast.LENGTH_SHORT).show();
            }
        }
    };

}