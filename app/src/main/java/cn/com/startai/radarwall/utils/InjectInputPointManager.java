package cn.com.startai.radarwall.utils;

import android.app.Application;
import android.app.Instrumentation;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.startai.radarwall.calibration.Calibration;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.app.IApp.IApp;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/9/3
 * desc
 */
public class InjectInputPointManager implements IApp {

    private static final String TAG = "InjectInputPointManager";

    private InjectInputPointManager() {
    }

    private final int SIZE = Calibration.TOUCH_FPS + 1;
    private final int BUFSIZE = SIZE * 20;
    private Queue<PointS> queue;
    private PointS[] pointSs;
    private final Object syncObj = new Object();
    private ExecutorService executorService;
    private Input input;

    @Override
    public void init(Application app) {
        executorService = Executors.newSingleThreadExecutor();
//        queue = new LinkedList<>();
//        queue = new ConcurrentLinkedQueue<>(); // 非阻塞
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            queue = new ConcurrentLinkedDeque<>();// 阻塞
        } else {
            queue = new ArrayDeque<>(BUFSIZE);
        }

        pointSs = new PointS[BUFSIZE];
        for (int i = 0; i < BUFSIZE; i++) {
            pointSs[i] = new PointS();
        }
        input = new Input();
        setScreen(new PointS(ScreenUtils.getScreenWH19(app)));
    }

    public boolean isRun() {
        return input.run;
    }

    public boolean isInput() {
        return input.input;
    }

    public void start() {
        if (input.run) {
            return;
        }
        input.run = true;
        executorService.execute(input);
    }

    public void stop() {
        input.run = false;
    }

    private int index;

    private int add = 0;

    public void offer(PointS mPointS) {
        Tlog.w(TAG, " poll:" + add + " :" + mPointS);
        PointS pointSs = this.pointSs[index++ % BUFSIZE];
        pointSs.set(mPointS);
        queue.offer(pointSs);
        add++;
        synchronized (syncObj) {
            syncObj.notify();
        }
    }

    private final PointS S = new PointS();

    // 设置屏幕宽高
    public void setScreen(PointS s) {
        S.set(s);
        Tlog.v(TAG, " screen:" + S);
    }

    private static final class ClassHolder {
        private static final InjectInputPointManager IIP = new InjectInputPointManager();
    }

    public static InjectInputPointManager getInstance() {
        return ClassHolder.IIP;
    }

    private final class Input implements Runnable {

        private volatile boolean run;
        private PointS mLastPointS = new PointS(0, 0);
        private Instrumentation m_Instrumentation = new Instrumentation();
        private volatile boolean input;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            long start = System.nanoTime();
            while (run) {
//                Tlog.e(TAG, " Instrumentation use time: " + (System.nanoTime() - start) + "ns"); //16097396ns
                if (add <= 0) {
                    Tlog.e(TAG, "Input runnable add <= 0; wait()");
                    input = false;
                    synchronized (syncObj) {
                        try {
                            syncObj.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                start = System.nanoTime();
                add--;
                PointS poll = queue.poll();
                if (poll == null) {
                    Tlog.d(TAG, "queue.poll()=null continue");
                    continue;
                }
                input = true;
                Tlog.d(TAG, " poll:" + add + " :" + poll);
                move(poll);
//                click(poll);
            }
        }

        private void move(PointS poll) {

            if ((poll.x < 0 || poll.x > S.x)
                    ||
                    (poll.y < 0 || poll.y > S.y)) { // 当前点无效

                if ((mLastPointS.x >= 0 && mLastPointS.x <= S.x)
                        &&
                        (mLastPointS.y >= 0 && mLastPointS.y <= S.y)) { // 上一点有效
                    Tlog.v(TAG, " sendKeySync ACTION_UP");
                    m_Instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_B));

                    m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, mLastPointS.x, mLastPointS.y, 0));

                }

                mLastPointS.set(poll);

                return;
            }

            if ((mLastPointS.x < 0 || mLastPointS.x > S.x)
                    ||
                    (mLastPointS.y < 0 || mLastPointS.y > S.y)) { // 上一点无效
                Tlog.v(TAG, " sendKeySync ACTION_DOWN");
                m_Instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B));

                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, poll.x, poll.y, 0));

            }

            Tlog.v(TAG, " sendKeySync ACTION_MOVE");
            m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, poll.x, poll.y, 0));

            mLastPointS.set(poll);

        }

        // 单击一个点
        private void click(PointS poll) {
            if ((poll.x < 0 || poll.x > S.x)
                    ||
                    (poll.y < 0 || poll.y > S.y)) { // 当前点无效
                return;
            }
            m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_B);
            m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, poll.x, poll.y, 0));
            m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, poll.x, poll.y, 0));
        }

    }

}
