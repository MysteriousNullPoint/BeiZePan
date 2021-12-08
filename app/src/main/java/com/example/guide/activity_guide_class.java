package com.example.guide;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Admin;
import com.example.cupboard.Constants;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import java.util.ArrayList;
import java.util.List;

public class activity_guide_class extends AppCompatActivity {

    /************************
     * GridView定义表格
     * mlist标签对列
     ************************/
    private GridView gridView;
    Admin admin;
    private List<String> mList=new ArrayList<>();
    private MyAdapter adapter ;

    ImageView background;
    TextView tv_cardNO;
    EditText ed_adminName;
    ImageView IV_confirm;
    IUHFService iuhfService;
    String cardID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        admin = new Admin(this);
        mList=admin.getAdminName();
        adapter = new MyAdapter(activity_guide_class.this, mList);
        Log.i("test", "进入设置标签");
        setContentView(R.layout.activity_guide_class);
        ImageView pre = (ImageView) findViewById(R.id.pre_step_class);
        ImageView skip = (ImageView) findViewById(R.id.skip);
        ImageView finish = (ImageView) findViewById(R.id.finish);
        ImageView add_class = (ImageView) findViewById(R.id.guide_addclass);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        iuhfService = UHFManager.getUHFService(this);
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

        /*********************
         * 上一页按钮
         ********************/
        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_guide_class.this, activity_guide_date.class);
                startActivity(intent);
                finish();
            }
        });

        /************
         * 跳过按钮
         ************/
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("First", false);
                editor.apply();
                finish();
            }
        });

        /********
         * 设置完成按钮
         ********/
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("First", false);
                editor.apply();
                finish();
            }
        });

        /********
         * 配置表格
         **********/
        mList=admin.getAdminName();

        this.gridView = (GridView) findViewById(R.id.gridview);

        gridView.setAdapter(adapter);
        //ListView item 中的删除按钮的点击事件
        adapter.setOnItemDeleteClickListener(new MyAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                if (i >= 1) {
                    mList.remove(i);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        add_class.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminDialog();
                iuhfService.openDev();
                iuhfService.inventoryStart();

            }
        });
    }


    /***********
     * 新增标签弹出框
     *********/
    private void showAdminDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_admin_dialog, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
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
                admin.insertAdmin(cardID, name);
                iuhfService.inventoryStop();
                iuhfService.closeDev();
                mList.add(name);
                adapter.notifyDataSetChanged();
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
                case 0:
                    tv_cardNO.setVisibility(View.VISIBLE);
                    tv_cardNO.setText(Integer.toString(mList.size() + 1));
                    ed_adminName.setVisibility(View.VISIBLE);
                    IV_confirm.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

}
