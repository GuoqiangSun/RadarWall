package cn.com.startai.radarwall.utils;

import android.app.Application;

import java.io.File;

import cn.com.swain.baselib.file.FileTemplate;
import cn.com.swain.baselib.log.Tlog;

/**
 * author: Guoqiang_Sun
 * date : 2018/5/15 0015
 * desc :
 */
public class FileManager extends FileTemplate {

    private FileManager() {
    }

    private static final class ClassHolder {
        private static final FileManager FM = new FileManager();
    }

    public static FileManager getInstance() {
        return ClassHolder.FM;
    }

    @Override
    public void init(Application app) {
        super.init(app);

//        String absolutePath = getProjectPath().getAbsolutePath();

//        FileUtil.notifySystemToScan(app, absolutePath);

//        MediaScannerConnection.scanFile(app, new String[]{absolutePath}, null, null);

        Tlog.i(" FileManager init finish ; success:" + exit);
    }

    public void recreate(Application app) {
        super.init(app);

//        String absolutePath = getProjectPath().getAbsolutePath();

//        FileUtil.notifySystemToScan(app, absolutePath);

        Tlog.i(" FileManager recreate finish ; success:" + exit);
    }


    /**
     * app缓存数据的目录
     */
    protected File initMyProjectPath() {
        return new File(getAppRootPath(), "radar");
    }

    @Override
    protected String initMyAppRootPath() {
        return "startai";
    }
}
