package cn.com.startai.radarwall;

import android.app.Application;

import cn.com.startai.radarwall.utils.FileManager;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/2
 * desc
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FileManager.getInstance().init(this);
        Tlog.setDebug(true);
    }
}
