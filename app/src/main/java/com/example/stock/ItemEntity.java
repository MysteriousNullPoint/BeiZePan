package com.example.stock;

import com.tuacy.azlist.IAZItem;
import com.tuacy.fuzzysearchlibrary.IFuzzySearchItem;

import java.util.HashMap;
import java.util.List;

public class ItemEntity implements IAZItem, IFuzzySearchItem {

    private String mValue;
    private String mSortLetters;
    private List<String> mFuzzySearchKey;

    private List<String> cupName;
    private int stock;
    private int sum;
    private int used;
    private int scrapped;

    public ItemEntity(String value, String sortLetters, List<String> fuzzySearchKey) {
        mValue = value;
        mSortLetters = sortLetters;
        mFuzzySearchKey = fuzzySearchKey;

    }

    @Override
    public String getSortLetters() {
        return mSortLetters;
    }

    @Override
    public String getSourceKey() {
        return mValue;
    }

    @Override
    public List<String> getFuzzyKey() {
        return mFuzzySearchKey;
    }

    public String getValue(){return mValue;}

    public List<String> getCupName(){
        return cupName;
    }

    public void setStock(int mstock){
        stock=mstock;
    }

    public int getStock(){
        return stock;
    }

    public void setSum(int msum){
        sum=msum;
    }

    public int getSum(){
        return sum;
    }

    public void setUsed(int mused){
        used=mused;
    }

    public int getUsed(){
        return used;
    }

    public void setScrapped(int mScrap){
        scrapped=mScrap;
    }

    public int getScrapped(){
        return scrapped;
    }

    public void addScrapped(){
        scrapped++;
    }
}
