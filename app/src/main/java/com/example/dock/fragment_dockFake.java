package com.example.dock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.cupboard.MainActivity;
import com.example.cupboard.R;
import com.example.cupboard.Socket;
import com.example.home.fragment_home;
import com.example.stock.fragment_stock;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_dockFake#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_dockFake extends Fragment {

    ImageView background;

    private MainActivity mainActivity;
    AlertDialog dialog;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_dockFake() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_dockFake.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_dockFake newInstance(String param1, String param2) {
        fragment_dockFake fragment = new fragment_dockFake();
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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setHandler(handler);
    }

    public void onDestroy() {
        if (dialog != null) dialog.dismiss();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dock_fake, container, false);
        background = view.findViewById(R.id.dock_fake_background);
        background.setSelected(true);
        return view;
    }

    private void showCustomizeDialog() {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_dock_check, null, false);
        dialog = new AlertDialog.Builder(mainActivity).setView(view).create();
        ImageView cancel = (ImageView) view.findViewById(R.id.dialog_dock_cancel);
        ImageView check = (ImageView) view.findViewById(R.id.dialog_dock_check);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> sendMessage = new ArrayList<>();
                sendMessage.add("xx000004");
                sendMessage.add("xx000004");
                sendMessage.add("xx000004");
                try {
                    mainActivity.sendData(sendMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new fragment_home(), null)
                        .addToBackStack(null)
                        .commit();

            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                List<String> sendMessage = new ArrayList<>();
                sendMessage.add("xx000007");
                sendMessage.add("xx000007");
                sendMessage.add("xx000007");
                sendMessage.add("A000000000000001");
                try {
                    mainActivity.sendData(sendMessage);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 3:
                    showCustomizeDialog();
                    break;
            }
        }
    };
}