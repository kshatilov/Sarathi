package me.shatilov.symlab.sarathi.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import me.shatilov.symlab.sarathi.service.SarathiService;
import me.shatilov.utility.EasyCallable;

/**
 * Created by Kirill on 09-Feb-18.
 */

public class ServiceObserver extends BroadcastReceiver {

    private EasyCallable<String> callback;

    public ServiceObserver(@Nullable EasyCallable<String> callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String msg = bundle.getString(SarathiService.LOAD_PARAM);
            if (null != callback) {
                callback.accept("\n" + msg);
            }
        }
    }
}
