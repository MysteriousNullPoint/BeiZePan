package com.example.file;

import java.util.HashMap;
import java.util.List;

public class ExportData {
    private String name;
    private String location;
    private String remain;
    private String stock;
    private String used;
    private String scrapped;
    private String type;
    private String class_name;

    public ExportData(String type,String instrument_name,String location,String remain,String stock,String used,String scrapped) {
        this.type=type;
        this.name = instrument_name;
        this.location=location;
        this.remain=remain;
        this.stock=stock;
        this.used=used;
        this.scrapped=scrapped;
    }

    public ExportData(String type,String instrument_name,String class_name,String sum){
        this.type=type;
        this.name = instrument_name;
        this.class_name=class_name;
        this.stock=sum;
    }

    public ExportData(String type,String instrument_name,String sum){
        this.type=type;
        this.name = instrument_name;
        this.stock=sum;
    }

    public ExportData(String type,String instrument_name,String location,String remain,String stock,String used) {
        this.type=type;
        this.name = instrument_name;
        this.location=location;
        this.remain=remain;
        this.stock=stock;
        this.used=used;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public String getStock(){
        return stock;
    }

    public String getRemain(){
        return remain;
    }

    public String getUsed(){
        return used;
    }

    public String getScrapped(){
        return scrapped;
    }

    public String getType(){
        return type;
    }

    public String getClass_name(){
        return class_name;
    }
}
