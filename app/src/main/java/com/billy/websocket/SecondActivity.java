package com.billy.websocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Billy_Cui on 2018/7/25
 * Describe: 测试内存泄漏
 */

public class SecondActivity extends AppCompatActivity {

    static Demo sDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (sDemo == null) {
            sDemo = new Demo();
        }
        Log.d("LeakCanary","SecondActivity");
        finish();
    }

    class Demo {
    }
}
