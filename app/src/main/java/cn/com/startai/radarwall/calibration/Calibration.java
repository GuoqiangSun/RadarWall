package cn.com.startai.radarwall.calibration;

import android.os.Process;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.startai.radarwall.RadarSensor;
import cn.com.startai.radarwall.utils.PrintData;
import cn.com.swain.baselib.alg.MathUtils;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/7/30
 * desc
 */
public class Calibration implements Runnable, Serializable {

    private final String TAG = CalibrationManager.TAG;

    private String TAG_LARG = "distance";

    // 可以校准
    private boolean canCalibration;

    // 校准成功
    private boolean calibrationFinish;

    private final int SIZE = RadarSensor.FRAME_DATA_SIZE;
    private final int MAX_DISTANCE = RadarSensor.MAX_DISTANCE;
    private final float[] DEGREE = RadarSensor.DEGREE;
    private final int WEAK_SIGNAL = RadarSensor.WEAK_SIGNAL;
    private final int DATA_HZ = RadarSensor.MAX_FPS;
    private final int TOUCH_FPS = 15; // 1秒 x帧
    private final int AVAILABLE_TOUCH_LENGTH = DATA_HZ / TOUCH_FPS;
    private final int clearPS = Math.min(Math.max(AVAILABLE_TOUCH_LENGTH / 2, 1), 5);

    private transient ExecutorService executorService;

    private WallScreen mWallScreen;
    private VertexCollect mVertexCollect;
    private BackgroundData mBackgroundData;

    private PointS[] mAvailableTouchPointInScreen;
    private final long createTime;

    private transient ICalibrationCallBack mCallBack;

    public void setCalibrationCallBack(ICalibrationCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    private transient IVertexFinish mVertexFinish;

    public void setIVertexFinish(IVertexFinish mVertexFinish) {
        this.mVertexFinish = mVertexFinish;
    }

    Calibration() {
        this.mWallScreen = new WallScreen();
        this.canCalibration = false;

        this.executorService = Executors.newSingleThreadExecutor();

        this.mBackgroundData = new BackgroundData();
        this.BG = mBackgroundData.getAvgBg();

        this.mVertexCollect = new VertexCollect();

        mAvailableTouchPointInScreen = new PointS[AVAILABLE_TOUCH_LENGTH];
        for (int i = 0; i < AVAILABLE_TOUCH_LENGTH; i++) {
            mAvailableTouchPointInScreen[i] = new PointS();
        }
        createTime = System.currentTimeMillis();
        Tlog.v(TAG, " new Calibration() :: " + toString());
    }

    public Calibration(PointS mPointA,
                       PointS mPointB,
                       PointS mPointC,
                       PointS mPointD,
                       PointS mPointScreen) {
        this();
        setCollectPoint(mPointA, mPointB, mPointC, mPointD, mPointScreen);
    }

    public void setCollectPoint(PointS mPointA,
                                PointS mPointB,
                                PointS mPointC,
                                PointS mPointD,
                                PointS mPhoneScreen) {
        this.mWallScreen.set(mPointA, mPointB, mPointC, mPointD, mPhoneScreen);
        this.canCalibration = true;
    }

    public void start() {
        if (this.run) {
            return;
        }
        this.run = true;
        if (executorService == null) {
            Tlog.v(TAG, " Calibration start() newSingleThreadExecutor()");
            this.executorService = Executors.newSingleThreadExecutor();
        }
        this.executorService.execute(this);
    }

    public void stop() {
        if (!this.run) {
            return;
        }
        this.run = false;
        synchronized (synObj) {
            synObj.notify();
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    private final Object synObj = new byte[1];

    private volatile boolean run;

    private char[] BG;

    private int collectIndex = -1;

    public void setCollectIndex(int i) {
        calibrationFinish = false;
        collectIndex = i;
        if (mVertexCollect != null) {
            mVertexCollect.resetIndex(i);
        }
    }

    private char[] mBuf;

    public void setPositionData(int result, char[] buf) {
        if (result == RadarSensor.RC_OK) {
            this.mBuf = buf;
            synchronized (synObj) {
                synObj.notify();
            }
        }
    }

    @Override
    public void run() {

        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

        Tlog.e(TAG, " Calibration run start :: " + run);

        char[] mDistanceBuf;
        final int[] mDiffBuf = new int[SIZE]; // 测试数据减去背景数据的差值

        final int[] mAvailableIndexBuf = new int[SIZE]; // 可用下标
        final PointS nullPointFSerial = new PointS(-30f, 0f);
        final PointS touchPointSInWall = new PointS(-30f, 0f);
        final PointS touchPointSInScreen = new PointS();

        if (mBackgroundData.countBGFinish()) {
            if (this.mCallBack != null) {
                this.mCallBack.onWallBG(BG);
            }
            if (mVertexCollect.collectFinish()) {
                if (mVertexFinish != null) {
                    mVertexFinish.onCollectPointInWall(mVertexCollect.getPointSS());
                    mVertexFinish.onCollectPointInScreen(mWallScreen.calculationVertexInScreen());
                    mVertexFinish.onVirtualScreenRect(mWallScreen.calculationVirtualScreenRect());
                    mVertexFinish.onVirtualScreen(mWallScreen.calculationVirtualScreen());
                    mVertexFinish.showTxt(mWallScreen.getMsg());
                }
            }
        }

        while (run) {

            synchronized (synObj) {
//                Tlog.e(TAG, " Calibration synObj.wait() ");
                try {
                    synObj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 先把数据copy出来,避免计算时，数组内容被重置
            mDistanceBuf = mBuf;

            if (mDistanceBuf == null) {
                Tlog.e(TAG, " mDistanceBuf == null continue;");
                continue;
            }

            runFps();

            if (!countBg(mDistanceBuf)) {
                Tlog.i(TAG, " count Background data ... ");
            }

            // 计算 测试数据 和 背景数据的 差值
            // 并求差值中的极值
//            Arrays.stream(mDiffBuf).max().getAsInt();
            int availableSize = obstacleIndex(mDiffBuf, BG, mDistanceBuf, mAvailableIndexBuf);
            if (this.mCallBack != null) {
                this.mCallBack.onWallBGDiff(mDiffBuf, SIZE);
            }

            PrintData.println(TAG_LARG, mDistanceBuf, "DATA:");
            PrintData.println(TAG_LARG, BG, "BACK:");
            PrintData.println(TAG_LARG, mDiffBuf, "DIFF:");

            if (availableSize < 3) {
                notouchTimes++;
                if (notouchTimes > clearPS) {
                    addPointIndex = 0;

                    if (this.mCallBack != null) {
                        this.mCallBack.onTouchPointInScreen(nullPointFSerial);
                    }

                }
                if (this.mCallBack != null) {
                    this.mCallBack.onTouchPointInWall(nullPointFSerial);
                }
                continue;
            }
            Tlog.e(TAG_LARG, " find touch point ");

            calculationTouchPoint(touchPointSInWall, mAvailableIndexBuf, availableSize, mDistanceBuf);

            if (this.mCallBack != null) {
                this.mCallBack.onTouchPointInWall(touchPointSInWall);
            }

            if (!calibrationFinish) {
                if (canCalibration && calculationVertex(touchPointSInWall) >= 4) {
                    calibrationFinish = true;
                    collectIndex = -1;
                    mVertexCollect.calculationABCD();
                    mWallScreen.setWallPoint(mVertexCollect.getPointSS());
                    if (mVertexFinish != null) {
                        mVertexFinish.onCollectPointInWall(mVertexCollect.getPointSS());
                        mVertexFinish.onCollectPointInScreen(mWallScreen.calculationVertexInScreen());
                        mVertexFinish.onVirtualScreenRect(mWallScreen.calculationVirtualScreenRect());
                        mVertexFinish.onVirtualScreen(mWallScreen.calculationVirtualScreen());
                        mVertexFinish.showTxt(mWallScreen.getMsg());
                    }
                }

                continue;
            }

            mWallScreen.calculationInScreen(touchPointSInWall, touchPointSInScreen);
            onTouchPointInScreen(touchPointSInScreen);
        }

        Tlog.e(TAG, " Calibration run finish ");
    }

    private int fps = 0;
    private int lastFps = 0;
    private long lastFpsTime = 0;

    // 算法运行的帧率
    private void runFps() {
        long l = System.currentTimeMillis();
        if (Math.abs(lastFpsTime - l) >= 1000) {
            lastFps = fps;
            fps = 0;
            lastFpsTime = l;
        }
        fps++;
        if (mCallBack != null) {
            mCallBack.onFps(fps, lastFps);
        }
    }

    private int notouchTimes = 0;
    private int addPointIndex = 0;
    private final PointS avgPoints = new PointS();
    private final PointS totalPoints = new PointS();

    private void onTouchPointInScreen(PointS mPointS) {
        notouchTimes = 0;
        int i = addPointIndex % AVAILABLE_TOUCH_LENGTH;
        mAvailableTouchPointInScreen[i].set(mPointS);
        addPointIndex++;
        totalPoints.x += mPointS.x;
        totalPoints.y += mPointS.y;
        if (i == (AVAILABLE_TOUCH_LENGTH - 1)) {
            avgPoints.x = 0;
            avgPoints.y = 0;

            avgPoints.x = totalPoints.x / AVAILABLE_TOUCH_LENGTH;
            avgPoints.y = totalPoints.y / AVAILABLE_TOUCH_LENGTH;

            totalPoints.x = 0;
            totalPoints.y = 0;

            if (mCallBack != null) {
                mCallBack.onTouchPointInScreen(avgPoints);
            }
            touchFps();
        }
    }

    private int touchFps = 0;
    private int lastTouchFps = 0;
    private long lastTouchFpsTime = 0;

    // 触摸点的帧率
    private void touchFps() {
        long l = System.currentTimeMillis();
        if (Math.abs(lastTouchFpsTime - l) >= 1000) {
            lastTouchFps = touchFps;
            touchFps = 0;
            lastTouchFpsTime = l;
        }
        touchFps++;
        if (mCallBack != null) {
            mCallBack.onTouchFps(touchFps, lastTouchFps);
        }
    }

    private int calculationVertex(PointS touchPointS) {
        int add = mVertexCollect.add(touchPointS, collectIndex);
        switch (add) {
            case -1:
                Tlog.i(TAG, " calculation vertex data ...");
                break;
            case 1:
                collectIndex = -1;
                if (mVertexFinish != null) {
                    mVertexFinish.onCollectPointInWall(0, mVertexCollect.getPointS(0));
                }
                break;
            case 2:
                collectIndex = -1;
                if (mVertexFinish != null) {
                    mVertexFinish.onCollectPointInWall(1, mVertexCollect.getPointS(1));
                }
                break;
            case 3:
                collectIndex = -1;
                if (mVertexFinish != null) {
                    mVertexFinish.onCollectPointInWall(2, mVertexCollect.getPointS(2));
                }
                break;
            case 4:
                collectIndex = -1;
                if (mVertexFinish != null) {
                    mVertexFinish.onCollectPointInWall(3, mVertexCollect.getPointS(3));
                }
                break;
        }

        return add;

    }

    private boolean countBg(char[] mDistanceBuf) {
        if (mBackgroundData.countBGFinish()) {
            return true;
        }
        boolean collect = mBackgroundData.setOneFrame(mDistanceBuf);
        if (collect) {
            if (this.mCallBack != null) {
                this.mCallBack.onWallBG(BG);
            }
        }
        return collect;
    }

    // 计算触摸点在墙上的xy
    private void calculationTouchPoint(PointS touchPointS, int[] mAvailableIndexBuf,
                                       int availableSize, char[] mDistanceBuf) {
        float totalDegree = 0;
        float totalDistance = 0;
        for (int i = 0; i < availableSize; i++) {
            int mAvailableIndex1 = mAvailableIndexBuf[i];
            totalDegree += DEGREE[mAvailableIndex1];
            totalDistance += mDistanceBuf[mAvailableIndex1];
        }

        float avgDegree = totalDegree / availableSize;
        float avgDistance = totalDistance / availableSize;
        touchPointS.x = (float) (avgDistance * MathUtils.sin(avgDegree));
        touchPointS.y = (float) (avgDistance * MathUtils.cos(avgDegree));

        Tlog.v(TAG, " totalDegree: " + totalDegree + " avgDegree:" + avgDegree);
        Tlog.v(TAG, " totalDistance: " + totalDistance + " avgDistance:" + avgDistance);
        Tlog.v(TAG, " touchPoint: " + touchPointS.toString());

    }

    private final int MAX_DIFF_DIS = -500; //单位  nm
    private final int MIN_DIFF_DIS = -5000;//单位  nm

    private final int MMIN_DIFF_DIS = -10000;//单位  nm
    private final int MMAX_DIFF_DIS = -14500;//单位  nm

    private final int MIN_DIS_AVAILABLE = 500;
    private final int MAX_DIS_AVAILABLE = 6000;//单位  nm

    /**
     * 求buf最大值的下标
     */
    private int obstacleIndex(int[] diff, char[] bg, char[] mDistanceBuf, int[] mAvailableIndexBuf) {
        int maxAvailable = 0;
        int mAvailableSize = -1;

        for (int i = 0; i < SIZE; i++) {

            int mBufDis = (int) mDistanceBuf[i];
            int mBgDis = (int) bg[i];
            int mDiff = diff[i] = mBufDis - mBgDis;

            if (i < 10 || i > (SIZE - 10)) { // 前后角不做处理
                continue;
            }

            // 当前距离无效,跳两步，看第三点数据
            if (mBufDis > MAX_DISTANCE) {
                int u = 2;
                while (u > 0 && (i < SIZE - 1)) {
                    i++;
                    mBufDis = (int) mDistanceBuf[i];
                    diff[i] = mBufDis - (int) bg[i];
                    if (mBufDis < MAX_DISTANCE) {
                        u--;
                    } else if (++u > 2) {
                        u = 2;
                    }

                }

                continue;
            }

            boolean findAvailable = false;
            // 有效差距求最大值
            if ((mDiff < MAX_DIFF_DIS && mDiff > MIN_DIFF_DIS // 背景有效时
                    && (mDistanceBuf[i + 1] < MAX_DISTANCE)// 下两点数据有效
                    && (mDistanceBuf[i + 2] < MAX_DISTANCE))
                    ||
                    (mDiff < MMIN_DIFF_DIS && mDiff > MMAX_DIFF_DIS //背景无效时
                            && mDistanceBuf[i - 1] < MAX_DISTANCE // 下一点和下一点数据有效
                            && mDistanceBuf[i + 1] < MAX_DISTANCE)) {

                int absHead = Math.abs(mDiff);
                if (absHead > maxAvailable) {
                    maxAvailable = absHead;
                    findAvailable = true;
                    Tlog.i(TAG_LARG, "mDiff:" + mDiff
                            + " maxAvailable:" + maxAvailable
                            + " indexAvailable:" + i);
                }
            }

            if (findAvailable) { // 找到了有效点，看有效点附近点是否有效
                int availableSize = availableData(i, mDistanceBuf, mAvailableIndexBuf);
                if (availableSize >= 3) {
                    mAvailableSize = availableSize;
                }
            }

        }


        return mAvailableSize;
    }

    private final int RANGE = 220; // 单位 nm

    // 取有效点左右 RANGE的点为 有效范围
    private int availableData(int availableIndex, char[] mDistanceBuf, int[] mAvailableIndexBuf) {

        int availableDistance = mDistanceBuf[availableIndex];
        int minAvailableDistance = availableDistance - RANGE;
        int maxAvailableDistance = availableDistance + RANGE;
        int minIndex = availableIndex;
        int maxIndex = availableIndex;

        int point = 0;
        mAvailableIndexBuf[point] = minIndex;
        point++;

        for (int i = 0; i < 6; i++) {
            minIndex--;
            if (minIndex >= 0) {
                int c = mDistanceBuf[minIndex];
                if (c <= MAX_DISTANCE) {
                    if (c <= availableDistance && c >= minAvailableDistance) {
//                        Tlog.i(TAG_LARG, " available min data :" + c + " index:" + minIndex);
                        mAvailableIndexBuf[point] = minIndex;
                        point++;
                    }
                } else {
//                    Tlog.i(TAG_LARG, " available min data > MAX_DISTANCE");
                }

            }

            maxIndex++;
            if (maxIndex < SIZE) {
                int c = mDistanceBuf[maxIndex];
                if (c <= MAX_DISTANCE) {
                    if (c > availableDistance && c <= maxAvailableDistance) {
//                        Tlog.i(TAG_LARG, " available max data :" + c + " index:" + maxIndex);
                        mAvailableIndexBuf[point] = maxIndex;
                        point++;
                    }
                } else {
//                    Tlog.i(TAG_LARG, " available max data > MAX_DISTANCE");
                }

            }

            if (minIndex < 0 && maxIndex > SIZE) {
                break;
            }

        }

        Tlog.i(TAG_LARG, " available data :" + availableDistance
                + " index:" + availableIndex
                + " size:" + point);

        return point;
    }

    public interface ICalibrationCallBack {

        void onTouchPointInScreen(PointS mPointS);

        void onTouchPointInWall(PointS mPointS);

        void onWallBG(char[] buf);

        void onWallBGDiff(int[] buf, int size);

        void onFps(int fps, int lastFps);

        void onTouchFps(int fps, int lastFps);
    }


    public interface IVertexFinish {

        // 校准单点
        void onCollectPointInWall(int i, PointS mPointS);

        // 校准四点
        void onCollectPointInWall(PointS[] mPointSs);

        void onCollectPointInScreen(PointS[] mPointSs);

        void onVirtualScreen(PointS[] mPointSs);

        void onVirtualScreenRect(PointS[] mPointSs);

        void showTxt(String msg);

    }

    @Override
    public String toString() {
        return "Calibration[hashcode:" + hashCode() + " createTimes:" + createTime + "]";
    }
}
