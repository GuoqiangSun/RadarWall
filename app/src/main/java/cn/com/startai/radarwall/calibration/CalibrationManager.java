package cn.com.startai.radarwall.calibration;

import android.content.Context;

import java.io.Serializable;

import cn.com.swain.baselib.clone.SerialManager;
import cn.com.swain.baselib.display.PointS;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/11
 * desc
 */
public class CalibrationManager implements Serializable {

    private static final String TAG = "Calibration";

    public static void save(Context ctx) {
        CalibrationManager instance = getInstance();
        boolean calibrationManager = SerialManager.saveObj(ctx, instance, "CalibrationManager");
        String msg = instance == null ? "null" : instance.toString();
        Tlog.v(TAG, "CalibrationManager save:" + calibrationManager + " : " + msg);
    }

    public static void get(Context ctx) {

        mCalibrationManager = (CalibrationManager) SerialManager.getObj(ctx, "CalibrationManager");
        String msg = mCalibrationManager == null ? "null" : mCalibrationManager.toString();
        Tlog.v(TAG, "CalibrationManager get: " + msg);

    }

    @Override
    public String toString() {
        return "hashcode:" + hashCode() + " createTimes:" + createTime;
    }

    private final Calibration mCalibration;
    private final long createTime;

    private CalibrationManager() {
        mCalibration = new Calibration();
        createTime = System.currentTimeMillis();
        Tlog.v(TAG, " new CalibrationManager() createTime:" + createTime);
    }

    private volatile static CalibrationManager mCalibrationManager;

    public static CalibrationManager getInstance() {
        if (mCalibrationManager == null) {
            synchronized (CalibrationManager.class) {
                if (mCalibrationManager == null) {
                    mCalibrationManager = new CalibrationManager();
                }
            }
        }
        return mCalibrationManager;
    }

    private Object readResolve() {
        return mCalibrationManager;
    }

    public void setPositionData(int result, char[] buf) {
        mCalibration.setPositionData(result, buf);
    }

    public void setCollectPoint(PointS a, PointS b, PointS c, PointS d, PointS s) {
        mCalibration.setCollectPoint(a, b, c, d, s);
    }

    public void start() {
        mCalibration.start();
    }

    public void stop() {
        mCalibration.stop();
    }

    public void setCollectIndex(int i) {
        mCalibration.setCollectIndex(i);
    }

    public void setCalibrationCallBack(Calibration.ICalibrationCallBack mCallBack) {
        mCalibration.setCalibrationCallBack(mCallBack);
    }

    public void setIVertexFinish(Calibration.IVertexFinish mVertexFinish) {
        mCalibration.setIVertexFinish(mVertexFinish);
    }
}
