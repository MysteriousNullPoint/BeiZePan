package com.example.cupboard;

public class Constants {
    public static final String DATABASE_NAME="cupboard.db";
    public static final int VERSION_CODE=1;
    public static final String CLASS_NAME="class_name";
    public static final int INIT_INPUT=0;
    public static final int START_INPUT=1;
    public static final int READY_TO_SCAN=2;
    public static final int SCANNING=3;
    public static final int END_SCAN=4;
    public static final String DATA_TITLE_SCRAP="仪器名称                                                                         数量";
    public static final String DATA_TITLE_MAINTAIN="仪器名称                                              周期                 状态";
    public static final String DATA_TITLE_BILL= "   日期                   操作           仪器名称        数量/余量       操作人";
    public static final int EPC_LENGTH_LABEL=8;
    public static final int ECP_LENGTH_CUPBOARD=4;
    public static final int EPC_LENGTH_ADMIN=16;

    public static final String TTS_START="开始扫描，请稍后";
    public static final String TTS_ALERT="请刷身份卡或登记";
    public static final String TTS_RESULT="本次共";
    public static final String TTS_CAMERA="请将条形码对准摄像头进行补录";
    public static final String TTS_FINISH="确认完毕，可以离开";
}
