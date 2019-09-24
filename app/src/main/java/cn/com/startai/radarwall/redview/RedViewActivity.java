package cn.com.startai.radarwall.redview;

import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;

import cn.com.startai.radarwall.R;
import cn.com.startai.radarwall.RadarSensor;
import cn.com.startai.radarwall.calibration.Calibration;
import cn.com.startai.radarwall.calibration.CalibrationManager;
import cn.com.startai.radarwall.utils.InjectInputPointManager;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/7/30
 * desc
 */
public class RedViewActivity extends AppCompatActivity {

    private final int size = RadarSensor.FRAME_DATA_SIZE;
    public static final String TAG = "RedView";
    private RedView mRedView;
    private RadarSensor.IDataCallBack mDataCallBack;
    private RadarSensor sensor;

    private boolean canCollect;
    private InjectInputPointManager mInjectInputPointManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redview);

        sensor = RadarSensor.getInstance();
        mInjectInputPointManager = InjectInputPointManager.getInstance();
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);


        mRedView = findViewById(R.id.redview);
        mRedView.setTouchLsn(new RedView.IVertexTouchLsn() {

            @Override
            public void onClick(int i) {
                collect(i);
            }
        });


        mDataCallBack = new RadarSensor.IDataCallBack() {
            @Override
            public void onPositionData(char[] buf, int size, int result) {
                if (mRedView != null) {
                    mRedView.setPoints(result, buf, false);
                }
                CalibrationManager.getInstance().setPositionData(result, buf);
            }
        };
        PointF screenWH19 = ScreenUtils.getScreenWH19(getApplicationContext());
        float ofx = screenWH19.x / 4f; // 4分之一 x
        float oey = screenWH19.y / 8f;// 8分之一 y
        PointS A = new PointS(ofx, oey * 2);
        PointS C = new PointS(ofx * 3, oey * 7);
        PointS B = new PointS(A.x, C.y);
        PointS D = new PointS(C.x, A.y);
        PointS S = new PointS(screenWH19.x, screenWH19.y);
        InjectInputPointManager.getInstance().setScreen(S);
        mRedView.setVertex(A, B, C, D);

        CalibrationManager.getInstance().setCollectPoint(A, B, C, D, S);
        CalibrationManager.getInstance().setCalibrationCallBack(mICalibrationCallBack);
        CalibrationManager.getInstance().setIVertexFinish(mIVertexFinish);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CalibrationManager.getInstance().start(); // 等redView初始化完再运行
            }
        }, 1000);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        CalibrationManager.getInstance().setCalibrationCallBack(null);
        CalibrationManager.getInstance().setIVertexFinish(null);
        CalibrationManager.getInstance().stop();
        CalibrationManager.save(getApplicationContext());
    }

    public void start(View v) {
        if (flag) {
            stop();
            ((Button) v).setText("start");
        } else {
            start();
            ((Button) v).setText("stop");
        }
    }

    private boolean flag = false;

    private void start() {
        if (flag) {
            return;
        }
        flag = true;
        sensor.setCallBack(mDataCallBack);
        sensor.alwaysAcquirePositionData();
    }

    private void stop() {
        if (!flag) {
            return;
        }
        flag = false;
        sensor.setCallBack(null);
        sensor.stopAlwaysAcquirePositionData();
    }

    public void once(View view) {
        if (flag) {
            Toast.makeText(getApplicationContext(), "in always", Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService executorService = sensor.getExecutorService();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                char[] chars = new char[size];
                int result = sensor.acquirePositionDataArrayJ(chars, size);
                Tlog.v(TAG, " acquirePositionData result:" + result);
                mRedView.setPoints(result, chars, true);
                CalibrationManager.getInstance().setPositionData(result, chars);
            }
        });
    }

    private int clickCollectTimes;

    public void collect(View view) {
        int i = clickCollectTimes % 4;
        boolean collect = collect(i + 1);
        if (collect) clickCollectTimes++;
    }

    long toastTime;

    private boolean collect(int i) {

        if (!canCollect) {
            Toast.makeText(getApplicationContext(), "正在收集背景数据", Toast.LENGTH_SHORT).show();
            return false;
        }
        long l = System.currentTimeMillis();
        if ((toastTime + 3000) < l) {
            Toast.makeText(getApplicationContext(), "开始收集" + i + "点", Toast.LENGTH_SHORT).show();
            toastTime = l;
            CalibrationManager.getInstance().setCollectIndex(i);
            return true;
        }
        return false;
    }

    private Calibration.IVertexFinish mIVertexFinish = new Calibration.IVertexFinish() {
        @Override
        public void onCollectPointInWall(int i, PointS mPointS) {
            final int show = i + 1;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "第" + show + "个点收集完成",
                            Toast.LENGTH_SHORT).show();
                }
            });
            if (mRedView != null) {
                mRedView.setCollectPointInWall(i, mPointS);
            }
        }


        @Override
        public void onCollectPointInWall(PointS[] mPointSs) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "4个点收集完成",
                            Toast.LENGTH_LONG).show();
                }
            });
            if (mRedView != null) {
                mRedView.setCollectPointSInWall(mPointSs);
            }
        }

        @Override
        public void onVirtualScreen(PointS[] mPointSs) {
            if (mRedView != null) {
                mRedView.setVirtualScreen(mPointSs);
            }
        }

        @Override
        public void onVirtualScreenRect(PointS[] mPointSs) {
            if (mRedView != null) {
                mRedView.setVirtualScreenRect(mPointSs);
            }
        }

        @Override
        public void showTxt(String msg) {
            if (mRedView != null) {
                mRedView.showTxt(msg);
            }
        }

        @Override
        public void onCollectPointInScreen(PointS[] mPointSs) {
            mRedView.setCollectPointInScreen(mPointSs);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "校准点计算完成",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

    };

    private Calibration.ICalibrationCallBack mICalibrationCallBack = new Calibration.ICalibrationCallBack() {
        @Override
        public void onTouchPointInScreen(PointS mPointS) {
            if (mRedView != null) {
                mRedView.setPointInScreen(mPointS);
            }
            mInjectInputPointManager.offer(mPointS);
        }

        @Override
        public void onTouchPointInWall(PointS mPointS) {
            if (mRedView != null) {
                mRedView.setPointInWall(mPointS);
            }
        }


        @Override
        public void onWallBG(char[] buf) {
            canCollect = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "背景收集完成",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onWallBGDiff(int[] buf, int size) {
            if (mRedView != null) {
                mRedView.bgDiff(buf, size);
            }
        }

        @Override
        public void onFps(int fps, int lastFps) {
            if (mRedView != null) {
                mRedView.setAlgFps(fps, lastFps);
            }
        }

        @Override
        public void onTouchFps(int fps, int lastFps) {
            if (mRedView != null) {
                mRedView.setTouchFps(fps, lastFps);
            }
        }
    };

    public void resetBG(View view) {
        CalibrationManager.getInstance().resetBG();
        Toast.makeText(getApplicationContext(), "重新收集背景", Toast.LENGTH_SHORT).show();
    }

    public void toastTest(View view) {
        Toast.makeText(getApplicationContext(), "clickToastBtn", Toast.LENGTH_SHORT).show();
    }
}
