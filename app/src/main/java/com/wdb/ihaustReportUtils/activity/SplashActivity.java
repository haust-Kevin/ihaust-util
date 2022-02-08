package com.wdb.ihaustReportUtils.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.wdb.ihaustReportUtils.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        getWindow().setBackgroundDrawableResource(R.drawable.startup);
        waitEnterMain();
    }
    private void waitEnterMain() {
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Intent it = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(it);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}