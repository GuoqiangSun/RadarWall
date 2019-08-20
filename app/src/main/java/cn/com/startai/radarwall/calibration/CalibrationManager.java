package cn.com.startai.radarwall.calibration;

import android.app.Application;
import android.content.Context;

import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.app.IApp.IApp;
import cn.com.swain.baselib.app.utils.DataCleanManager;
import cn.com.swain.baselib.clone.SerialManager;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/11
 * desc
 */
public class CalibrationManager implements IApp {

    public static final String TAG = "Calibration";

    public static void save(Context ctx) {
        Calibration mCalibration = getInstance().mCalibration;
        Tlog.v(TAG, " CalibrationManager save: " + mCalibration);
        if (mCalibration != null) {
            boolean flag = SerialManager.saveObj(mCalibration, ctx, "CalibrationManager.java");
            Tlog.v(TAG, " CalibrationManager save: " + flag);
        }
    }

    @Override
    public void init(Application app) {
        if (mCalibration == null) {
            Object obj = SerialManager.getObj(app.getApplicationContext(), "CalibrationManager.java");
            Tlog.v(TAG, " CalibrationManager init: " + obj);
            if (!(obj instanceof Calibration)) {
                obj = new Calibration();
            }
            setCalibration((Calibration) obj);
        }
    }

    public static void clear(Context ctx) {
        DataCleanManager.cleanInternalCache(ctx);
    }

    private Calibration mCalibration;

    private void setCalibration(Calibration mCalibration) {
        this.mCalibration = mCalibration;
    }

    private Calibration getCalibration() {
        return mCalibration;
    }

    private final long createTime;

    private CalibrationManager() {
        createTime = System.currentTimeMillis();
        Tlog.v(TAG, " new CalibrationManager() :: " + toString());
    }

    private static class ClassHolder {
        private static final CalibrationManager mCalibrationManager = new CalibrationManager();
    }

    public static CalibrationManager getInstance() {
        return ClassHolder.mCalibrationManager;
    }

    public void setPositionData(int result, char[] buf) {
        getCalibration().setPositionData(result, buf);
    }

    public void setCollectPoint(PointS a, PointS b, PointS c, PointS d, PointS s) {
        getCalibration().setCollectPoint(a, b, c, d, s);
    }

    public void start() {
        getCalibration().start();
    }

    public void stop() {
        getCalibration().stop();
    }

    public void setCollectIndex(int i) {
        getCalibration().setCollectIndex(i);
    }

    public void setCalibrationCallBack(Calibration.ICalibrationCallBack mCallBack) {
        getCalibration().setCalibrationCallBack(mCallBack);
    }

    public void setIVertexFinish(Calibration.IVertexFinish mVertexFinish) {
        getCalibration().setIVertexFinish(mVertexFinish);
    }

    @Override
    public String toString() {
        return "CalibrationManager[hashcode:" + hashCode() + " createTimes:" + createTime + "] mCalibration=" + (mCalibration);
    }

}
