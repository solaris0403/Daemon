package com.tony.daemon.push;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tony.daemon.AppContext;
import com.tony.daemon.LogUtils;
import com.tony.daemon.NotifyReceiver;

public class CoreService extends Service {
    private static final int FOREGROUNDS_SERVICE_ID = 10001;
    public static final String ACTION_NAME = "com.daemon.coreservice";
    private static boolean isRunning;
    private Thread mThread;

    public CoreService() {
//        1.弱引用或软引用，
//        2.尽量多地去置空一些不必要的引用并在需要的时候再赋值，
//        3.Service本身也提供了onTrimMemory何时需要释放掉不必要的资源，灵活使用这类方法可以最大程度的让我们的后台Service
//        4.尽量让我们的后台进程做更少的事情，及时释放资源，才是硬道理。
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DeathRecipientHandler();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    Log.i("123", "run...");
                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        mThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("123", "onDestroy");
        if (mThread.isAlive()) {
            mThread.interrupt();
        }
        super.onDestroy();
        //防止手动停止
        restartService();
    }

    //==============================================================================================

    /**
     * 设置前台Service 提高优先级
     */
    private void startForegroundService() {
        //思路一：API < 18，启动前台Service时直接传入new Notification()；此方法能有效隐藏Notification上的图标
        //思路二：API >= 18，同时启动两个id相同的前台Service，然后再将后启动的Service做stop处理；
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(FOREGROUNDS_SERVICE_ID, new Notification());
        } else {
            Intent innerIntent = new Intent(this, InnerService.class);
            startService(innerIntent);
            startForeground(FOREGROUNDS_SERVICE_ID, new Notification());
        }
    }

    public static class InnerService extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(FOREGROUNDS_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }
    //==============================================================================================

    /**
     * 重启service
     */
    private void restartService() {
        startService(new Intent(AppContext.getInstance(), CoreService.class));
    }
    //==============================================================================================

    /**
     * 轮询启动
     */
    private void startMonitor() {
        if (!isRunning) {
            isRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("NotifyReceiver", "CoreService Runnable");
                    SystemClock.sleep(3000);
                    startService(new Intent(CoreService.this, NotifyReceiver.NotifyService.class));
                }
            }).start();
        }
    }
    //==============================================================================================

    /**
     * 死亡监听
     */
    private class DeathRecipientHandler extends DeathRecipientInterface.Stub {
        @Override
        public void setBinder(IBinder client) throws RemoteException {
            if (client != null) {
                client.linkToDeath(new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        Log.e("123", "client died");
                        startService(new Intent(CoreService.this, NotifyReceiver.NotifyService.class));
                    }
                }, 0);
            }
        }
    }

    //==============================================================================================
    @Override
    public void onTrimMemory(int level) {
        LogUtils.i("onTrimMemory---" + level);
        switch (level) {
            case TRIM_MEMORY_MODERATE://表示手机目前内存已经很低了，并且我们的程序处于LRU缓存列表的中间位置，如果手机内存还得不到进一步释放的话，那么我们的程序就有被系统杀掉的风险了。
                System.gc();
                break;
            case TRIM_MEMORY_RUNNING_CRITICAL://应用程序仍然正常运行，但是系统已经根据LRU缓存规则杀掉了大部分缓存的进程了。这个时候我们应当尽可能地去释放任何不必要的资源，不然的话系统可能会继续杀掉所有缓存中的进程
                System.gc();
                break;
        }
        super.onTrimMemory(level);
    }
}
