package com.example.cameralessphotography;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends Activity {

    private MenuAdapter menuAdapter;
    private RecyclerView menuRCV;

    private List<String> menuItemList = Arrays.asList("拍攝方向", "長寬比", "閃光燈", "曝光時間", "光圈", "ISO", "白平衡", "焦距");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        init();
    }

    private void init()
    {
        menuRCV = findViewById(R.id.menuRecyclerview);
        menuAdapter = new MenuAdapter(this, menuItemList);
        menuRCV.setAdapter(menuAdapter);
        menuRCV.setLayoutManager(new LinearLayoutManager(this));

    }


    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);

            startActivity(intent);
        }
        return true;
    }
}
