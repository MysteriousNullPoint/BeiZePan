package com.example.input;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.example.cupboard.R;

public class MyPicker extends NumberPicker {

    public MyPicker(Context context) {
        super(context);
    }

    public MyPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {
            //设置文字的颜色和大小
            ((EditText) view).setTextColor(getResources().getColor(R.color.blue));
            ((EditText) view).setTextSize(25);
        }
    }
}

