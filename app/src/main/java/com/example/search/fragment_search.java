package com.example.search;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.FindInstruAdapter;
import com.example.cupboard.MainActivity;
import com.example.cupboard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_search#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_search extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    MainActivity mainActivity;
    TextView tv_search;
    String searchName;
    ListView listView;
    GridView gridView;
    SearchInstruAdapter listViewAdapter;
    FindInstruAdapter gridViewAdapter;
    MyDao myDao;

    private List<HashMap<String,String>> mList_instru = new ArrayList<>();
    private List<String> find_detail = new ArrayList<>();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_search() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_search.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_search newInstance(String param1, String param2) {
        fragment_search fragment = new fragment_search();
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
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        myDao=new MyDao(this.getContext());
        tv_search = view.findViewById(R.id.search_tv_name);
        searchName = mainActivity.editText.getText().toString().toUpperCase();
        String s = "“" + searchName + "”";
        tv_search.setText(s);
        mList_instru=myDao.getSearchName(searchName);
        listView = view.findViewById(R.id.search_listview);
        gridView = view.findViewById(R.id.search_gridview);
        listViewAdapter = new SearchInstruAdapter(this.getContext(), mList_instru);
        listView.setAdapter(listViewAdapter);
        mainActivity.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mainActivity.frameFlag.equals("SEARCH")) {
                    if (!mainActivity.editText.getText().toString().equals("")) {
                        if (myDao.isInstrumentID(mainActivity.editText.getText().toString())) {
                            mainActivity.getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment, new fragment_changeID(), null)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        else {
                            mainActivity.getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment, new fragment_search(), null)
                                    .addToBackStack(null)
                                    .commit();
                            mainActivity.frameFlag = "SEARCH";
                        }
                    }
                }
                else {
                    if (myDao.isInstrumentID(mainActivity.editText.getText().toString())) {
                        mainActivity.getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment, new fragment_changeID(), null)
                                .addToBackStack(null)
                                .commit();
                    }
                    else {
                        searchName = mainActivity.editText.getText().toString().toUpperCase();
                        String s = "“" + searchName + "”";
                        tv_search.setText(s);
                        mList_instru.clear();
                        List<HashMap<String, String>> newList = myDao.getSearchName(searchName);
                        for (HashMap<String, String> map : newList) {
                            mList_instru.add(map);
                        }
                        listViewAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        gridViewAdapter = new FindInstruAdapter(this.getContext(), find_detail);
        gridView.setAdapter(gridViewAdapter);

        listViewAdapter.setOnSearchAddClickListener(new SearchInstruAdapter.onSearchAddListener() {
            @Override
            public void onAddClick(int i) {
                Boolean notIn=true;
                for(String name:find_detail){
                    if(name.equals(mList_instru.get(i).get("name"))){
                        notIn=false;
                        break;
                    }
                }
                if(notIn) {
                    find_detail.add(mList_instru.get(i).get("name"));
                    gridViewAdapter.notifyDataSetChanged();
                }
            }
        });

        gridViewAdapter.setOnItemDeleteClickListener(new FindInstruAdapter.onSearchDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                find_detail.remove(i);
                gridViewAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

}