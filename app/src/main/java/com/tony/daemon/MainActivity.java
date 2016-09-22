package com.tony.daemon;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.tony.daemon.push.CoreService;
import com.tony.daemon.push.DeathRecipientInterface;
import com.tony.daemon.sync.SyncAuthService;

public class MainActivity extends BaseActivity {
    private static final String CONTENT_AUTHORITY = "com.tony.daemon";
    private Binder mBinder = new Binder();
    private DeathRecipientInterface mDeathRecipient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(AppContext.getInstance(), CoreService.class));
        startService(new Intent(AppContext.getInstance(), NotifyReceiver.NotifyService.class));
//        startService(new Intent(AppContext.getInstance(), ToolsService.class));
        triggerRefresh();

        bindService(new Intent(AppContext.getInstance(), CoreService.class), connection, Service.BIND_AUTO_CREATE);
    }

    public void triggerRefresh() {
        //账户调用Sync服务
//        Bundle b = new Bundle();
//        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//        ContentResolver.requestSync(account, CONTENT_AUTHORITY, b);
        //添加账号
        Account account = SyncAuthService.GetAccount(getString(R.string.account_auth_type));
        AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(account, null, null);
        //激活同步功能
        ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, new Bundle(), 10);//10s
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("NotifyReceiver", "Connected");
            mDeathRecipient = DeathRecipientInterface.Stub.asInterface(iBinder);
            try {
                mDeathRecipient.setBinder(mBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
                mDeathRecipient = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("NotifyReceiver", "Disconnected");
            mDeathRecipient = null;
            startService(new Intent(AppContext.getInstance(), CoreService.class));
        }
    };

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }
}
