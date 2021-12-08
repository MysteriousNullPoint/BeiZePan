package com.example.settings;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.cupboard.R;
import com.spd.mdm.manager.MdmManager;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_time#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_time extends Fragment {

    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnSync;

    DatePicker datePicker;
    TimePicker timePicker;
    private ImageView btnConfirm;
    MdmManager manager;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_time() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_time.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_time newInstance(String param1, String param2) {
        fragment_set_time fragment = new fragment_set_time();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_time, container, false);
        btnInfo = view.findViewById(R.id.set_time_tab_info);
        btnId = view.findViewById(R.id.set_time_tab_id);
        //btnPermission=view.findViewById(R.id.set_time_tab_permission);
        btnStock=view.findViewById(R.id.set_time_tab_stock);
        btnTime = view.findViewById(R.id.set_time_tab_time);
        btnReset = view.findViewById(R.id.set_time_tab_reset);
        btnConfirm = view.findViewById(R.id.set_time_btn_confirm);
        btnSync=view.findViewById(R.id.settings_time_tab_sync);

        manager = MdmManager.getInstance();

        btnTime.setSelected(true);
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

        datePicker = (DatePicker) view.findViewById(R.id.set_time_datePicker);
        timePicker = (TimePicker) view.findViewById(R.id.set_time_timePicker);
        timePicker.setIs24HourView(true);
        setDatePickerDividerColor(datePicker);
        setTimePickerDividerColor(timePicker);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth() + 1;
                int day = datePicker.getDayOfMonth();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String time = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day) + " " + Integer.toString(hour) + ":" + Integer.toString(minute) + ":" + "0";
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //设置要读取的时间字符串格式
                Date date = null;
                try {
                    date = format.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //转换为Date类
                Long timestamp = date.getTime();
                manager.setSysTime(timestamp);
                Toast.makeText(fragment_set_time.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
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
                        pf.set(picker, new ColorDrawable(Color.parseColor("#000000")));//设置分割线颜色
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
        for (int i = 0; i < count; i++) {
            try {
                Field dividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(
                        ContextCompat.getColor(this.getContext(), android.R.color.black));
                dividerField.set(numberPicker, colorDrawable);
                numberPicker.invalidate();
            } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                Log.w("setNumberPickerTxtClr", e);
            }
        }
    }
}