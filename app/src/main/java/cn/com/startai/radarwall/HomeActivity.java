package cn.com.startai.radarwall;

import android.app.Instrumentation;
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
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.display.StatusBarUtil;
import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.log.logRecord.impl.LogRecordManager;
import cn.com.swain.baselib.permission.PermissionGroup;
import cn.com.swain.baselib.permission.PermissionRequest;
import cn.com.swain.baselib.permission.PermissionSingleton;
import cn.com.swain.baselib.util.IpUtil;
import cn.com.swain.baselib.util.WiFiUtil;

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
            String connectedWiFiSSID = WiFiUtil.getConnectedWiFiSSID(getApplication());
            if ("TP-LINK_280024".equalsIgnoreCase(connectedWiFiSSID)) {
                //内网地址
                ipStr = "192.168.0.119";
            } else {
                // 外网地址
                // 外网映射到内网的 192.168.0.119:4010
                ipStr = "192.168.1.230";
            }
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
                if (port > 65535 || port < 0) {
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
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int result = sensor.setLD(5);
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
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int result = sensor.setLD(4);
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

    int i = 0;
    final int m = 4;

    public void inputTab(View view) {
        View viewById = findViewById(R.id.toast_btn);
        int[] locationOnScreen = ScreenUtils.getLocationOnScreen(viewById);
        switch (i % m) {
            case 0:
                invokeInjectInputEvent(locationOnScreen);
                break;
            case 1:
                instrumentation(locationOnScreen);
                break;
            case 2:
                inputTap(locationOnScreen);
                break;
            case 3:
                inputShell(locationOnScreen);
                break;
            default:
                invokeInjectInputEvent(locationOnScreen);
                break;
        }
        i++;
    }

    private void instrumentation(final int[] locationOnScreen) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Instrumentation m_Instrumentation = new Instrumentation();
                long l = System.nanoTime();
                m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_B);
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, locationOnScreen[0], locationOnScreen[1], 0));
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, locationOnScreen[0], locationOnScreen[1], 0));
                Tlog.e(TAG, " Instrumentation use time: " + (System.nanoTime() - l));
            }
        });
    }

    private void invokeInjectInputEvent(final int[] locationOnScreen) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                InputManager im = (InputManager) getSystemService(Context.INPUT_SERVICE);
                Class<? extends InputManager> aClass = im.getClass();
                try {
                    Method injectInputEvent = aClass.getDeclaredMethod("injectInputEvent", InputEvent.class, int.class);
                    try {
                        long l = System.nanoTime();
                        InputEvent inputEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B);
                        injectInputEvent.invoke(im, inputEventDown, 2);

                        InputEvent inputEventUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_B);
                        injectInputEvent.invoke(im, inputEventUp, 2);

                        InputEvent inputEventDownXy = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_DOWN,
                                locationOnScreen[0], locationOnScreen[1], 0);
                        Object invoke = injectInputEvent.invoke(im, inputEventDownXy, 2);
                        Tlog.v(TAG, " invoke injectInputEvent : " + invoke);

                        InputEvent inputEventUpXy = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_UP,
                                locationOnScreen[0], locationOnScreen[1], 0);
                        invoke = injectInputEvent.invoke(im, inputEventUpXy, 2);
                        Tlog.v(TAG, " invoke injectInputEvent : " + invoke);


                        Tlog.e(TAG, " invoke injectInputEvent use time: " + (System.nanoTime() - l));


                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    Tlog.e(TAG, " injectInputEvent ", e);
                }
            }
        });

    }

    private void inputShell(final int[] locationOnScreen) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long l = System.nanoTime();
//                int shell = RadarSensor.getInstance().shell(" input keyevent 4 ");
                int shell = RadarSensor.getInstance().shell("input tap " + locationOnScreen[0] + " " + locationOnScreen[1]);
                Tlog.v(TAG, " shell: in android " + shell + " useTime:" + (System.nanoTime() - l));
            }
        });
    }

    private void inputTap(final int[] locationOnScreen) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long l = System.nanoTime();
//                int shell = RadarSensor.getInstance().shell(" input keyevent 4 ");
                RadarSensor.getInstance().tapxy(locationOnScreen[0], locationOnScreen[1]);
                Tlog.v(TAG, " tapxy: in android  useTime:" + (System.nanoTime() - l));
            }
        });
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
}
