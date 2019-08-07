package cn.com.startai.radarwall.calibration;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.com.startai.radarwall.MainActivity;
import cn.com.startai.radarwall.R;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/7/30
 * desc
 */
public class CalibrationActivity extends AppCompatActivity implements MainActivity.IDataCallBack {

    private String TAG = "calActivity";

    Button btnA;
    Button btnB;
    Button btnC;
    Button btnD;

    int[] locationA; // x , y ,w, d
    int[] locationB;
    int[] locationC;
    int[] locationD;

    MainActivity sensor;


    private Calibration mCalibration;

    private boolean flag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calibration);

        btnA = findViewById(R.id.a_btn);
        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tlog.v(TAG, " collect A");
                Toast.makeText(getApplicationContext(), "开始收集A点", Toast.LENGTH_SHORT).show();
                if (mCalibration != null) {
                    mCalibration.setCollectIndex(1);
                }
            }
        });
        btnB = findViewById(R.id.b_btn);
        btnB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "开始收集B点", Toast.LENGTH_SHORT).show();
                Tlog.v(TAG, " collect B");
                if (mCalibration != null) {
                    mCalibration.setCollectIndex(2);
                }
            }
        });
        btnC = findViewById(R.id.c_btn);
        btnC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "开始收集C点", Toast.LENGTH_SHORT).show();
                Tlog.v(TAG, " collect C");
                if (mCalibration != null) {
                    mCalibration.setCollectIndex(3);
                }
            }
        });
        btnD = findViewById(R.id.d_btn);
        btnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "开始收集D点", Toast.LENGTH_SHORT).show();
                Tlog.v(TAG, " collect D");
                if (mCalibration != null) {
                    mCalibration.setCollectIndex(4);
                }
            }
        });

        sensor = MainActivity.getInstance();

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensor.setCallBack(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensor.setCallBack(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flag) {
            stop(null);
        }
    }


    @Override
    public void onPositionData(char[] buf, int size, int result) {
        MainActivity.reserveBuf(buf);
        if (mCalibration != null) {
            mCalibration.setPositionData(result, buf);
        }
    }

    public void testCalibrationResult(View view) {
        Toast.makeText(getApplicationContext(), "测试结果按钮", Toast.LENGTH_LONG).show();
    }

    public void start(View view) {

        if (btnA.getTop() == 0 && btnA.getBottom() == 0) {
            Toast.makeText(getApplicationContext(), "view not init,please wait a moment", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] locationA = ScreenUtils.getLocationOnScreen(btnA);
        Tlog.v(TAG, " A :: x:" + locationA[0] + " y:" + locationA[1]
                + " w:" + locationA[2] + " h:" + locationA[3]);
        int[] locationB = ScreenUtils.getLocationOnScreen(btnB);
        Tlog.v(TAG, " B :: x:" + locationB[0] + " y:" + locationB[1]
                + " w:" + locationB[2] + " h:" + locationB[3]);
        int[] locationC = ScreenUtils.getLocationOnScreen(btnC);
        Tlog.v(TAG, " C :: x:" + locationC[0] + " y:" + locationC[1]
                + " w:" + locationC[2] + " h:" + locationC[3]);
        int[] locationD = ScreenUtils.getLocationOnScreen(btnD);
        Tlog.v(TAG, " D :: x:" + locationD[0] + " y:" + locationD[1]
                + " w:" + locationD[2] + " h:" + locationD[3]);

        if (mCalibration == null) {
            PointF pointFA = new PointF(locationA[0], locationA[1]);
            PointF pointFB = new PointF(locationB[0], locationB[1]);
            PointF pointFC = new PointF(locationC[0], locationC[1]);
            PointF pointFD = new PointF(locationD[0], locationD[1]);
            PointF screenWH19 = ScreenUtils.getScreenWH19(getApplicationContext());

            mCalibration = new Calibration(pointFA, pointFB, pointFC, pointFD, screenWH19);
            mCalibration.start();
        }
        if (!flag) {
            flag = true;
            sensor.alwaysAcquirePositionData();
        }
    }

    public void stop(View view) {
        if (mCalibration != null) {
            mCalibration.stop();
            mCalibration = null;
        }
        if (flag) {
            flag = false;
            sensor.stopAlwaysAcquirePositionData();
        }
    }


    public void calABCD(View view) {

        Tlog.v(TAG, " -------tb--------");
        Tlog.v(TAG, " A :: top:" + btnA.getTop() + " left:" + btnA.getLeft()
                + " bottom:" + btnA.getBottom() + " right:" + btnA.getRight());
        Tlog.v(TAG, " B :: top:" + btnB.getTop() + " left:" + btnB.getLeft()
                + " bottom:" + btnB.getBottom() + " right:" + btnB.getRight());
        Tlog.v(TAG, " C :: top:" + btnC.getTop() + " left:" + btnC.getLeft()
                + " bottom:" + btnC.getBottom() + " right:" + btnC.getRight());
        Tlog.v(TAG, " D :: top:" + btnD.getTop() + " left:" + btnD.getLeft()
                + " bottom:" + btnD.getBottom() + " right:" + btnD.getRight());

        Tlog.v(TAG, " -------wd--------");
        Tlog.v(TAG, " A :: x:" + btnA.getX() + " y:" + btnA.getY()
                + " w:" + btnA.getWidth() + " height:" + btnA.getHeight());
        Tlog.v(TAG, " B :: x:" + btnB.getX() + " y:" + btnB.getY()
                + " w:" + btnB.getWidth() + " height:" + btnB.getHeight());
        Tlog.v(TAG, " C :: x:" + btnC.getX() + " y:" + btnC.getY()
                + " w:" + btnC.getWidth() + " height:" + btnC.getHeight());
        Tlog.v(TAG, " D :: x:" + btnD.getX() + " y:" + btnD.getY()
                + " w:" + btnD.getWidth() + " height:" + btnD.getHeight());


        Tlog.v(TAG, " -------location--------");
        locationA = ScreenUtils.getLocationOnScreen(btnA);
        Tlog.v(TAG, " A :: x:" + locationA[0] + " y:" + locationA[1]
                + " w:" + locationA[2] + " h:" + locationA[3]);
        locationB = ScreenUtils.getLocationOnScreen(btnB);
        Tlog.v(TAG, " B :: x:" + locationB[0] + " y:" + locationB[1]
                + " w:" + locationB[2] + " h:" + locationB[3]);
        locationC = ScreenUtils.getLocationOnScreen(btnC);
        Tlog.v(TAG, " C :: x:" + locationC[0] + " y:" + locationC[1]
                + " w:" + locationC[2] + " h:" + locationC[3]);
        locationD = ScreenUtils.getLocationOnScreen(btnD);
        Tlog.v(TAG, " D :: x:" + locationD[0] + " y:" + locationD[1]
                + " w:" + locationD[2] + " h:" + locationD[3]);

        Tlog.v(TAG, " -------screen--------");
        PointF screenWH = ScreenUtils.getScreenWH(getApplicationContext());
        Tlog.v(TAG, " screenWH ::  w:" + screenWH.x + " h:" + screenWH.y);
        PointF screenWH19 = ScreenUtils.getScreenWH19(getApplicationContext());
        Tlog.v(TAG, " screenWH19 ::  w:" + screenWH19.x + " h:" + screenWH19.y);
        PointF windowWH = ScreenUtils.getWindowWH(getApplicationContext());
        Tlog.v(TAG, " windowWH ::  w:" + windowWH.x + " h:" + windowWH.y);
    }

}
