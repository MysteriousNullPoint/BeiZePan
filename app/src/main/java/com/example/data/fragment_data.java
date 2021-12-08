package com.example.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.example.AAChartCoreLib.AAChartCreator.AAChartView;
import com.example.AAChartCoreLib.AAChartCreator.AASeriesElement;
import com.example.AAChartCoreLib.AAChartEnum.AAChartType;
import com.example.AAChartCoreLib.AAOptionsModel.AADataLabels;
import com.example.AAChartCoreLib.AAOptionsModel.AAPie;
import com.example.DataBaseHelper.Dao;
import com.example.DataBaseHelper.MyDao;
import com.example.cupboard.Constants;
import com.example.cupboard.R;
import com.example.file.ExcelUtil;
import com.example.file.ExportData;
import com.example.stock.ItemEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_data#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_data extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ConstraintLayout constraintLayout;
    private ImageView btnStatistic;
    private ImageView btnScrap;
    private ImageView btnMaintain;
    private ImageView btnBill;
    private ImageView btnExport;
    private Spinner spinner;

    String excelFileName;
    String tabTag = "";

    private TextView tvTab;
    private ListView listView;
    private DataScrapAdapter dataScrapAdapter;
    private DataLogAdapter dataLogAdapter;
    private DataMaintainAdapter maintainAdapter;
    private MyDao myDao;
    private List<HashMap<String, String>> scrapList = new ArrayList<>();
    private List<HashMap<String, String>> logList = new ArrayList<>();
    private List<String> instruList = new ArrayList<>();
    private List<String> classList = new ArrayList<>();

    private AAChartView aaChartView1;
    private AAChartView aaChartView2;
    private AAChartView aaChartView3;


    public fragment_data() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_data.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_data newInstance(String param1, String param2) {
        fragment_data fragment = new fragment_data();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        View view = inflater.inflate(R.layout.fragment_data, container, false);
        myDao = new MyDao(this.getContext());
        constraintLayout = view.findViewById(R.id.data_background);
        btnStatistic = view.findViewById(R.id.data_btn_statistic);
        btnScrap = view.findViewById(R.id.data_btn_scrap);
        btnMaintain = view.findViewById(R.id.data_btn_maintain);
        btnBill = view.findViewById(R.id.data_btn_bill);
        btnExport = view.findViewById(R.id.data_export);
        tvTab = view.findViewById(R.id.data_tv_tab);
        listView = view.findViewById(R.id.data_listView);
        spinner = view.findViewById(R.id.data_spinner);
        aaChartView1 = view.findViewById(R.id.barchart);
        aaChartView2 = view.findViewById(R.id.barchart_2);
        aaChartView3 = view.findViewById(R.id.barchart_3);

        scrapList = myDao.getScrap();
        logList = myDao.getSheet();
        instruList = myDao.getInstruName();
        classList = myDao.getClassName();
        initSpinner();
        dataScrapAdapter = new DataScrapAdapter(this.getContext(), scrapList);
        dataLogAdapter = new DataLogAdapter(this.getContext(), logList);
        maintainAdapter = new DataMaintainAdapter(this.getContext(), instruList);
        maintainAdapter.setOnItemDeleteClickListener(new DataMaintainAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(int i) {
                myDao.updateMaintain(instruList.get(i));
                List<String> tempList = new ArrayList<>(instruList);
                instruList.clear();
                instruList.addAll(tempList);
                maintainAdapter.notifyDataSetChanged();
            }
        });
        btnStatistic.setSelected(true);
        btnStatistic.setOnClickListener(l);
        btnScrap.setOnClickListener(l);
        btnMaintain.setOnClickListener(l);
        btnBill.setOnClickListener(l);
        initChartView();

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeSeconds = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String Date = sdf.format(timeSeconds);
                excelFileName = tabTag  + Date + ".xls";
                exportExcel(fragment_data.this.getContext());
                Toast.makeText(fragment_data.this.getContext(), "导出成功", Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnStatistic.setSelected(false);
            btnScrap.setSelected(false);
            btnMaintain.setSelected(false);
            btnBill.setSelected(false);
            btnExport.setVisibility(View.VISIBLE);
            v.setSelected(true);
            switch (v.getId()) {
                case R.id.data_btn_statistic:
                    constraintLayout.setBackgroundResource(R.mipmap.data_page_backgroud);
                    tvTab.setVisibility(View.INVISIBLE);
                    spinner.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.INVISIBLE);
                    btnExport.setVisibility(View.INVISIBLE);
                    spinner.setVisibility(View.VISIBLE);
                    aaChartView1.setVisibility(View.VISIBLE);
                    aaChartView2.setVisibility(View.VISIBLE);
                    aaChartView3.setVisibility(View.VISIBLE);
                    break;
                case R.id.data_btn_scrap:
                    constraintLayout.setBackgroundResource(R.mipmap.data_page_backgroud_b);
                    tvTab.setVisibility(View.VISIBLE);
                    tvTab.setText(Constants.DATA_TITLE_SCRAP);
                    listView.setAdapter(dataScrapAdapter);
                    listView.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.VISIBLE);
                    aaChartView1.setVisibility(View.INVISIBLE);
                    aaChartView2.setVisibility(View.INVISIBLE);
                    aaChartView3.setVisibility(View.INVISIBLE);
                    spinner.setSelection(0);
                    tabTag = "报废清单";
                    break;
                case R.id.data_btn_maintain:
                    constraintLayout.setBackgroundResource(R.mipmap.data_page_backgroud_b);
                    tvTab.setVisibility(View.VISIBLE);
                    tvTab.setText(Constants.DATA_TITLE_MAINTAIN);
                    listView.setVisibility(View.VISIBLE);
                    listView.setAdapter(maintainAdapter);
                    spinner.setVisibility(View.VISIBLE);
                    aaChartView1.setVisibility(View.INVISIBLE);
                    aaChartView2.setVisibility(View.INVISIBLE);
                    aaChartView3.setVisibility(View.INVISIBLE);
                    btnExport.setVisibility(View.INVISIBLE);
                    spinner.setSelection(0);
                    tabTag = "保养记录";
                    break;
                case R.id.data_btn_bill:
                    constraintLayout.setBackgroundResource(R.mipmap.data_page_backgroud_b);
                    tvTab.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.INVISIBLE);
                    tvTab.setText(Constants.DATA_TITLE_BILL);
                    listView.setVisibility(View.VISIBLE);
                    aaChartView1.setVisibility(View.INVISIBLE);
                    aaChartView2.setVisibility(View.INVISIBLE);
                    aaChartView3.setVisibility(View.INVISIBLE);
                    listView.setAdapter(dataLogAdapter);
                    tabTag = "借还台账";
                    break;
            }
        }
    };

    private void initSpinner() {
        //声明一个下拉列表的数组适配器
        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(fragment_data.this.getContext(), R.layout.spinner_item_select, classList.toArray(new String[classList.size()]));
        //设置数组适配器的布局样式
        starAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        //设置下拉框的数组适配器
        spinner.setAdapter(starAdapter);
        //设置下拉框默认的显示第一项
        spinner.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        spinner.setOnItemSelectedListener(new MySelectedListener());
    }

    class MySelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            switch (tabTag) {
                case "报废清单":
                    if (i == 0) {
                        scrapList.clear();
                        List<HashMap<String, String>> tempList = myDao.getScrap();
                        for (HashMap<String, String> map : tempList) {
                            scrapList.add(map);
                        }
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    } else {
                        scrapList.clear();
                        List<HashMap<String, String>> tempList = myDao.getScrap(classList.get(i));
                        Log.i("test", tempList.size() + "");
                        for (HashMap<String, String> map : tempList) {
                            scrapList.add(map);
                        }
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                    break;
                case "保养清单":
                    if (i == 0) {
                        instruList.clear();
                        List<String> tempList = myDao.getInstruName();
                        /*
                        for(HashMap<String,String> map:tempList){
                            scrapList.add(map);
                        }
                        Log.i("test",""+scrapList.size());
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                    else{
                        scrapList.clear();
                        List<HashMap<String,String>> tempList=myDao.getScrap(classList.get(i));
                        Log.i("test",tempList.size()+"");
                        for(HashMap<String,String> map:tempList){
                            scrapList.add(map);
                        }
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                        Log.i("test",""+scrapList.size());*/
                    }


                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //msg就是子线程发送过来的消息。
            switch (msg.what) {
                case 0:
                    dataScrapAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    break;
            }
        }
    };

    private void exportExcel(Context context) {
        String filePath = "/storage/emulated/0/Rfid/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        myDao.insertFile(tabTag, excelFileName);
        if(tabTag.equals("报废清单")) {
            String[] title = {"仪器名称", "累计报废"};

            List<ExportData> demoBeanList = new ArrayList<>();
            for (HashMap<String, String> map : scrapList) {
                String type = "报废";
                String name = map.get("name");
                String location = map.get("scrap");
                Log.i("datachange",name+"  "+location);
                ExportData exportData = new ExportData(type, name, location);
                demoBeanList.add(exportData);
            }
            filePath = "/storage/emulated/0/Rfid/" + excelFileName;
            ExcelUtil.initExcel(filePath, excelFileName, title);
            ExcelUtil.writeObjListToExcel(demoBeanList, filePath, context);
        }
        else{
            String[] title = {"日期", "操作","仪器名称", "数量","余量","管理员"};
            List<ExportData> demoBeanList = new ArrayList<>();
            for (HashMap<String, String> map : logList) {
                String type = "库存";
                String date=map.get("datelog");
                String behave=map.get("behave");
                String name=map.get("name");
                String number=map.get("number");
                String stock=map.get("stock");
                String admin;
                if(map.get("admin")!=null) admin=map.get("admin");
                else admin="";
                ExportData exportData = new ExportData(type, date,behave,name,number,stock,admin);
                demoBeanList.add(exportData);
            }
            filePath = "/storage/emulated/0/Rfid/" + excelFileName;
            File usbFile = new File(filePath);
            if (usbFile.isFile() && usbFile.exists()) {
                usbFile.delete();
            }
            ExcelUtil.initExcel(filePath, excelFileName, title);
            ExcelUtil.writeObjListToExcel(demoBeanList, filePath, context);
        }


    }

    private void initChartView() {
        List<String> mList = myDao.getClassName();
        mList.remove(0);
        String[] strs = mList.toArray(new String[mList.size()]);
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Column)
                .title("总计")
                //.subtitle("Virtual Data")
                .categories(strs)
                .backgroundColor(Color.WHITE)
                .dataLabelsEnabled(false)
                .yAxisGridLineWidth(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("总数")
                                .data(myDao.getChartData1(0, mList).toArray(new Integer[0])),
                        new AASeriesElement()
                                .name("在库")
                                .data(myDao.getChartData1(1, mList).toArray(new Integer[0])),
                        new AASeriesElement()
                                .name("报废")
                                .data(myDao.getChartData1(2, mList).toArray(new Integer[0])),
                });
        List<Map<String, Object>> pieList = myDao.getChartData2();
        int a = pieList.size();
        Object obj[][] = new Object[a][2];
        for (int i = 0; i < a; i++) {
            obj[i][0] = (String) pieList.get(i).get("name");
            obj[i][1] = (Integer) pieList.get(i).get("count");
        }
        float b;
        if(a<=8) b=120;
        else if(a<=12) b=110;
        else b=80;
        AAChartModel aaChartModel2 = new AAChartModel()
                .chartType(AAChartType.Pie)
                .backgroundColor("#ffffff")
                .title("使用频率")
                .series(new AAPie[]{
                                new AAPie()
                                        .name("库存")
                                        .size(b)
                                        .dataLabels(new AADataLabels()
                                                .format(""))
                                        .data(obj)
                        }
                );

        strs=new String[]{"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};
        int size=mList.size();
        AASeriesElement seriesElement[]=new AASeriesElement[size];
        for(int i=0;i<size;i++){
            String name=mList.get(i);
            seriesElement[i]=new AASeriesElement().name(name).data(myDao.getChartData3(name).toArray(new Integer[0]));
        }
        AAChartModel aaChartModel3 = new AAChartModel()
                .chartType(AAChartType.Line)
                .title("今年使用次数")
                .backgroundColor(Color.WHITE)
                .categories(strs)
                .dataLabelsEnabled(false)
                .yAxisGridLineWidth(0f)
                .series(seriesElement);

        aaChartView1.aa_drawChartWithChartModel(aaChartModel);
        aaChartView2.aa_drawChartWithChartModel(aaChartModel2);
        aaChartView3.aa_drawChartWithChartModel(aaChartModel3);
    }
}