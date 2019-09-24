package cn.com.startai.radarwall;

import android.app.Application;
import android.os.Process;

import cn.com.startai.radarwall.calibration.CalibrationManager;
import cn.com.startai.radarwall.utils.FileManager;
import cn.com.startai.radarwall.utils.InjectInputPointManager;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/2
 * desc
 */
public class App extends Application implements Thread.UncaughtExceptionHandler {

    @Override
    public void onCreate() {
        super.onCreate();
        Tlog.setDebug(true);
        Thread.setDefaultUncaughtExceptionHandler(this);
        FileManager.getInstance().init(this);
        CalibrationManager.getInstance().init(this);
        InjectInputPointManager.getInstance().init(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        FileManager.getInstance().saveAppException(t, e);
        android.os.Process.killProcess(Process.myPid());
    }
}
