package com.tony.daemon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ToolsService extends Service {
    public ToolsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
