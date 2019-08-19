package cn.com.startai.radarwall.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;

import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/16
 * desc
 */
public class RadarService extends Service {

    private static final String TAG = "RadarService";

    private class Binder extends IRadarAidl.Stub {

        private Handler handler;

        private Binder() {
            super();
            Tlog.v(TAG, "RadarService new Binder");
            HandlerThread handlerThread = new HandlerThread("123");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        @Override
        public void basicTypes(int anInt,
                               long aLong,
                               boolean aBoolean,
                               float aFloat,
                               double aDouble,
                               String aString) throws RemoteException {
            Tlog.v(TAG, " IRadarAidl.basicTypes() ");
        }

        @Override
        public int add(final int arg1, final int arg2) throws RemoteException {
            Tlog.v(TAG, " IRadarAidl.add(" + arg1 + "," + arg2 + ")");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Tlog.v(TAG, "post IRadarAidl.add(" + arg1 + "," + arg2 + ")");
                }
            });
            return arg1 + arg2;
        }

    }

    private final Binder binder = new Binder();

    @Override
    public IBinder onBind(Intent intent) {
        Tlog.v(TAG, "RadarService onBind ");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Tlog.v(TAG, "RadarService onUnbind ");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Tlog.v(TAG, "RadarService onCreate ");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Tlog.v(TAG, "RadarService onDestroy ");
        super.onDestroy();
    }
}
