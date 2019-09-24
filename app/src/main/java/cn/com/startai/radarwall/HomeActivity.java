package cn.com.startai.radarwall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import cn.com.startai.radarwall.aidl.IRadarAidl;
import cn.com.startai.radarwall.aidl.RadarService;
import cn.com.startai.radarwall.calibration.CalibrationManager;
import cn.com.startai.radarwall.redview.RedViewActivity;
import cn.com.startai.radarwall.utils.FileManager;
import cn.com.startai.radarwall.utils.InjectInputPointManager;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.display.StatusBarUtil;
import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.log.logRecord.impl.LogRecordManager;
import cn.com.swain.baselib.permission.PermissionGroup;
import cn.com.swain.baselib.permission.PermissionRequest;
import cn.com.swain.baselib.permission.PermissionSingleton;
import cn.com.swain.baselib.util.IpUtil;

/**
 * author Guoqiang_Sun
 * date 2019/7/29
 * desc
 */
public class HomeActivity extends AppCompatActivity {

    private String TAG = "radar";

    private final int size = RadarSensor.FRAME_DATA_SIZE;
    private ExecutorService executorService;
    private RadarSensor sensor;
    private EditText mTime1Edt; //第一积分
    private EditText mTime2Edt;//第二积分
    private EditText mTimeEdt;//积分


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.fullscreenShowBarFontBlack(getWindow());
        setContentView(R.layout.activity_main);

        sensor = RadarSensor.getInstance();
        executorService = sensor.initPool();

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(sensor.stringFromJNI());

        ipEdt = findViewById(R.id.ip_edt);
        portEdt = findViewById(R.id.port_edt);
        mTime1Edt = findViewById(R.id.time1_edt);
        mTime2Edt = findViewById(R.id.time2_edt);

        mTimeEdt = findViewById(R.id.time_edt);

        Intent s = new Intent(this, RadarService.class);
        bindService(s, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    private EditText ipEdt;
    private EditText portEdt;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IRadarAidl iRadarAidl = IRadarAidl.Stub.asInterface(service);
            try {
                iRadarAidl.add(1, 2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
        connect = RadarSensor.RC_FAILED;
        sensor.setCallBack(null);
        sensor.stopAlwaysAcquirePositionData();
        sensor.disconnect();
        sensor.Uninitialize();
        sensor.releaseService();
        PermissionSingleton.getInstance().release(this);
        PermissionSingleton.getInstance().clear();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Tlog.v(TAG, " onTouchEvent " + event);
        return super.onTouchEvent(event);
    }

    public void disconnect(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), "already disconnected", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                sensor.disconnect();
                connect = RadarSensor.RC_FAILED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "disconnect success", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private int connect = RadarSensor.RC_FAILED;

    public void connect(View view) {
        if (connect == RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), "already connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (executorService == null) {
            Toast.makeText(getApplicationContext(), "executorService released", Toast.LENGTH_SHORT).show();
            return;
        }

        String ipStr = ipEdt.getText().toString();
        if ("".equalsIgnoreCase(ipStr)) {
//            String connectedWiFiSSID = WiFiUtil.getConnectedWiFiSSID(getApplication());
//            if ("TP-LINK_280024".equalsIgnoreCase(connectedWiFiSSID)) {
//                //内网地址
//                ipStr = "192.168.0.119";
//            } else {
//                // 外网地址
//                // 外网映射到内网的 192.168.0.119:4010
//                ipStr = "192.168.1.230";
//            }
            ipStr = "192.168.0.119";
            ipEdt.setText(ipStr);
        } else {
            if (!IpUtil.ipMatches(ipStr)) {
                Toast.makeText(getApplicationContext(), "ip输入不合法", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final String conIp = ipStr;

        String portStr = portEdt.getText().toString();
        int port = 4010;
        if ("".equalsIgnoreCase(portStr)) {
            portEdt.setText("4010");
        } else {
            try {
                port = Integer.parseInt(portStr);
                if (!IpUtil.isAvailablePort(port)) {
                    Toast.makeText(getApplicationContext(), "ip输入不合法", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "ip输入不合法", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final int conPort = port;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int initialize = sensor.Initialize();
                Tlog.v(TAG, " Initialize " + initialize);
                connect = sensor.connect(conIp, conPort);
                Tlog.v(TAG, " connect " + connect);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                (connect == RadarSensor.RC_OK) ?
                                        "connect success"
                                        : "connect fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void isconnect(View view) {
        Toast.makeText(getApplicationContext(),
                (sensor.isConnected() && connect == RadarSensor.RC_OK)
                        ? "Connected"
                        : "Disconnected", Toast.LENGTH_SHORT).show();
    }

    private boolean flag;

    private final Runnable showToast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
        }
    };

    public void alwaysReqData(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!flag) {
            Toast.makeText(getApplicationContext(), " 开始请求数据", Toast.LENGTH_SHORT).show();
            ((Button) view).setText("停止请求");
            sensor.setCallBack(new RadarSensor.IDataCallBack() {
                private long showTimes;

                @Override
                public void onPositionData(char[] buf, int size, int result) {
                    if (result != RadarSensor.RC_OK) {
                        Tlog.e(TAG, " onPositionData:" + result);
                        long l = System.currentTimeMillis();
                        if (Math.abs(showTimes - l) > 3000) {
                            runOnUiThread(showToast);
                            showTimes = l;
                        }
                    }
                }
            });
            sensor.alwaysAcquirePositionData();
        } else {
            Toast.makeText(getApplicationContext(), " 停止请求数据", Toast.LENGTH_SHORT).show();
            ((Button) view).setText("一直请求");
            sensor.stopAlwaysAcquirePositionData();
            sensor.setCallBack(null);
        }
        flag = !flag;
    }

    public void reqData(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                char[] chars = new char[size];
                final int result = sensor.acquirePositionDataArrayJ(chars, size);
                Tlog.v(TAG, " acquirePositionData result:" + result);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                (result == RadarSensor.RC_OK) ? "success" : "Error::" + result,
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    public void closeLd(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int result = sensor.closeLD();
                Tlog.v(TAG, " closeLd result:" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                (result == RadarSensor.RC_OK) ?
                                        "close success"
                                        : "close fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void openLd(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int result = sensor.openLD();
                Tlog.v(TAG, " openLd result:" + result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                (result == RadarSensor.RC_OK) ?
                                        "open success"
                                        : "open fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void skipRedview(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, RedViewActivity.class));
    }

    public void calibration(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, RedViewActivity.class));
    }

    public void saveLog(View view) {
        PermissionSingleton.getInstance().requestPermission(this, new PermissionRequest.OnPermissionResult() {
            @Override
            public boolean onPermissionRequestResult(String permission, boolean granted) {

                Toast.makeText(getApplicationContext(), permission + " " + granted, Toast.LENGTH_SHORT).show();

                if (granted) {
                    FileManager.getInstance().init(getApplication());
                    Tlog.setLogRecordDebug(true);

                    if (!Tlog.hasILogRecordImpl()) {
                        File logPath = FileManager.getInstance().getLogPath();
                        LogRecordManager mLogRecord = new LogRecordManager(logPath,
                                "radar",
                                1024 * 1024 * 8);

                        Tlog.set(mLogRecord);
                        Toast.makeText(getApplicationContext(), "saveLog:" + logPath.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }

                }
                return false;
            }
        }, PermissionGroup.STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionSingleton.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    public void toastclick(View view) {
        Toast.makeText(getApplicationContext(), "点击了toastButton", Toast.LENGTH_LONG).show();
    }

    public void clear(View view) {
        CalibrationManager.clear(getApplicationContext());
        Toast.makeText(getApplicationContext(), "clear success", Toast.LENGTH_SHORT).show();
    }

    public void clearIpPort(View view) {
        ipEdt.setText("");
        portEdt.setText("");
    }

    public void setTime(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        String s = mTimeEdt.getText().toString();
        final int time;
        try {
            time = Integer.parseInt(s);
            if (time < 0 || time > 800) {
                Toast.makeText(getApplicationContext(), "积分时间范围0~800", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "请输入数字", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int i = sensor.setIntegrationTime(time);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i != RadarSensor.RC_OK) {
                            Toast.makeText(getApplicationContext(), "积分时间设置失败 ERROR::" + i, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "积分时间设置成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public void setTwiceTime(View view) {
        if (connect != RadarSensor.RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        String s = mTime1Edt.getText().toString();
        final int time;
        try {
            time = Integer.parseInt(s);
            if (time < 0 || time > 800) {
                Toast.makeText(getApplicationContext(), "积分时间范围0~800", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "请输入数字", Toast.LENGTH_SHORT).show();
            return;
        }
        String s1 = mTime2Edt.getText().toString();
        final int time2;
        try {
            time2 = Integer.parseInt(s1);
            if (time2 < 0 || time2 > 800) {
                Toast.makeText(getApplicationContext(), "积分时间范围0~800", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "请输入数字", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int i = sensor.setTwiceIntegrationTime(time, time2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (i != RadarSensor.RC_OK) {
                            Toast.makeText(getApplicationContext(), "积分时间设置失败 ERROR::" + i, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "积分时间设置成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    public void tapTest(View view) {
        startActivity(new Intent(this, InjectTestActivity.class));
    }
}
