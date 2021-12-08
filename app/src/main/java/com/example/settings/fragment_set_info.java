package com.example.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.stock.fragment_inventory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_info#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_info extends Fragment {

    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnConfirm;
    private ImageView btnSync;

    private EditText EditSchool;
    private EditText EditLab;
    private EditText EditDays;

    private MainActivity mainActivity;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_info() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_info.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_info newInstance(String param1, String param2) {
        fragment_set_info fragment = new fragment_set_info();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_info, container, false);
        btnInfo = view.findViewById(R.id.set_info_tab_info);
        btnId = view.findViewById(R.id.set_info_tab_id);
        btnStock = view.findViewById(R.id.set_info_tab_stock);
        btnTime = view.findViewById(R.id.set_info_tab_time);
        btnReset = view.findViewById(R.id.set_info_tab_reset);
        btnConfirm = view.findViewById(R.id.set_info_btn_confirm);
        btnSync=view.findViewById(R.id.settings_info_tab_sync);
        EditSchool = view.findViewById(R.id.settings_school);
        EditLab = view.findViewById(R.id.settings_lab);
        EditDays = view.findViewById(R.id.settings_days);
        mainActivity = (MainActivity) getActivity();
        btnInfo.setSelected(true);
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

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!EditLab.getText().toString().equals("") && !EditSchool.getText().toString().equals("") && !EditDays.getText().toString().equals("")) {
                    setInfo();
                    Toast.makeText(fragment_set_info.this.getContext(), "修改完成", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(fragment_set_info.this.getContext(), "请补全信息", Toast.LENGTH_SHORT).show();
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
        return view;
    }

    private void setInfo() {
        String school_name = EditSchool.getText().toString();
        String lab_name = EditLab.getText().toString();
        String day = EditDays.getText().toString();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("FirstRun", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("school", school_name);
        editor.putString("lab", lab_name);
        editor.putString("days", day);
        editor.apply();
        mainActivity.school_name.setText(school_name);
        mainActivity.lab_name.setText(lab_name);
    }
}