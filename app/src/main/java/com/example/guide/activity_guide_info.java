package com.example.guide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cupboard.R;

public class activity_guide_info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_info);
        ImageView next_step=(ImageView)findViewById(R.id.next_step);
        EditText school=(EditText)findViewById(R.id.school);
        EditText lab=(EditText)findViewById(R.id.lab);
        next_step.setImageAlpha(165);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        school.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editIsNull(school,lab,next_step);
            }
        });
        lab.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editIsNull(school,lab,next_step);
            }
        });

        next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(school.getText().toString().length()==0 || lab.getText().toString().length()==0) {
                    Toast.makeText(activity_guide_info.this, "请输入学校名和实验室名", Toast.LENGTH_LONG).show();
                }
                else{
                    set_info(school,lab);
                    Intent intent=new Intent(activity_guide_info.this,activity_guide_date.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    private void editIsNull(EditText school,EditText lab,ImageView image)
    {
        if(school.getText().toString().length()==0 || lab.getText().toString().length()==0) {
            image.setImageAlpha(165);
        }
        else{image.setImageAlpha(255);}
    }

    private void set_info(EditText school,EditText lab)
    {
        String school_name=school.getText().toString();
        String lab_name=lab.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("FirstRun",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("school",school_name);
        editor.putString("lab",lab_name);
        editor.putString("days","30");
        editor.apply();
    }
}