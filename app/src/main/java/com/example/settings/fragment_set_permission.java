package com.example.settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cupboard.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_set_permission#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_set_permission extends Fragment {

    private ImageView btnInfo;
    private ImageView btnId;
    private ImageView btnPermission;
    private ImageView btnStock;
    private ImageView btnTime;
    private ImageView btnReset;
    private ImageView btnSync;
    private ImageView tabUser;
    private ImageView tabGroup;
    private ImageView btnEdit;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_set_permission() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_set_permission.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_set_permission newInstance(String param1, String param2) {
        fragment_set_permission fragment = new fragment_set_permission();
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
        View view = inflater.inflate(R.layout.fragment_set_permission, container, false);
        btnInfo = view.findViewById(R.id.set_per_tab_info);
        btnId = view.findViewById(R.id.set_per_tab_id);
        btnPermission=view.findViewById(R.id.set_per_tab_permission);
        btnStock=view.findViewById(R.id.set_per_tab_stock);
        btnTime = view.findViewById(R.id.set_per_tab_time);
        btnReset = view.findViewById(R.id.set_per_tab_reset);
        btnSync=view.findViewById(R.id.settings_per_tab_sync);
        tabUser=view.findViewById(R.id.set_per_tab_user);
        tabGroup=view.findViewById(R.id.set_per_tab_group);
        btnEdit=view.findViewById(R.id.set_per_edit);

        tabUser.setSelected(true);
        btnPermission.setSelected(true);
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

        tabUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabUser.setSelected(true);
                tabGroup.setSelected(false);
            }
        });

        tabGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabUser.setSelected(false);
                tabGroup.setSelected(true);
            }
        });
        return view;
    }
}