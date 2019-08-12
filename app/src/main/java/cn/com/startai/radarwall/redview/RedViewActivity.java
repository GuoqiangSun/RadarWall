package cn.com.startai.radarwall.redview;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;

import cn.com.startai.radarwall.MainActivity;
import cn.com.startai.radarwall.R;
import cn.com.startai.radarwall.calibration.Calibration;
import cn.com.startai.radarwall.calibration.CalibrationManager;
import cn.com.swain.baselib.display.PointS;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.display.StatusBarUtil;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/7/30
 * desc
 */
public class RedViewActivity extends AppCompatActivity {

    private final int size = MainActivity.FRAME_DATA_SIZE;
    public static final String TAG = "RedView";
    private RedView mRedView;
    private MainActivity.IDataCallBack mDataCallBack;
    private MainActivity sensor;

    private boolean canCollect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.fullScreenHideStatusBar(getWindow(), false);
        setContentView(R.layout.activity_redview);

        sensor = MainActivity.getInstance();

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


        mDataCallBack = new MainActivity.IDataCallBack() {
            @Override
            public void onPositionData(char[] buf, int size, int result) {
                MainActivity.reserveBuf(buf);
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

        mRedView.setVertex(A, B, C, D);

        CalibrationManager.get(getApplicationContext());
        CalibrationManager.getInstance().setCollectPoint(A, B, C, D, S);
        CalibrationManager.getInstance().setCalibrationCallBack(mICalibrationCallBack);
        CalibrationManager.getInstance().setIVertexFinish(mIVertexFinish);
        CalibrationManager.getInstance().start();

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
        if (flag) {
            stop(null);
        }
        CalibrationManager.getInstance().setCalibrationCallBack(null);
        CalibrationManager.getInstance().setIVertexFinish(null);
        CalibrationManager.getInstance().stop();
        CalibrationManager.save(getApplicationContext());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Tlog.v(TAG, " RedViewActivity onTouchEvent x:" + x + " y:" + y);
        return super.onTouchEvent(event);
    }

    private boolean flag;

    public void start(View view) {
        if (flag) {
            return;
        }
        flag = true;
        sensor.setCallBack(mDataCallBack);
        sensor.alwaysAcquirePositionData();
    }

    public void stop(View view) {
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
        if (executorService == null) {
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                char[] chars = new char[size];
                int result = sensor.acquirePositionDataArray(chars, size);
                Tlog.v(TAG, " acquirePositionData result:" + result);
                MainActivity.reserveBuf(chars);
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
        public void onCollectPointInWall(int i, PointS mPointF) {
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
                mRedView.setCollectPointInWall(i, mPointF);
            }
        }


        @Override
        public void onCollectPointInWall(PointS[] mPointFs) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "4个点收集完成",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onVirtualScreen(PointS[] mPointFs) {
            if (mRedView != null) {
                mRedView.setVirtualScreen(mPointFs);
            }
        }

        @Override
        public void onVirtualScreenRect(PointS[] mPointFs) {
            if (mRedView != null) {
                mRedView.setVirtualScreenRect(mPointFs);
            }
        }

        @Override
        public void showTxt(String msg) {
            if (mRedView != null) {
                mRedView.showTxt(msg);
            }
        }

        @Override
        public void onCollectPointInScreen(PointS[] mPointF) {
            mRedView.setCollectPointInScreen(mPointF);
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
        public void onTouchPointInScreen(PointS mPointF) {
            if (mRedView != null) {
                mRedView.setPointInScreen(mPointF);
            }
        }

        @Override
        public void onTouchPointInWall(PointS pointF) {
            if (mRedView != null) {
                mRedView.setPointInWall(pointF);
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
    };

}
