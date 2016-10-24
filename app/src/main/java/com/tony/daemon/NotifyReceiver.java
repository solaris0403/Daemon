package com.tony.daemon;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tony.daemon.push.CoreService;

public class NotifyReceiver extends BroadcastReceiver {
    private static final String ACTION_POLL = "IS_POLL";//是否需要轮询

    public NotifyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("123", intent.getAction());
        AppContext.getInstance().startService(new Intent(AppContext.getInstance(), CoreService.class));
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED://开机
                break;
            case Intent.ACTION_USER_PRESENT://解锁
                break;
            case Intent.ACTION_BATTERY_CHANGED://电量变化
                break;
            case Intent.ACTION_TIME_TICK://时间改变
                break;
            case Intent.ACTION_SCREEN_ON://屏幕亮起
                break;
            case Intent.ACTION_SCREEN_OFF://屏幕变黑
                break;
            case Intent.ACTION_PACKAGE_RESTARTED://force stop
                break;
            case NotifyService.ALARM_ACTION://定时闹钟
                if (intent.getBooleanExtra(ACTION_POLL, false)) {
                    Intent pollIntent = new Intent(AppContext.getInstance(), NotifyService.class);
                    pollIntent.putExtra(ACTION_POLL, true);
                    AppContext.getInstance().startService(pollIntent);
                }
                break;
            case NotifyService.JOB_ACTION://定时job
                break;
            case NotificationListener.ACTION_NOTIFICATION:
                break;
            default:
                break;
        }
    }

    public static class NotifyService extends Service {
        private NotifyReceiver mNotifyReceiver;
        private AlarmManager mAlarmManager;
        private static final int REQUEST_CODE = 10001;
        public static final String ALARM_ACTION = "com.tony.daemon.alarm";
        public static final String JOB_ACTION = "com.tony.daemon.job";

        private static final long INTERVAL_TIME = AlarmManager.INTERVAL_HALF_HOUR;//轮询间隔

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            registerNotifyReceiver();
            startPoll();
            startNotificationListener();
            startMonitor();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            LogUtils.i("onStartCommand");
            if (intent != null && intent.getBooleanExtra(ACTION_POLL, false)) {
                startPoll();
            }
            return Service.START_STICKY;
        }

        /**
         * 需要手动设置权限，一般情况不会使用
         */
        private void startNotificationListener() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                startService(new Intent(this, NotificationListener.class));
            }
        }

        /**
         * 通过系统广播唤醒
         */
        private void registerNotifyReceiver() {
            mNotifyReceiver = new NotifyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);//电量变化
            intentFilter.addAction(Intent.ACTION_TIME_TICK);//时间改变
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);//屏幕亮起
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);//屏幕变黑
            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            registerReceiver(mNotifyReceiver, intentFilter);
        }

        /**
         * 使用系统AlarmManager/JobScheduler进行轮询操作
         */
        private void startPoll() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//5.0一下，setInexactRepeating，节约资源
                mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, new Intent(ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
                mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_TIME, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//5.0-6.0 节约资源
                JobScheduler JobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
                builder.setPeriodic(INTERVAL_TIME);
                builder.setPersisted(true);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//只在有网络情况下激活
                JobScheduler.schedule(builder.build());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0以上 setAndAllowWhileIdle() 在Doze模式下启动的alarms
                mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(ALARM_ACTION);
                intent.putExtra(ACTION_POLL, true);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, new Intent(ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
                //单次轮询
                mAlarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + INTERVAL_TIME, pendingIntent);
            } else {//默认使用第一种
                mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, new Intent(ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
                mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_TIME, pendingIntent);
            }
        }

        /**
         * 可并入startPoll，测试用
         */
        private void startMonitor() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(3000);
                    AppContext.getInstance().startService(new Intent(AppContext.getInstance(), CoreService.class));
                }
            }).start();
        }

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

        @Override
        public void onDestroy() {
            unregisterReceiver(mNotifyReceiver);
            super.onDestroy();
        }
    }

    @SuppressLint("OverrideAbstract")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static class NotificationListener extends NotificationListenerService {
        public static final String ACTION_NOTIFICATION = "com.tony.daemon.notification";

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return Service.START_STICKY;
        }

        @Override
        public void onNotificationPosted(StatusBarNotification sbn) {
            super.onNotificationPosted(sbn);
            sendBroadcast(new Intent(ACTION_NOTIFICATION));
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification sbn) {
            super.onNotificationRemoved(sbn);
            sendBroadcast(new Intent(ACTION_NOTIFICATION));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class JobSchedulerService extends JobService {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return Service.START_STICKY;
        }

        @Override
        public boolean onStartJob(JobParameters jobParameters) {
            sendBroadcast(new Intent(NotifyService.JOB_ACTION));
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return false;
        }
    }
}
