package com.example.guide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.spd.mdm.manager.MdmManager;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class activity_guide_date extends AppCompatActivity {

    MdmManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager=MdmManager.getInstance();
        setContentView(R.layout.activity_guide_date);
        ImageView pre=(ImageView)findViewById(R.id.pre_step);
        ImageView next=(ImageView)findViewById(R.id.next_step_date);
        DatePicker datepicker=(DatePicker)findViewById(R.id.datePicker);
        TimePicker timepicker=(TimePicker)findViewById(R.id.timePicker) ;
        timepicker.setIs24HourView(true);
        setDatePickerDividerColor(datepicker);
        setTimePickerDividerColor(timepicker);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(activity_guide_date.this,activity_guide_info.class);
                startActivity(intent);
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("test","开始设置");
                int year=datepicker.getYear();
                int month=datepicker.getMonth()+1;
                int day=datepicker.getDayOfMonth();
                int hour=timepicker.getHour();
                int minute=timepicker.getMinute();
                String time= year +"-"+ month +"-"+ day +" "+ hour +":"+ minute +":"+"0";
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //设置要读取的时间字符串格式
                Date date = null;
                try {
                    date = format.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //转换为Date类
                Long timestamp=date.getTime();
                manager.setSysTime(timestamp);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent=new Intent(activity_guide_date.this, activity_guide_class.class);
                SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("First", false);
                editor.apply();
                startActivity(intent);
                finish();
            }
        });
    }

    private void setDatePickerDividerColor(DatePicker datePicker) {
        // Divider changing:

        // 获取 mSpinners
        LinearLayout llFirst = (LinearLayout) datePicker.getChildAt(0);

        // 获取 NumberPicker
        LinearLayout mSpinners = (LinearLayout) llFirst.getChildAt(0);
        for (int i = 0; i < mSpinners.getChildCount(); i++) {
            NumberPicker picker = (NumberPicker) mSpinners.getChildAt(i);

            Field[] pickerFields = NumberPicker.class.getDeclaredFields();
            for (Field pf : pickerFields) {
                if (pf.getName().equals("mSelectionDivider")) {
                    pf.setAccessible(true);
                    try {
                        pf.set(picker, new ColorDrawable(Color.parseColor("#ffffff")));//设置分割线颜色
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private void setTimePickerDividerColor(TimePicker timePicker) {
        Resources systemResources = Resources.getSystem();
        int hourNumberPickerId = systemResources.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = systemResources.getIdentifier("minute", "id", "android");
        NumberPicker hourNumberPicker = (NumberPicker) timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = (NumberPicker) timePicker.findViewById(minuteNumberPickerId);
        setNumberPickerDivider(hourNumberPicker);
        setNumberPickerDivider(minuteNumberPicker);

    }

    private void setNumberPickerDivider(NumberPicker numberPicker) {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            try{
                Field dividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(
                        ContextCompat.getColor(this, android.R.color.white));
                dividerField.set(numberPicker,colorDrawable);
                numberPicker.invalidate();
            }
            catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e){
                Log.w("setNumberPickerTxtClr", e);
            }
        }
    }
}