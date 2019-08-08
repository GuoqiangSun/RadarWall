package cn.com.startai.radarwall;

import android.graphics.PointF;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.swain.baselib.log.Tlog;

public class MainActivity {

    private String TAG = "radar";

    private MainActivity() {
    }

    public static MainActivity getInstance() {
        return ClassHolder.SENSOR;
    }

    private static class ClassHolder {
        private static final MainActivity SENSOR = new MainActivity();
    }

    private ExecutorService executorService;

    public synchronized ExecutorService initPool() {
        this.executorService = Executors.newFixedThreadPool(70);
        return this.executorService;
    }

    public synchronized void releaseService() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static final float[] DEGREE = {0f, 0.42f, 0.83f, 1.25f, 1.66f, 2.07f, 2.47f, 2.88f, 3.28f, 3.68f,
            4.08f, 4.47f, 4.87f, 5.26f, 5.65f, 6.04f, 6.42f, 6.81f, 7.19f, 7.57f, 7.95f, 8.33f, 8.70f, 9.08f, 9.45f,
            9.82f, 10.19f, 10.56f, 10.93f, 11.30f, 11.66f, 12.03f, 12.39f, 12.75f, 13.11f, 13.47f, 13.83f, 14.18f,
            14.54f, 14.90f, 15.25f, 15.60f, 15.95f, 16.31f, 16.66f, 17.01f, 17.35f, 17.70f, 18.05f, 18.39f, 18.74f,
            19.08f, 19.43f, 19.77f, 20.11f, 20.45f, 20.79f, 21.13f, 21.47f, 21.81f, 22.15f, 22.48f, 22.82f, 23.16f,
            23.49f, 23.83f, 24.16f, 24.49f, 24.82f, 25.16f, 25.49f, 25.82f, 26.15f, 26.48f, 26.81f, 27.14f, 27.46f,
            27.79f, 28.12f, 28.45f, 28.77f, 29.10f, 29.42f, 29.75f, 30.07f, 30.40f, 30.72f, 31.04f, 31.37f, 31.69f,
            32.01f, 32.33f, 32.65f, 32.98f, 33.30f, 33.62f, 33.94f, 34.26f, 34.58f, 34.90f, 35.21f, 35.53f, 35.85f,
            36.17f, 36.49f, 36.80f, 37.12f, 37.44f, 37.76f, 38.07f, 38.39f, 38.70f, 39.02f, 39.34f, 39.65f, 39.97f,
            40.28f, 40.60f, 40.91f, 41.23f, 41.54f, 41.86f, 42.17f, 42.48f, 42.80f, 43.11f, 43.42f, 43.74f, 44.05f,
            44.36f, 44.68f, 44.99f, 45.30f, 45.62f, 45.93f, 46.24f, 46.55f, 46.87f, 47.18f, 47.49f, 47.80f, 48.12f,
            48.43f, 48.74f, 49.05f, 49.37f, 49.68f, 49.99f, 50.30f, 50.61f, 50.93f, 51.24f, 51.55f, 51.86f, 52.17f,
            52.48f, 52.80f, 53.11f, 53.42f, 53.73f, 54.04f, 54.36f, 54.67f, 54.98f, 55.29f, 55.60f, 55.91f, 56.23f,
            56.54f, 56.85f, 57.16f, 57.47f, 57.79f, 58.10f, 58.41f, 58.72f, 59.03f, 59.35f, 59.66f, 59.97f, 60.28f,
            60.60f, 60.91f, 61.22f, 61.53f, 61.85f, 62.16f, 62.47f, 62.78f, 63.10f, 63.41f, 63.72f, 64.04f, 64.35f,
            64.66f, 64.98f, 65.29f, 65.61f, 65.92f, 66.23f, 66.55f, 66.86f, 67.18f, 67.49f, 67.81f, 68.12f, 68.44f,
            68.75f, 69.07f, 69.39f, 69.70f, 70.02f, 70.34f, 70.65f, 70.97f, 71.29f, 71.61f, 71.92f, 72.24f, 72.56f,
            72.88f, 73.20f, 73.52f, 73.84f, 74.16f, 74.48f, 74.80f, 75.12f, 75.44f, 75.76f, 76.09f, 76.41f, 76.73f,
            77.05f, 77.38f, 77.70f, 78.03f, 78.35f, 78.68f, 79.00f, 79.33f, 79.66f, 79.98f, 80.31f, 80.64f, 80.97f,
            81.30f, 81.63f, 81.96f, 82.29f, 82.62f, 82.95f, 83.28f, 83.62f, 83.95f, 84.28f, 84.62f, 84.95f, 85.29f,
            85.63f, 85.97f, 86.30f, 86.64f, 86.98f, 87.32f, 87.66f, 88.01f, 88.35f, 88.69f, 89.04f, 89.38f, 89.73f,
            90.07f, 90.42f, 90.77f, 91.12f, 91.47f, 91.82f, 92.17f, 92.53f, 92.88f, 93.23f, 93.59f, 93.95f, 94.31f,
            94.67f, 95.03f, 95.39f, 95.75f, 96.11f, 96.48f, 96.85f, 97.21f, 97.58f, 97.95f, 98.32f, 98.70f, 99.07f,
            99.45f, 99.83f, 100.21f, 100.59f, 100.97f, 101.35f, 101.74f, 102.13f, 102.52f, 102.91f, 103.30f, 103.70f,
            104.10f, 104.50f, 104.90f, 105.30f, 105.71f, 106.12f, 106.53f, 106.94f, 107.36f, 107.78f};


    public static void reserveBuf(char[] chars) {
//        char t;
//        for (int i = 0; i < chars.length / 2; i++) {
//            t = chars[i];
//            chars[i] = chars[chars.length - 1 - i];
//            chars[chars.length - 1 - i] = t;
//        }
    }

    public static final int RC_DEFAULT = -2;
    public static final int RC_FAILED = -1;
    public static final int RC_OK = 0; // 成功
    public static final int RC_INVALID_ARGUMENT = 1;
    public static final int RC_NOT_CONNECTED = 2;
    public static final int RC_ALREADY_CONNECTED = 3;
    public static final int RC_IN_PROGRESS = 4;
    public static final int RC_COMMUNICATION_FAILED = 5;
    public static final int RC_INVALID_RESPONSE = 6;
    public static final int RC_CONNECTION_CLOSED = 7;
    public static final int RC_ADDRESS_BIND_FAILED = 8;

    public static final int FRAME_DATA_SIZE = 320; // 有效点个数
    public static final int MAX_DISTANCE = 8000; // 最大距离
    public static final int WEAK_SIGNAL = 15000; // 信号弱
    public static final int MAX_SWEEP_ANGLE = 108; // 扫描角度
    public static final int MAX_FPS = 70;

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native int Initialize();

    public native void Uninitialize();

    public native int connect(String ip, int port);

    public native int connect(String ip, int port, String remoteIp, int remotePort);

    public native void disconnect();

    public native boolean isConnected();

    public native int setIntegrationTime(int time);

    public native int setTwiceIntegrationTime(int time1, int time2);

    public native int setLD(int gear);

    public native int[] acquirePositionData();

    public native int acquirePositionDataArray(char[] chars, int length);

    public native void alwaysAcquirePositionData();

    public native void stopAlwaysAcquirePositionData();

    public native int shell(String cmd);

    public native void tapxy(int x,int y);

    protected final void callBack(char[] buf, int size, int result) {
        if (mDataCallBack != null) {
            mDataCallBack.onPositionData(buf, size, result);
        } else {
            Tlog.v(TAG, " callBack()  mDataCallBack==null :: " + result);
        }
    }

    private IDataCallBack mDataCallBack;

    public void setCallBack(IDataCallBack mDataCallBack) {
        this.mDataCallBack = mDataCallBack;
    }

    public interface IDataCallBack {
        void onPositionData(char[] buf, int size, int result);
    }

}
