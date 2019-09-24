package cn.com.startai.radarwall.redview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcelable;
import android.os.Process;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.startai.radarwall.RadarSensor;
import cn.com.swain.baselib.alg.MathUtils;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/7/24
 * desc
 */
public class RedView extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "RedView";

    public RedView(Context context) {
        super(context);
        init();
    }

    public RedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        SurfaceHolder mSurfaceHolder = getHolder();//得到SurfaceHolder对象
        mSurfaceHolder.addCallback(this);//注册SurfaceHolder
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);//保持屏幕长亮
//        setZOrderOnTop(true);
//        setLayerType(LAYER_TYPE_HARDWARE, null);
        mDrawThread = new DrawThread();
        executorService = Executors.newSingleThreadExecutor();
        Tlog.v(TAG, " RedView isHardwareAccelerated():" + isHardwareAccelerated());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDrawThread != null) {
            mDrawThread.move(event);
        }
        return super.onTouchEvent(event);
    }

    private final int RANGE = 60;

    private void touchVertex(float x, float y) {
        if (mIVertexTouchLsn == null) {
            return;
        }
        if (mDrawThread.vertexA.x + RANGE > x
                && mDrawThread.vertexA.x - RANGE < x
                && mDrawThread.vertexA.y + RANGE > y
                && mDrawThread.vertexA.y - RANGE < y
        ) {
            Tlog.e(TAG, " touch A ");
            mIVertexTouchLsn.onClick(1);
        } else if (mDrawThread.vertexB.x + RANGE > x
                && mDrawThread.vertexB.x - RANGE < x
                && mDrawThread.vertexB.y + RANGE > y
                && mDrawThread.vertexB.y - RANGE < y
        ) {
            Tlog.e(TAG, " touch B ");
            mIVertexTouchLsn.onClick(2);
        } else if (mDrawThread.vertexC.x + RANGE > x
                && mDrawThread.vertexC.x - RANGE < x
                && mDrawThread.vertexC.y + RANGE > y
                && mDrawThread.vertexC.y - RANGE < y
        ) {
            Tlog.e(TAG, " touch C ");
            mIVertexTouchLsn.onClick(3);
        } else if (mDrawThread.vertexD.x + RANGE > x
                && mDrawThread.vertexD.x - RANGE < x
                && mDrawThread.vertexD.y + RANGE > y
                && mDrawThread.vertexD.y - RANGE < y
        ) {
            Tlog.e(TAG, " touch D ");
            mIVertexTouchLsn.onClick(4);
        }

    }

    public void setCollectPointInWall(int i, PointS mPointS) {
        if (mDrawThread != null) {
            mDrawThread.setCollectPointInWall(i, mPointS);
        }
    }

    public void setCollectPointSInWall(PointS[] mPoints) {
        if (mDrawThread != null) {
            mDrawThread.setCollectPointSInWall(mPoints);
        }
    }

    public void setPointInScreen(PointS mPointS) {
        if (mDrawThread != null) {
            mDrawThread.setPointInScreen(mPointS);
        }
    }

    public void setCollectPointInScreen(PointS[] mPoints) {
        if (mDrawThread != null) {
            mDrawThread.setCollectPointInScreen(mPoints);
        }
    }

    public void setVirtualScreen(PointS[] mPoints) {
        if (mDrawThread != null) {
            mDrawThread.setVirtualScreen(mPoints);
        }
    }

    public void setVirtualScreenRect(PointS[] mPoints) {
        if (mDrawThread != null) {
            mDrawThread.setVirtualScreenRect(mPoints);
        }
    }

    public void showTxt(String msg) {
        if (mDrawThread != null) {
            mDrawThread.showTxt(msg);
        }
    }

    public void setAlgFps(int fps, int lastFps) {
        if (mDrawThread != null) {
            mDrawThread.setAlgFps(fps, lastFps);
        }
    }

    public void setTouchFps(int fps, int lastFps) {
        if (mDrawThread != null) {
            mDrawThread.setTouchFps(fps, lastFps);
        }
    }

    public interface IVertexTouchLsn {
        void onClick(int i);
    }

    private IVertexTouchLsn mIVertexTouchLsn;

    public void setTouchLsn(IVertexTouchLsn mIVertexTouchLsn) {
        this.mIVertexTouchLsn = mIVertexTouchLsn;
    }

    @Override
    public boolean performClick() {
        Tlog.v(TAG, " RedView performClick ");
        return super.performClick();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Tlog.v(TAG, " onSaveInstanceState ");
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Tlog.v(TAG, " onRestoreInstanceState ");
        super.onRestoreInstanceState(state);
    }

    private DrawThread mDrawThread;
    private ExecutorService executorService;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Tlog.v(TAG, " surfaceCreated ");
        if (mDrawThread == null) {
            mDrawThread = new DrawThread(holder);
        } else {
            mDrawThread.review(holder);
        }
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.execute(mDrawThread);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Tlog.v(TAG, " surfaceChanged width:" + width + " height:" + height + " format:" + format);
        if (mDrawThread != null) {
            mDrawThread.surfaceChanged(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Tlog.v(TAG, " surfaceDestroyed ");
        if (mDrawThread != null) {
            mDrawThread.release();
        }
    }


    public void setVertex(PointS A, PointS B, PointS C, PointS D) {
        if (mDrawThread != null) {
            mDrawThread.setVertex(A, B, C, D);
        }
    }


    public void setPoints(int result, char[] points, boolean clearError) {
        if (mDrawThread != null) {
            mDrawThread.setPoints(result, points, clearError);
        }
    }

    public void setPointInWall(PointS mPointS) {
        if (mDrawThread != null) {
            mDrawThread.setPointInWall(mPointS);
        }
    }

    public void bgDiff(int[] buf, int size) {
        if (mDrawThread != null) {
            mDrawThread.bgDiff(buf, size);
        }
    }

    private static final int MAX_SWEEP_ANGLE = RadarSensor.MAX_SWEEP_ANGLE;
    private static final float[] DEGREE = RadarSensor.DEGREE;

    private static final int MAX_POINT = RadarSensor.FRAME_DATA_SIZE;

    private static final int MAX_WIDTH = RadarSensor.MAX_DISTANCE;
    private static final int MAX_HEIGHT = RadarSensor.MAX_DISTANCE;


    private class DrawThread implements Runnable {

        private SurfaceHolder holder;

        // 320个距离点的画笔
        private Paint mDistancePaint = new Paint();
        // 帧率画笔
        private Paint mFramePaint = new Paint();
        // 帧率画笔
        private Paint mFramePaint2 = new Paint();
        // 错误画笔
        private Paint mErrorPaint = new Paint();
        // xy坐标画笔
        private Paint mCoordPaint = new Paint();
        // 雷达坐标点画笔
        private Paint mCoordPointPaint = new Paint();
        // 清除画笔
        private Paint mClearPaint = new Paint();
        // 画手机触摸点
        private Paint mEventPaint = new Paint();
        // 画手机上的屏幕校准点
        private Paint mPhoneVertexPaint = new Paint();
        // 雷达角度画笔
        private Paint mArcPaint = new Paint();
        // 雷达角度画笔
        private Paint mDegreePaint = new Paint();
        // 在墙上触摸的画笔
        private Paint mTouchInWallPaint = new Paint();
        //背景差值画笔
        private Paint mBgDiffPaint = new Paint();
        // 在墙上触摸点对应在手机上点的画笔
        private Paint mTouchInScreenPaint = new Paint();

        // 手机上的校准点对应在墙上的点的画笔
        private Paint mVertexPaint = new Paint();

        // 墙上虚拟屏幕
        private Paint mVirtualScreenPaintRect = new Paint();
        // 墙上虚拟屏幕
        private Paint mVirtualScreenPaint = new Paint();
        // 屏幕A点再墙上的xy
        private Paint mVirtualScreenA = new Paint();

        // 雷达角度
        private RectF oval = new RectF();
        private volatile boolean run;

        private int VERTEX_RADIUS = 30;

        DrawThread() {
            init();
        }

        DrawThread(SurfaceHolder holder) {
            this.holder = holder;
            this.run = true;
            init();
        }

        void review(SurfaceHolder holder) {
            this.holder = holder;
            this.run = true;
            this.mLastDrawPointS.set(-1, -1);
        }

        void release() {
            run = false;
            holder = null;
            synchronized (syncObj) {
                syncObj.notify();
            }
        }

        private void init() {
            float density = ScreenUtils.getDensity(getContext());
            Tlog.d(TAG, " getDensity: " + density);
            VERTEX_RADIUS = ScreenUtils.dip2px(20, density);
            initPaint();
            surfaceChanged(getWidth(), getHeight());
        }

        // 屏幕宽
        private float width;
        // 屏幕高
        private float height;


        private float radarBottom;
        private float radarTop;

        private float radarWidth;
        private float radarHeight;

        // 距离 高度比例的缩放比
        private float wdr;
        // 距离 宽度比例的缩放比
        private float hdr;

        // x 宽度比例的缩放比
        private float wxr;
        // y 高度比例的缩放比
        private float hyr;

        private float frameX1; // distance fps x
        private float frameX; // canvas fps x
        private float frameY; // distance canvas fps y
        private float frameY2; //alg fps y
        private float frameY3; //touch point fps y
        private float frameY4; //event point fps y

        private final int[] locationOnScreen = new int[2];

        void surfaceChanged(int width, int height) {
            this.width = width;
            this.height = height;
            Tlog.v(TAG, " DrawThread surfaceChanged() width:" + width + " height:" + height);

            float yd;
            if (this.width < this.height) {
                yd = this.height - this.width;
            } else {
                yd = this.height * 0.36f;
            }

            this.radarTop = yd / 4 * 3;
            this.radarWidth = this.width;
            this.radarHeight = this.height - yd;
            this.radarBottom = this.radarTop + this.radarHeight;

            this.wdr = this.radarWidth / MAX_POINT; // x 点位数
            this.hdr = this.radarHeight / MAX_HEIGHT; // y 距离
            Tlog.v(TAG, " DrawThread surfaceChanged() wdr:" + wdr + " hdr:" + hdr);

            this.wxr = this.radarWidth / MAX_WIDTH; // x 正方向距离
            this.hyr = this.radarHeight / MAX_HEIGHT; //y 正方向距离
            Tlog.v(TAG, " DrawThread surfaceChanged() wxr:" + wxr + " hyr:" + hyr);

            this.oval.set(-this.radarWidth, -(this.radarHeight - this.radarTop),
                    this.radarWidth, this.radarBottom);

            setStrokeWidth();

            this.frameX1 = this.width / 10 * 1;
            this.frameX = this.width / 10 * 7;
            this.frameY = this.radarTop / 7;
            this.frameY2 = this.frameY + txtSize;
            this.frameY3 = this.frameY2 + txtSize;
            this.frameY4 = this.frameY3 + txtSize;


            int[] locationInWindow = ScreenUtils.getLocationInWindow(RedView.this);
            Tlog.v(TAG, " window :: x:" + locationInWindow[0] + " y:" + locationInWindow[1]);

            ScreenUtils.getLocationOnScreen(RedView.this, locationOnScreen);
            Tlog.v(TAG, " screen :: x:" + locationOnScreen[0] + " y:" + locationOnScreen[1]);

            vertexA.set(A.x - locationOnScreen[0], A.y - locationOnScreen[1]);
            vertexB.set(B.x - locationOnScreen[0], B.y - locationOnScreen[1]);
            vertexC.set(C.x - locationOnScreen[0], C.y - locationOnScreen[1]);
            vertexD.set(D.x - locationOnScreen[0], D.y - locationOnScreen[1]);

            touchPointSInScreen.x = touchPointSInScreenOriginal.x - locationOnScreen[0];
            touchPointSInScreen.y = touchPointSInScreenOriginal.y - locationOnScreen[1];

            collectInScreen[0].x = collectInScreenOriginal[0].x - locationOnScreen[0];
            collectInScreen[0].y = collectInScreenOriginal[0].y - locationOnScreen[1];

            collectInScreen[1].x = collectInScreenOriginal[1].x - locationOnScreen[0];
            collectInScreen[1].y = collectInScreenOriginal[1].y - locationOnScreen[1];

            collectInScreen[2].x = collectInScreenOriginal[2].x - locationOnScreen[0];
            collectInScreen[2].y = collectInScreenOriginal[2].y - locationOnScreen[1];

            collectInScreen[3].x = collectInScreenOriginal[3].x - locationOnScreen[0];
            collectInScreen[3].y = collectInScreenOriginal[3].y - locationOnScreen[1];

        }

        private final float NOT_DRAW_XY = -100;

        private final Object syncObj = new byte[1];

        // 原始距离点
        private char[] distance = new char[MAX_POINT];
        // 距离 个数 坐标点
        private float[] drawPoints = new float[MAX_POINT * 2];
        // xy 坐标点
        private float[] coordPoints = new float[MAX_POINT * 2];
        private int result = RadarSensor.RC_OK;
        // 成功请求的次数
        private int successReqFrame = 0;
        // 请求的总次数
        private int totalReqFrame = 0;

        private int drawErrorTimes = 0;
        // 画的总次数
        private int drawFrame = 0;

        private int algFps = 0;
        private int lastAlgFps = 0;

        void setAlgFps(int fps, int lastFps) {
            this.algFps = fps;
            this.lastAlgFps = lastFps;
        }

        private int touchFps = 0;
        private int lastTouchFps = 0;

        void setTouchFps(int fps, int lastFps) {
            this.touchFps = fps;
            this.lastTouchFps = lastFps;
        }


        private int fps = 0;
        private int lastFps = 0;
        private long lastFpsTime = 0;

        void setPoints(int result, char[] points, boolean clearError) {

            long l = System.currentTimeMillis();
            if (Math.abs(lastFpsTime - l) >= 1000) {
                lastFps = fps;
                fps = 0;
                lastFpsTime = l;
            }

            if (result == RadarSensor.RC_OK) {
                this.distance = points;
                this.calculation = false; // 收到一包数据，重置计算
                this.successReqFrame++;
                this.fps++;
                // 保留错误，多绘制几次
                if (clearError) {
                    this.result = result;
                } else if (++this.drawErrorTimes >= 70) {
                    this.result = result;
                }
            } else {
                this.drawErrorTimes = 0;
                this.result = result;
            }
            this.totalReqFrame++;

            synchronized (syncObj) {
                syncObj.notify();
            }

        }

        // 下面方法计算过
        private boolean calculation = false;

        private void calDrawPoint() {
            calculation = true;

            int xyDistance;
            int realDistance;
            for (int i = 0; i < MAX_POINT; i++) {
                realDistance = (int) distance[i];

//                            Tlog.v(TAG, " index(" + i + ") = " + point);

                if (realDistance > MAX_HEIGHT) {
                    xyDistance = 0;
                    realDistance = MAX_HEIGHT - 10; // 无效点减10,画在边的上方
                } else {
                    xyDistance = realDistance;
                }

                coordPoints[i * 2] = (float)
                        (xyDistance * MathUtils.sin(RadarSensor.DEGREE[i])
                                * wxr);
                coordPoints[i * 2 + 1] = (float)
                        (xyDistance * MathUtils.cos(RadarSensor.DEGREE[i])
                                * hyr)
                        + radarTop;

//                Tlog.v(TAG, " index(" + i + ") = " + realDistance
//                        + " x:" + coordPoints[i * 2]
//                        + " y:" + coordPoints[i * 2 + 1]);

                drawPoints[i * 2] = i * wdr;
                drawPoints[i * 2 + 1] = realDistance * hdr + radarTop;

            }

        }

        private final PointS touchPointSInWall = new PointS();

        void setPointInWall(PointS mPointS) {
            touchPointSInWall.x = mPointS.x * wxr;
            touchPointSInWall.y = mPointS.y * hyr + radarTop;
            synchronized (syncObj) {
                syncObj.notify();
            }
        }

        private final PointS touchPointSInScreen = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);
        private final PointS touchPointSInScreenOriginal = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);

        void setPointInScreen(PointS mPointS) {
            touchPointSInScreenOriginal.set(mPointS);
            touchPointSInScreen.x = mPointS.x - locationOnScreen[0];
            touchPointSInScreen.y = mPointS.y - locationOnScreen[1];
            synchronized (syncObj) {
                syncObj.notify();
            }
        }


        private int[] bgDiffBuf = new int[MAX_POINT];
        private float[] drawBgDiffPoints = new float[MAX_POINT * 2];

        void bgDiff(int[] buf, int size) {
            bgDiffBuf = buf;
            for (int i = 0; i < size; i++) {
                int diffDistance = bgDiffBuf[i];
                drawBgDiffPoints[i * 2] = i * wdr;
                drawBgDiffPoints[i * 2 + 1] = diffDistance * hdr + radarTop;
            }
            synchronized (syncObj) {
                syncObj.notify();
            }
        }

        void move(MotionEvent event) {
            String emsg = "x:" + event.getX() + " y:" + event.getY() + " getAction:" + event.getAction();
            Tlog.v(TAG, emsg);
            msg = emsg;
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // 如果点击
                    performClick();
                    break;
                case MotionEvent.ACTION_UP:
//                touchVertex(x, y);
                    move(0, 0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    move(event.getX(), event.getY());
                    break;
            }
            countTouchTimes();
        }

        private long lastEventTs;
        private int eventFps;
        private int lastEventFps;

        private void countTouchTimes() {
            eventFps++;
            long l = System.currentTimeMillis();
            if (l - lastEventTs >= 1000) {
                lastEventTs = l;
                lastEventFps = eventFps;
                eventFps = 1;
            }
        }

        // 手指移动的x
        // 手指移动的y
        private PointS mMovePointS = new PointS(0, 0);
        private PointS mLastDrawPointS = new PointS(-1, -1);

        void move(float x, float y) {
            this.mMovePointS.set(x, y);
            if (mLastDrawPointS.x != mMovePointS.x
                    || mLastDrawPointS.y != mMovePointS.y) { // 单点不要重复绘制
                synchronized (syncObj) {
                    syncObj.notify();
                }
            }
        }

        private PointS A = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);
        private PointS B = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);
        private PointS C = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);
        private PointS D = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);
        private PointS vertexA = new PointS();
        private PointS vertexB = new PointS();
        private PointS vertexC = new PointS();
        private PointS vertexD = new PointS();

        // 校准点 在手机屏幕上的坐标
        private void setVertex(PointS a, PointS b, PointS c, PointS d) {
            this.A.set(a);
            this.B.set(b);
            this.C.set(c);
            this.D.set(d);
            this.vertexA.set(A.x - locationOnScreen[0], A.y - locationOnScreen[1]);
            this.vertexB.set(B.x - locationOnScreen[0], B.y - locationOnScreen[1]);
            this.vertexC.set(C.x - locationOnScreen[0], C.y - locationOnScreen[1]);
            this.vertexD.set(D.x - locationOnScreen[0], D.y - locationOnScreen[1]);
        }

        private float[] collectVertexXY = new float[]
                {
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY
                };


        //校准点 在墙面的点
        void setCollectPointInWall(int i, PointS mPointS) {
            collectVertexXY[(i % 4) * 2] = mPointS.x * wxr;
            collectVertexXY[(i % 4) * 2 + 1] = mPointS.y * hyr + radarTop;
        }

        void setCollectPointSInWall(PointS[] mPoints) {
            if (mPoints == null || mPoints.length <= 0) {
                return;
            }
            for (int i = 0; i < mPoints.length; i++) {
                setCollectPointInWall(i, mPoints[i]);
            }
        }

        private float[] virtualScreenRect = new float[]
                {
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY
                };

        void setVirtualScreenRect(PointS[] mPoints) {
            if (mPoints == null || mPoints.length < 4) {
                return;
            }
            for (int i = 0; i < 4; i++) {
                virtualScreenRect[(i % 4) * 2] = mPoints[i].x * wxr;
                virtualScreenRect[(i % 4) * 2 + 1] = mPoints[i].y * hyr + radarTop;
            }
        }

        private float[] virtualScreen = new float[]
                {
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY
                };

        //校准点 在墙面的点
        void setVirtualScreen(PointS[] mPoints) {
            if (mPoints == null || mPoints.length < 4) {
                return;
            }
            for (int i = 0; i < 4; i++) {
                virtualScreen[(i % 4) * 2] = mPoints[i].x * wxr;
                virtualScreen[(i % 4) * 2 + 1] = mPoints[i].y * hyr + radarTop;
            }
        }

        // 墙上较准点在屏幕上
        private final PointS[] collectInScreen = new PointS[]
                {
                        new PointS(NOT_DRAW_XY, NOT_DRAW_XY), new PointS(NOT_DRAW_XY, NOT_DRAW_XY),
                        new PointS(NOT_DRAW_XY, NOT_DRAW_XY), new PointS(NOT_DRAW_XY, NOT_DRAW_XY)
                };


        // 墙上较准点在屏幕上
        private final PointS[] collectInScreenOriginal = new PointS[]
                {
                        new PointS(NOT_DRAW_XY, NOT_DRAW_XY), new PointS(NOT_DRAW_XY, NOT_DRAW_XY),
                        new PointS(NOT_DRAW_XY, NOT_DRAW_XY), new PointS(NOT_DRAW_XY, NOT_DRAW_XY)
                };

        void setCollectPointInScreen(PointS[] mPoints) {
            if (mPoints != null && mPoints.length >= 4) {
                collectInScreen[0].x = mPoints[0].x - locationOnScreen[0];
                collectInScreen[0].y = mPoints[0].y - locationOnScreen[1];

                collectInScreen[1].x = mPoints[1].x - locationOnScreen[0];
                collectInScreen[1].y = mPoints[1].y - locationOnScreen[1];

                collectInScreen[2].x = mPoints[2].x - locationOnScreen[0];
                collectInScreen[2].y = mPoints[2].y - locationOnScreen[1];

                collectInScreen[3].x = mPoints[3].x - locationOnScreen[0];
                collectInScreen[3].y = mPoints[3].y - locationOnScreen[1];

                collectInScreenOriginal[0].set(mPoints[0]);
                collectInScreenOriginal[1].set(mPoints[1]);
                collectInScreenOriginal[2].set(mPoints[2]);
                collectInScreenOriginal[3].set(mPoints[3]);
            }
        }

        String msg;

        void showTxt(String msg) {
            this.msg = msg;
        }

        private final PorterDuffXfermode porterDuffXfermodeClear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        private final PorterDuffXfermode porterDuffXfermodeSrc = new PorterDuffXfermode(PorterDuff.Mode.SRC);

        // 清画布
        private void clearCanvas(Canvas canvas) {
            canvas.drawColor(Color.TRANSPARENT);
            mClearPaint.setXfermode(porterDuffXfermodeClear);
            canvas.drawPaint(mClearPaint);
            mClearPaint.setXfermode(porterDuffXfermodeSrc);
        }

        @Override
        public void run() {

            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            long startDrawTime = System.currentTimeMillis();

            while (run) {

                Tlog.v(TAG, " draw use time: " + (System.currentTimeMillis() - startDrawTime));

                if ((mLastDrawPointS.x == mMovePointS.x)
                        && (mLastDrawPointS.y == mMovePointS.y)) { // 防止滑动事件没有绘制
                    synchronized (syncObj) {
                        Tlog.d(TAG, " syncObj.wait() ");
                        try {
                            syncObj.wait();
                        } catch (InterruptedException e) {
                            Tlog.e(TAG, " wait() InterruptedException ", e);
                        }
                    }
                } else {
                    Tlog.d(TAG, " skip syncObj.wait() ");
                }


                startDrawTime = System.currentTimeMillis();

                Canvas canvas = null;
                try {
                    if (holder != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            canvas = holder.lockHardwareCanvas();
                        } else {
                            canvas = holder.lockCanvas();
                        }
                    }

                    if (canvas == null) {
                        Tlog.e(TAG, " lockCanvas() canvas == null ");
                        mLastDrawPointS.set(mMovePointS);
                        continue;
                    }

//                    Tlog.v(TAG, " canvas isHardwareAccelerated() : " + canvas.isHardwareAccelerated());
//                    canvas.save();

                    clearCanvas(canvas);

                    // 画雷达角度
                    canvas.drawArc(oval, (360 - (MAX_SWEEP_ANGLE - 90)), MAX_SWEEP_ANGLE,
                            true, mArcPaint);
//                    for (int i = 0; i < DEGREE.length; i++) {
//                        // 画雷达角度
//                        canvas.drawArc(oval, (360 - (MAX_SWEEP_ANGLE - 90)) + DEGREE[i], MAX_SWEEP_ANGLE,
//                                true, mDegreePaint);
//                    }

                    // 画雷达xy
                    canvas.drawLine(0, radarTop, radarWidth, radarTop, mCoordPaint);
                    canvas.drawLine(0, radarBottom, radarWidth, radarBottom, mCoordPaint);

                    // 画屏幕底部
                    canvas.drawLine(0, height, width, height, mCoordPaint);

                    // 画帧率
                    canvas.drawText("F:" + successReqFrame + "/" + totalReqFrame + "--" + ++drawFrame,
                            frameX, frameY,
                            mFramePaint);
                    canvas.drawText("D-FPS1:" + lastFps + "--FPS2:" + fps, frameX1, frameY, mFramePaint2);
                    canvas.drawText("A-FPS1:" + lastAlgFps + "--FPS2:" + algFps, frameX1, frameY2, mFramePaint2);
                    canvas.drawText("P-FPS1:" + lastTouchFps + "--FPS2:" + touchFps, frameX1, frameY3, mFramePaint2);
                    if (Math.abs(startDrawTime - lastEventTs) > 3500) { // 防止没有event还绘制
                        lastEventFps = 0;
                        eventFps = 0;
                        lastEventTs = startDrawTime;
                    }
                    canvas.drawText("E-FPS1:" + lastEventFps + "--FPS2:" + eventFps, frameX1, frameY4, mFramePaint2);

                    // 画错误码
                    if (result != RadarSensor.RC_OK) {
                        canvas.drawText("ERROR::" + result, width / 2, radarTop / 2, mErrorPaint);
                    } else {

                        if (Math.abs(startDrawTime - lastSelectTime) > 1500) {
                            mVirtualScreenPaint.setColor(virtualScreenColors[++virtualScreenSelect % virtualScreenColors.length]);
                            lastSelectTime = startDrawTime;
                        }

                        if (msg != null) {
                            canvas.drawText(msg, width / 2, radarTop / 2, mErrorPaint);
                        }
                    }

                    // 计算雷达数据的xy
                    if (!calculation) {
                        calDrawPoint();
                    }
                    // 画雷达距离
                    canvas.drawPoints(drawPoints, mDistancePaint);
                    // 画雷达xy
                    canvas.drawPoints(coordPoints, mCoordPointPaint);
                    // 画墙上的校准点
                    canvas.drawPoints(collectVertexXY, mVertexPaint);

                    // 画手机上的屏幕校准点
                    canvas.drawCircle(vertexA.x, vertexA.y, VERTEX_RADIUS, mPhoneVertexPaint);
                    canvas.drawCircle(vertexB.x, vertexB.y, VERTEX_RADIUS, mPhoneVertexPaint);
                    canvas.drawCircle(vertexC.x, vertexC.y, VERTEX_RADIUS, mPhoneVertexPaint);
                    canvas.drawCircle(vertexD.x, vertexD.y, VERTEX_RADIUS, mPhoneVertexPaint);


                    canvas.drawPoint(virtualScreen[0], virtualScreen[1], mVirtualScreenA);

                    // 画墙上虚拟屏
                    canvas.drawLine(virtualScreen[0], virtualScreen[1], virtualScreen[2], virtualScreen[3], mVirtualScreenPaint);
                    canvas.drawLine(virtualScreen[2], virtualScreen[3], virtualScreen[4], virtualScreen[5], mVirtualScreenPaint);
                    canvas.drawLine(virtualScreen[4], virtualScreen[5], virtualScreen[6], virtualScreen[7], mVirtualScreenPaint);
                    canvas.drawLine(virtualScreen[6], virtualScreen[7], virtualScreen[0], virtualScreen[1], mVirtualScreenPaint);

                    canvas.drawLine(virtualScreenRect[0], virtualScreenRect[1], virtualScreenRect[2], virtualScreenRect[3], mVirtualScreenPaintRect);
                    canvas.drawLine(virtualScreenRect[2], virtualScreenRect[3], virtualScreenRect[4], virtualScreenRect[5], mVirtualScreenPaintRect);
                    canvas.drawLine(virtualScreenRect[4], virtualScreenRect[5], virtualScreenRect[6], virtualScreenRect[7], mVirtualScreenPaintRect);
                    canvas.drawLine(virtualScreenRect[6], virtualScreenRect[7], virtualScreenRect[0], virtualScreenRect[1], mVirtualScreenPaintRect);

//                    canvas.drawRect(virtualScreenRect[0], virtualScreenRect[1],
//                            virtualScreenRect[4], virtualScreenRect[5], mVirtualScreenPaintRect);

                    // 画校准算法计算的 手机屏幕上的校准点
                    canvas.drawCircle(collectInScreen[0].x, collectInScreen[0].y, VERTEX_RADIUS, mTouchInScreenPaint);
                    canvas.drawCircle(collectInScreen[1].x, collectInScreen[1].y, VERTEX_RADIUS, mTouchInScreenPaint);
                    canvas.drawCircle(collectInScreen[2].x, collectInScreen[2].y, VERTEX_RADIUS, mTouchInScreenPaint);
                    canvas.drawCircle(collectInScreen[3].x, collectInScreen[3].y, VERTEX_RADIUS, mTouchInScreenPaint);

                    // 画墙面触控点在手机屏幕上对应的点
                    canvas.drawCircle(touchPointSInScreen.x, touchPointSInScreen.y, VERTEX_RADIUS, mTouchInScreenPaint);

                    // 画墙面触摸点
                    canvas.drawPoint(touchPointSInWall.x, touchPointSInWall.y, mTouchInWallPaint);

                    // 画 测试数据 减去  雷达背景数据 的差值
                    canvas.drawPoints(drawBgDiffPoints, mBgDiffPaint);

                    // 画手机屏幕触摸点
                    mLastDrawPointS.set(mMovePointS.x, mMovePointS.y);
                    if (mLastDrawPointS.x != 0 && mLastDrawPointS.y != 0) {
                        canvas.drawCircle(mLastDrawPointS.x, mLastDrawPointS.y, VERTEX_RADIUS, mEventPaint);
                    }

//                    canvas.restore();

                } catch (Exception e) {
                    Tlog.e(TAG, " DrawThread lockCanvas ", e);
                    run = false;
                } finally {
                    if (canvas != null) {
                        try {
                            holder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            Tlog.e(TAG, " DrawThread unlockCanvasAndPost ", e);
                        }
                    }
                }

            }
            Tlog.e(TAG, " DrawThread run finish ");
        }

        int virtualScreenSelect = 0;
        long lastSelectTime = 0;

        final int[] virtualScreenColors = new int[]{
                Color.parseColor("#990033"), Color.parseColor("#CC6699"), Color.parseColor("#FF6699"),
                Color.parseColor("#FF3366"), Color.parseColor("#993366"), Color.parseColor("#CC0066"),
                Color.parseColor("#CC0033"), Color.parseColor("#FF0066"), Color.parseColor("#FF99CC"),
                Color.parseColor("#FF0099"), Color.parseColor("#CC3366"), Color.parseColor("#FF66CC"),
                Color.parseColor("#FF33CC"), Color.parseColor("#FFCCFF"), Color.parseColor("#FF99FF"),
                Color.parseColor("#FF66FF"), Color.parseColor("#CC33CC"), Color.parseColor("#CC00FF"),
                Color.parseColor("#FF33FF"), Color.parseColor("#CC99FF"), Color.parseColor("#9900CC"),
                Color.parseColor("#FF00FF"), Color.parseColor("#CC66FF"), Color.parseColor("#CC6699")};

        private final int txtSize = 16 * 2;

        private void initPaint() {

            mDistancePaint.setTextAlign(Paint.Align.CENTER);
            mDistancePaint.setStyle(Paint.Style.FILL);
            mDistancePaint.setColor(Color.RED);
            mDistancePaint.setAntiAlias(true);

            mCoordPointPaint.setTextAlign(Paint.Align.CENTER);
            mCoordPointPaint.setStyle(Paint.Style.FILL);
            mCoordPointPaint.setColor(Color.YELLOW);
            mCoordPointPaint.setAntiAlias(true);


            mCoordPaint.setStyle(Paint.Style.FILL);
            mCoordPaint.setColor(Color.WHITE);
            mCoordPaint.setAntiAlias(true);
            mCoordPaint.setTextAlign(Paint.Align.CENTER);
            mCoordPaint.setFakeBoldText(true);

            mTouchInWallPaint.setStyle(Paint.Style.FILL);
            mTouchInWallPaint.setColor(Color.GREEN);
            mTouchInWallPaint.setAntiAlias(true);
            mTouchInWallPaint.setTextAlign(Paint.Align.CENTER);
            mTouchInWallPaint.setFakeBoldText(true);

            mEventPaint.setStyle(Paint.Style.FILL);
            mEventPaint.setColor(Color.RED);
            mEventPaint.setAntiAlias(true);
            mEventPaint.setTextAlign(Paint.Align.CENTER);
            mEventPaint.setFakeBoldText(true);

            mTouchInScreenPaint.setStyle(Paint.Style.FILL);
            mTouchInScreenPaint.setColor(Color.GREEN);
            mTouchInScreenPaint.setAntiAlias(true);
            mTouchInScreenPaint.setTextAlign(Paint.Align.CENTER);
            mTouchInScreenPaint.setFakeBoldText(true);

            mVirtualScreenA.setStyle(Paint.Style.STROKE);
            mVirtualScreenA.setColor(Color.YELLOW);
            mVirtualScreenA.setAntiAlias(true);
            mVirtualScreenA.setTextAlign(Paint.Align.CENTER);
            mVirtualScreenA.setFakeBoldText(true);
            mVirtualScreenA.setAlpha(255);

//            #990033	#CC6699	#FF6699	#FF3366	#993366	#CC0066	#CC0033	#FF0066	#FF0033
//            #FF3399	#FF9999	#FF99CC	#FF0099	#CC3366	#FF66CC	#FF33CC	#FFCCFF	#FF99FF
//            #FF66FF	#CC33CC	#CC00FF	#FF33FF	#CC99FF	#9900CC	#FF00FF	#CC66FF	#990099

            mVirtualScreenPaint.setStyle(Paint.Style.STROKE);
            mVirtualScreenPaint.setColor(virtualScreenColors[virtualScreenSelect]);
            mVirtualScreenPaint.setAntiAlias(true);
            mVirtualScreenPaint.setTextAlign(Paint.Align.CENTER);
            mVirtualScreenPaint.setFakeBoldText(true);
            mVirtualScreenPaint.setAlpha(255);

            mVirtualScreenPaintRect.setStyle(Paint.Style.STROKE);
            mVirtualScreenPaintRect.setColor(Color.parseColor("#FFF0F5"));
            mVirtualScreenPaintRect.setAntiAlias(true);
            mVirtualScreenPaintRect.setTextAlign(Paint.Align.CENTER);
            mVirtualScreenPaintRect.setFakeBoldText(true);
            mVirtualScreenPaintRect.setAlpha(255);

            mVertexPaint.setStyle(Paint.Style.FILL);
            mVertexPaint.setColor(Color.parseColor("#00FF7F"));
            mVertexPaint.setAntiAlias(true);
            mVertexPaint.setTextAlign(Paint.Align.CENTER);
            mVertexPaint.setFakeBoldText(true);

            mBgDiffPaint.setStyle(Paint.Style.FILL);
            mBgDiffPaint.setColor(Color.parseColor("#DA70D6"));
            mBgDiffPaint.setAntiAlias(true);
            mBgDiffPaint.setTextAlign(Paint.Align.CENTER);
            mBgDiffPaint.setFakeBoldText(true);

            mArcPaint.setStyle(Paint.Style.STROKE);
            mArcPaint.setColor(Color.WHITE);
            mArcPaint.setAntiAlias(true);
            mArcPaint.setTextAlign(Paint.Align.CENTER);
            mArcPaint.setFakeBoldText(true);

            mDegreePaint.setStyle(Paint.Style.STROKE);
            mDegreePaint.setColor(Color.parseColor("#AFEEEE"));
            mDegreePaint.setAntiAlias(true);
            mDegreePaint.setTextAlign(Paint.Align.CENTER);
            mDegreePaint.setFakeBoldText(true);
            mDegreePaint.setAlpha(255 / 2);

            mErrorPaint.setStyle(Paint.Style.FILL);
            mErrorPaint.setColor(Color.RED);
            mErrorPaint.setAntiAlias(true);
            mErrorPaint.setStrokeWidth(6);
            mErrorPaint.setTextAlign(Paint.Align.CENTER);
            mErrorPaint.setFakeBoldText(true);
            mErrorPaint.setTextSize(txtSize);

            mFramePaint.setStyle(Paint.Style.FILL);
            mFramePaint.setColor(Color.WHITE);
            mFramePaint.setAntiAlias(true);
            mFramePaint.setStrokeWidth(6);
            mFramePaint.setTextAlign(Paint.Align.CENTER);
            mFramePaint.setFakeBoldText(true);
            mFramePaint.setTextSize(txtSize);

            mFramePaint2.setStyle(Paint.Style.FILL);
            mFramePaint2.setColor(Color.WHITE);
            mFramePaint2.setAntiAlias(true);
            mFramePaint2.setStrokeWidth(6);
            mFramePaint2.setTextAlign(Paint.Align.LEFT);
            mFramePaint2.setFakeBoldText(true);
            mFramePaint2.setTextSize(txtSize);

            mPhoneVertexPaint.setStyle(Paint.Style.FILL);
            mPhoneVertexPaint.setColor(Color.WHITE);
            mPhoneVertexPaint.setAntiAlias(true);
            mPhoneVertexPaint.setStrokeWidth(7);
            mPhoneVertexPaint.setTextAlign(Paint.Align.CENTER);
            mPhoneVertexPaint.setFakeBoldText(true);
            mPhoneVertexPaint.setTextSize(txtSize);
        }

        private void setStrokeWidth() {
            this.mDistancePaint.setStrokeWidth(wdr);
            this.mCoordPointPaint.setStrokeWidth(wdr);
            this.mTouchInWallPaint.setStrokeWidth(wdr * 1.5f);
            this.mVertexPaint.setStrokeWidth(wdr * 2);
            this.mCoordPaint.setStrokeWidth(wdr / 2);
            this.mArcPaint.setStrokeWidth(wdr / 2);
            this.mBgDiffPaint.setStrokeWidth(wdr);
            this.mTouchInScreenPaint.setStrokeWidth(wdr);
            this.mEventPaint.setStrokeWidth(wdr);
            this.mVirtualScreenPaint.setStrokeWidth(wdr / 1.5f);
            this.mVirtualScreenPaintRect.setStrokeWidth(wdr / 2);
            this.mVirtualScreenA.setStrokeWidth(wdr * 2);
        }

    }
}
