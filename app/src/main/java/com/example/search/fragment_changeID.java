package com.example.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_changeID#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_changeID extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyDao myDao;
    private Context mContext;
    private MainActivity mainActivity;
    private HashMap<String,String> map=new HashMap<>();
    String oldID;

    public fragment_changeID() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_changeID.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_changeID newInstance(String param1, String param2) {
        fragment_changeID fragment = new fragment_changeID();
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

    TextView textView;
    String name;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_i_d, container, false);
        textView=view.findViewById(R.id.changeID_Text);
        ImageView btn=view.findViewById(R.id.changeID_btn);
        mContext=this.getContext();
        myDao=new MyDao(mContext);
        map=myDao.getAllInstruMap();
        mainActivity = (MainActivity) getActivity();
        oldID=mainActivity.editText.getText().toString();
        if(map.get(oldID)!=null){
            name=map.get(oldID);
            textView.setText("“"+oldID+"”"+name);
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
        return view;
    }


    private void startScan(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(mainActivity);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setCaptureActivity(ActivityScanner.class); // 设置自定义的activity是QRActivity
        intentIntegrator.setRequestCode(1001);
        intentIntegrator.initiateScan();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 10:
                    String id=(String)msg.obj;
                    if(myDao.isInstrumentID(id)){
                        Toast.makeText(mContext,"所扫描标签已存在",Toast.LENGTH_LONG).show();
                    }
                    else {
                        myDao.changeID(oldID, id);
                        textView.setText("“" + id + "”" + name);
                        if (!mainActivity.aerial_state.isSelected()) {
                            List<String> sendList = new ArrayList<>();
                            sendList.add("xx000008");
                            sendList.add("xx000008");
                            sendList.add("xx000008");
                            sendList.add(oldID);
                            try {
                                mainActivity.sendData(sendList);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            sendList = new ArrayList<>();
                            sendList.add("xx000000");
                            sendList.add("xx000000");
                            sendList.add("xx000000");
                            sendList.add(id);
                            try {
                                mainActivity.sendData(sendList);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(mContext, "替换完成", Toast.LENGTH_SHORT).show();
                        } else {
                            List<String> sync=new ArrayList<>();
                            sync.add(id);
                            myDao.insertSync(1,sync);
                            sync=new ArrayList<>();
                            sync.add(oldID);
                            myDao.insertSync(2,sync);
                            Toast.makeText(mContext, "替换完成,门禁未同步，请同步门禁", Toast.LENGTH_SHORT).show();
                        }
                    }

            }
        }
    };

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity=(MainActivity) getActivity();
        mainActivity.setHandler(handler);
    }


}