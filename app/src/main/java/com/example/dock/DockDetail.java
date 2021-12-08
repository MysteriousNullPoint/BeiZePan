package com.example.dock;

import java.util.ArrayList;
import java.util.List;

public class DockDetail {
    private String instruName;
    private List<String> cupList=new ArrayList<>();
    private int sum;

    public void setInstruName(String name) {
        this.instruName = name;
    }

    public String getInstruName() {
        return this.instruName;
    }

    public void addCupName(String cupName) {
        if(cupList.isEmpty()){
            cupList.add(cupName);
        }
        else{
            boolean isNew=true;
            for(String name :cupList){
                if(cupName.equals(name)) isNew=false;
            }
            if(isNew){
                cupList.add(cupName);
            }
        }
    }

    public List<String> getCupName() {
        return this.cupList;
    }

    public void addSum() {
        this.sum++;
    }

    public void setSum() {
        this.sum = 1;
    }

    public int getSum() {
        return this.sum;
    }


}

