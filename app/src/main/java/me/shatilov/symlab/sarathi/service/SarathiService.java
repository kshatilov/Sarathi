package me.shatilov.symlab.sarathi.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import me.shatilov.symlab.sarathi.mqqt.MqqtClientWrapper;

public class SarathiService extends Service {
    public static final String SERVICE_NAME = "SymLab.SarathiService";
    public static final String URI_PARAM = "SarathiService.sender.uri";
    public static final String TOPIC_PARAM = "SarathiService.sender.topic";
    public static final String LOAD_PARAM = "SarathiService.sender.workload";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private MqqtClientWrapper sender;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SarathiService getService() {
            return SarathiService.this;
        }
    }

    private final class ServiceHandler extends Handler {
        private MqqtClientWrapper sender;

        public void setSender(MqqtClientWrapper sender) {
            this.sender = sender;
        }

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            sender.publishMessage(msg.getData().getString(LOAD_PARAM));
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (null != bundle &&
                null != bundle.getString(URI_PARAM) &&
                null != bundle.getString(TOPIC_PARAM)) {

            sender = new MqqtClientWrapper(
                    this,
                    bundle.getString(URI_PARAM),
                    bundle.getString(TOPIC_PARAM),
                    SERVICE_NAME,
                    (notification) -> {/*ignored*/},
                    (message) -> {/*ignored*/});
            sender.connect();
            mServiceHandler.setSender(sender);
        }

        return mBinder;
    }

    public void sendData(String load) {
        Bundle bundle = new Bundle();
        Message msg = mServiceHandler.obtainMessage();
        bundle.putString(LOAD_PARAM, load);
        msg.setData(bundle);
        mServiceHandler.handleMessage(msg);

        Intent notification = new Intent(SERVICE_NAME);
        notification.putExtra(LOAD_PARAM, load);
        sendBroadcast(notification);
    }

    @Override
    public void onDestroy() {
        sender.disconnect();
    }
}