package com.tony.daemon;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by tony on 9/18/16.
 */
public class BaseActivity extends AppCompatActivity{
    private PowerManager.WakeLock mwl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mwl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyTag");
        mwl.acquire();//屏幕关闭后保持活动
    }

    @Override
    protected void onDestroy() {
        mwl.release();//释放
        super.onDestroy();
    }
}
