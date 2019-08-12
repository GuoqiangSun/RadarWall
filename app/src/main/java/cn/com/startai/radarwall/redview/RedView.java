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

import cn.com.startai.radarwall.MainActivity;
import cn.com.swain.baselib.display.MathUtils;
import cn.com.swain.baselib.display.PointS;
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

        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        Tlog.v(TAG, " RedView onTouchEvent x:" + x + " y:" + y + " action:" + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 如果点击
                performClick();
                break;
            case MotionEvent.ACTION_UP:
                touchVertex(x, y);
                if (mDrawThread != null) {
                    mDrawThread.move(0, 0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDrawThread != null) {
                    mDrawThread.move(x, y);
                }
                break;
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

    public void setCollectPointInWall(int i, PointS mPointFSerial) {
        if (mDrawThread != null) {
            mDrawThread.setCollectPointInWall(i, mPointFSerial);
        }
    }

    public void setPointInScreen(PointS mPointFSerial) {
        if (mDrawThread != null) {
            mDrawThread.setPointInScreen(mPointFSerial);
        }
    }

    public void setCollectPointInScreen(PointS[] mPointFSerial) {
        if (mDrawThread != null) {
            mDrawThread.setCollectPointInScreen(mPointFSerial);
        }
    }

    public void setVirtualScreen(PointS[] mPointFSerials) {
        if (mDrawThread != null) {
            mDrawThread.setVirtualScreen(mPointFSerials);
        }
    }

    public void setVirtualScreenRect(PointS[] mPointFSerials) {
        if (mDrawThread != null) {
            mDrawThread.setVirtualScreenRect(mPointFSerials);
        }
    }

    public void showTxt(String msg) {
        if (mDrawThread != null) {
            mDrawThread.showTxt(msg);
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

    public void setPointInWall(PointS PointFSerial) {
        if (mDrawThread != null) {
            mDrawThread.setPointInWall(PointFSerial);
        }
    }

    public void bgDiff(int[] buf, int size) {
        if (mDrawThread != null) {
            mDrawThread.bgDiff(buf, size);
        }
    }

    private static final int MAX_SWEEP_ANGLE = MainActivity.MAX_SWEEP_ANGLE;

    private static final int MAX_POINT = MainActivity.FRAME_DATA_SIZE;

    private static final int MAX_WIDTH = MainActivity.MAX_DISTANCE;
    private static final int MAX_HEIGHT = MainActivity.MAX_DISTANCE;


    private class DrawThread implements Runnable {

        private SurfaceHolder holder;

        // 320个距离点的画笔
        private Paint mDistancePaint = new Paint();
        // 帧率画笔
        private Paint mFramePaint = new Paint();
        // 错误画笔
        private Paint mErrorPaint = new Paint();
        // xy坐标画笔
        private Paint mCoordPaint = new Paint();
        // 雷达坐标点画笔
        private Paint mCoordPointPaint = new Paint();
        // 清除画笔
        private Paint mClearPaint = new Paint();
        // 手指在屏幕上移动的画笔
        private Paint mMovePaint = new Paint();
        // 雷达角度画笔
        private Paint mArcPaint = new Paint();
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
        private boolean run;

        private int VERTEX_RADIUS = 30;

        DrawThread() {
            init();
        }

        DrawThread(SurfaceHolder holder) {
            this.holder = holder;
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
            this.run = true;
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

        private float frameX;
        private float frameY;

        private final int[] locationOnScreen = new int[4];

        void surfaceChanged(int width, int height) {
            this.width = width;
            this.height = height;

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

            this.wdr = this.radarWidth / MAX_POINT;
            this.hdr = this.radarHeight / MAX_HEIGHT;

            this.oval.set(-this.radarWidth, -(this.radarHeight - this.radarTop),
                    this.radarWidth, this.radarBottom);

            this.mDistancePaint.setStrokeWidth(wdr);
            this.mCoordPointPaint.setStrokeWidth(wdr);
            this.mTouchInWallPaint.setStrokeWidth(wdr * 1.5f);
            this.mVertexPaint.setStrokeWidth(wdr * 2);
            this.mCoordPaint.setStrokeWidth(wdr / 2);
            this.mArcPaint.setStrokeWidth(wdr / 2);
            this.mBgDiffPaint.setStrokeWidth(wdr);
            this.mTouchInScreenPaint.setStrokeWidth(wdr);
            this.mVirtualScreenPaint.setStrokeWidth(wdr / 1.5f);
            this.mVirtualScreenPaintRect.setStrokeWidth(wdr / 2);
            this.mVirtualScreenA.setStrokeWidth(wdr * 2);

            this.wxr = this.radarWidth / MAX_WIDTH;
            this.hyr = this.radarHeight / MAX_HEIGHT;

            this.frameX = this.width / 10 * 7;
            this.frameY = this.radarTop / 4;
            Tlog.v(TAG, " DrawThread surfaceChanged() width:" + width + " height:" + height);
            Tlog.v(TAG, " DrawThread surfaceChanged() wdr:" + wdr + " hdr:" + hdr);
            Tlog.v(TAG, " DrawThread surfaceChanged() wxr:" + wxr + " hyr:" + hyr);


            ScreenUtils.getLocationOnScreen(RedView.this, locationOnScreen);
            Tlog.v(TAG, " screen :: x:" + locationOnScreen[0] + " y:" + locationOnScreen[1]
                    + " w:" + locationOnScreen[2] + " h:" + locationOnScreen[3]);

            vertexA.set(A.x - locationOnScreen[0], A.y - locationOnScreen[1]);
            vertexB.set(B.x - locationOnScreen[0], B.y - locationOnScreen[1]);
            vertexC.set(C.x - locationOnScreen[0], C.y - locationOnScreen[1]);
            vertexD.set(D.x - locationOnScreen[0], D.y - locationOnScreen[1]);
            Tlog.v(TAG, " vertexA::" + vertexA.toString());
            Tlog.v(TAG, " vertexB::" + vertexB.toString());
            Tlog.v(TAG, " vertexC::" + vertexC.toString());
            Tlog.v(TAG, " vertexD::" + vertexD.toString());

            int[] locationInWindow = ScreenUtils.getLocationInWindow(RedView.this);
            Tlog.v(TAG, " window :: x:" + locationInWindow[0] + " y:" + locationInWindow[1]
                    + " w:" + locationInWindow[2] + " h:" + locationInWindow[3]);

        }

        private final float NOT_DRAW_XY = -100;

        private final Object syncObj = new byte[1];

        // 原始距离点
        private char[] distance = new char[MAX_POINT];
        // 距离 个数 坐标点
        private float[] drawPoints = new float[MAX_POINT * 2];
        // xy 坐标点
        private float[] coordPoints = new float[MAX_POINT * 2];
        private int result = MainActivity.RC_OK;
        // 成功请求的次数
        private int successReqFrame = 0;
        // 请求的总次数
        private int totalReqFrame = 0;

        private int drawErrorTimes = 0;
        // 画的总次数
        private int drawFrame = 0;

        void setPoints(int result, char[] points, boolean clearError) {
            if (result == MainActivity.RC_OK) {
                this.distance = points;
                this.calculation = false;
                this.successReqFrame++;
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
                        (xyDistance * MathUtils.sin(MainActivity.DEGREE[i])
                                * wxr);
                coordPoints[i * 2 + 1] = (float)
                        (xyDistance * MathUtils.cos(MainActivity.DEGREE[i])
                                * hyr)
                        + radarTop;

//                Tlog.v(TAG, " index(" + i + ") = " + realDistance
//                        + " x:" + coordPoints[i * 2]
//                        + " y:" + coordPoints[i * 2 + 1]);

//                            canvas.drawPoint(i * strong, height - (MAX_HEIGHT - point) * wf, mDistancePaint);
//                            canvas.drawPoint(i * pointStrong, height - point * pointWeight, mDistancePaint);
                drawPoints[i * 2] = i * wdr;
                drawPoints[i * 2 + 1] = realDistance * hdr + radarTop;

            }

        }

        private final PointS touchPointSInWall = new PointS();

        private void setPointInWall(PointS PointFSerial) {
            touchPointSInWall.x = PointFSerial.x * wxr;
            touchPointSInWall.y = PointFSerial.y * hyr + radarTop;
            synchronized (syncObj) {
                syncObj.notify();
            }
        }

        private final PointS touchPointSInScreen = new PointS(NOT_DRAW_XY, NOT_DRAW_XY);

        private void setPointInScreen(PointS mPointFSerial) {
            touchPointSInScreen.x = mPointFSerial.x - locationOnScreen[0];
            touchPointSInScreen.y = mPointFSerial.y - locationOnScreen[1];
            synchronized (syncObj) {
                syncObj.notify();
            }
        }


        private int[] bgDiffBuf = new int[MAX_POINT];
        private float[] drawBgDiffPoints = new float[MAX_POINT * 2];

        private void bgDiff(int[] buf, int size) {
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


        // 手指移动的x
        // 手指移动的y
        private PointS mMovePointS = new PointS(0, 0);
        private PointS mLastDrawPointS = new PointS(-1, -1);

        private void move(float x, float y) {
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
        private void setCollectPointInWall(int i, PointS mPointFSerial) {
            collectVertexXY[(i % 4) * 2] = mPointFSerial.x * wxr;
            collectVertexXY[(i % 4) * 2 + 1] = mPointFSerial.y * hyr + radarTop;
        }

        private float[] virtualScreenRect = new float[]
                {
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY,
                        NOT_DRAW_XY, NOT_DRAW_XY
                };

        public void setVirtualScreenRect(PointS[] mPointFSerials) {
            if (mPointFSerials == null || mPointFSerials.length < 4) {
                return;
            }
            for (int i = 0; i < 4; i++) {
                virtualScreenRect[(i % 4) * 2] = mPointFSerials[i].x * wxr;
                virtualScreenRect[(i % 4) * 2 + 1] = mPointFSerials[i].y * hyr + radarTop;
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
        private void setVirtualScreen(PointS[] mPointFSerials) {
            if (mPointFSerials == null || mPointFSerials.length < 4) {
                return;
            }
            for (int i = 0; i < 4; i++) {
                virtualScreen[(i % 4) * 2] = mPointFSerials[i].x * wxr;
                virtualScreen[(i % 4) * 2 + 1] = mPointFSerials[i].y * hyr + radarTop;
            }
        }

        // 墙上较准点在屏幕上
        private final PointS[] collectInScreen
                = new PointS[]{new PointS(NOT_DRAW_XY, NOT_DRAW_XY), new PointS(NOT_DRAW_XY, NOT_DRAW_XY),
                new PointS(NOT_DRAW_XY, NOT_DRAW_XY), new PointS(NOT_DRAW_XY, NOT_DRAW_XY)};

        private void setCollectPointInScreen(PointS[] mPointFSerial) {
            if (mPointFSerial != null && mPointFSerial.length >= 4) {
                collectInScreen[0].x = mPointFSerial[0].x - locationOnScreen[0];
                collectInScreen[0].y = mPointFSerial[0].y - locationOnScreen[1];

                collectInScreen[1].x = mPointFSerial[1].x - locationOnScreen[0];
                collectInScreen[1].y = mPointFSerial[1].y - locationOnScreen[1];

                collectInScreen[2].x = mPointFSerial[2].x - locationOnScreen[0];
                collectInScreen[2].y = mPointFSerial[2].y - locationOnScreen[1];

                collectInScreen[3].x = mPointFSerial[3].x - locationOnScreen[0];
                collectInScreen[3].y = mPointFSerial[3].y - locationOnScreen[1];
            }
        }

        String msg;

        public void showTxt(String msg) {
            this.msg = msg;
        }

        // 清画布
        private void clearCanvas(Canvas canvas) {
            canvas.drawColor(Color.TRANSPARENT);
            //清屏
            mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(mClearPaint);
            mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
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

                    // 画雷达xy
                    canvas.drawLine(0, radarTop, radarWidth, radarTop, mCoordPaint);
                    canvas.drawLine(0, radarBottom, radarWidth, radarBottom, mCoordPaint);

                    // 画屏幕底部
                    canvas.drawLine(0, height, width, height, mCoordPaint);

                    // 画帧率
                    canvas.drawText("F:" + successReqFrame + "/" + totalReqFrame + "--" + drawFrame,
                            frameX, frameY,
                            mFramePaint);
                    drawFrame++;

                    // 画错误码
                    if (result != MainActivity.RC_OK) {
                        canvas.drawText("ERROR::" + result, width / 2, radarTop / 2, mErrorPaint);
                    } else {
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
                    canvas.drawCircle(vertexA.x, vertexA.y, VERTEX_RADIUS, mMovePaint);
                    canvas.drawCircle(vertexB.x, vertexB.y, VERTEX_RADIUS, mMovePaint);
                    canvas.drawCircle(vertexC.x, vertexC.y, VERTEX_RADIUS, mMovePaint);
                    canvas.drawCircle(vertexD.x, vertexD.y, VERTEX_RADIUS, mMovePaint);


                    canvas.drawPoint(virtualScreen[0], virtualScreen[1], mVirtualScreenA);

                    if (Math.abs(startDrawTime - lastSelectTime) > 1500) {
                        mVirtualScreenPaint.setColor(virtualScreenColors[++virtualScreenSelect % virtualScreenColors.length]);
                        lastSelectTime = startDrawTime;
                    }
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
                        canvas.drawCircle(mLastDrawPointS.x, mLastDrawPointS.y, VERTEX_RADIUS, mMovePaint);
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

            mErrorPaint.setStyle(Paint.Style.FILL);
            mErrorPaint.setColor(Color.RED);
            mErrorPaint.setAntiAlias(true);
            mErrorPaint.setStrokeWidth(6);
            mErrorPaint.setTextAlign(Paint.Align.CENTER);
            mErrorPaint.setFakeBoldText(true);
            mErrorPaint.setTextSize(16 * 2);

            mFramePaint.setStyle(Paint.Style.FILL);
            mFramePaint.setColor(Color.WHITE);
            mFramePaint.setAntiAlias(true);
            mFramePaint.setStrokeWidth(6);
            mFramePaint.setTextAlign(Paint.Align.CENTER);
            mFramePaint.setFakeBoldText(true);
            mFramePaint.setTextSize(16 * 2);

            mMovePaint.setStyle(Paint.Style.FILL);
            mMovePaint.setColor(Color.WHITE);
            mMovePaint.setAntiAlias(true);
            mMovePaint.setStrokeWidth(7);
            mMovePaint.setTextAlign(Paint.Align.CENTER);
            mMovePaint.setFakeBoldText(true);
            mMovePaint.setTextSize(16 * 2);
        }

    }
}
