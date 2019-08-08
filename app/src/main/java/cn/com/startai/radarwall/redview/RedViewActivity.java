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
import cn.com.swain.baselib.display.ScreenUtils;
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

    private Calibration mCalibration;

    private boolean canCollect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                mRedView.setPoints(result, buf, false);
                if (mCalibration != null) {
                    mCalibration.setPositionData(result, buf);
                }
            }
        };

        PointF screenWH19 = ScreenUtils.getScreenWH19(getApplicationContext());
        float ofx = screenWH19.x / 4f; // 4分之一 x
        float oey = screenWH19.y / 8f;// 8分之一 y
        PointF A = new PointF(ofx, oey * 5);
        PointF C = new PointF(ofx * 3, oey * 7);
        PointF B = new PointF(A.x, C.y);
        PointF D = new PointF(C.x, A.y);
        PointF S = new PointF(screenWH19.x, screenWH19.y);

        mRedView.setVertex(A, B, C, D);
        mCalibration = new Calibration(A, B, C, D, S);
        mCalibration.setCalibrationCallBack(new Calibration.ICalibrationCallBack() {
            @Override
            public void onTouchPointInScreen(PointF mPointF) {
                mRedView.setPointInScreen(mPointF);
            }

            @Override
            public void onTouchPointInWall(PointF pointF) {
                mRedView.setPointInWall(pointF);
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
                mRedView.bgDiff(buf, size);
            }
        });
        mCalibration.setIVertexFinish(new Calibration.IVertexFinish() {
            @Override
            public void onCollectPointInWall(int i, PointF mPointF) {
                final int show = i + 1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "第" + show + "个点收集完成",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                mRedView.setCollectPointInWall(i, mPointF);
            }


            @Override
            public void onCollectPointInWall(PointF[] mPointFs) {
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
            public void onVirtualScreen(PointF[] mPointFs) {
                mRedView.setVirtualScreen(mPointFs);
            }

            @Override
            public void onVirtualScreenRect(PointF[] mPointFs) {
                mRedView.setVirtualScreenRect(mPointFs);
            }

            @Override
            public void onCollectPointInScreen(PointF[] mPointF) {
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

        });
        mCalibration.start();

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
        mDataCallBack = null;

        if (mCalibration != null) {
            mCalibration.stop();
            mCalibration = null;
        }
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
        flag = true;
        sensor.setCallBack(mDataCallBack);
        sensor.alwaysAcquirePositionData();
    }

    public void stop(View view) {
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
                if (mCalibration != null) {
                    mCalibration.setPositionData(result, chars);
                }
            }
        });
    }

    private int clickCollectTimes;

    public void collect(View view) {
        int i = clickCollectTimes % 4;
        boolean collect = collect(i+1);
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
            if (mCalibration != null) {
                mCalibration.setCollectIndex(i);
            }
            return true;
        }
        return false;
    }

}
