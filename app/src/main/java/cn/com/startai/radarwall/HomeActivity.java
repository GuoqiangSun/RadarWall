package cn.com.startai.radarwall;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutorService;

import cn.com.startai.radarwall.redview.RedViewActivity;
import cn.com.startai.radarwall.utils.FileManager;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.log.Tlog;
import cn.com.swain.baselib.log.logRecord.impl.LogRecordManager;
import cn.com.swain.baselib.permission.PermissionGroup;
import cn.com.swain.baselib.permission.PermissionRequest;
import cn.com.swain.baselib.permission.PermissionSingleton;
import cn.com.swain.baselib.util.WiFiUtil;

public class HomeActivity extends AppCompatActivity {

    private String TAG = "radar";

    private final int size = RadarSensor.FRAME_DATA_SIZE;
    private ExecutorService executorService;
    private RadarSensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensor = RadarSensor.getInstance();
        executorService = sensor.initPool();

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(sensor.stringFromJNI());

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
        connect = RadarSensor.RC_FAILED;
        sensor.setCallBack(null);
        sensor.stopAlwaysAcquirePositionData();
        sensor.disconnect();
        sensor.Uninitialize();
        sensor.releaseService();
        PermissionSingleton.getInstance().release(this);
        PermissionSingleton.getInstance().clear();
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
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int initialize = sensor.Initialize();
                Tlog.v(TAG, " Initialize " + initialize);

                String connectedWiFiSSID = WiFiUtil.getConnectedWiFiSSID(getApplication());

                Tlog.v(TAG, " getConnectedWiFiSSID :" + connectedWiFiSSID);

                if ("TP-LINK_280024".equalsIgnoreCase(connectedWiFiSSID)) {
                    //内网地址
                    connect = sensor.connect("192.168.0.119", 4010);
                } else {
                    // 外网映射到内网的 192.168.0.119:4010,直接访问外网地址即可
                    connect = sensor.connect("192.168.1.230", 4010);

                }
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
            ((Button) view).setText("停止请求数据");
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
            ((Button) view).setText("一直请求数据");
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

        if (executorService == null) {
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                char[] chars = new char[size];
                final int result = sensor.acquirePositionDataArray(chars, size);
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

    public void inputTab(View view) {
        View viewById = findViewById(R.id.toast_btn);
        final int[] locationOnScreen = ScreenUtils.getLocationOnScreen(viewById);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long l = System.currentTimeMillis();
//                int shell = RadarSensor.getInstance().shell(" input keyevent 4 ");
                Tlog.v(TAG, " shell: in android  startTime:" + l);
                int shell = RadarSensor.getInstance().shell("input tap " + locationOnScreen[0] + " " + locationOnScreen[1]);
                long l1 = System.currentTimeMillis();
                Tlog.v(TAG, " shell: in android  endTime:" + l1);
                Tlog.v(TAG, " shell: in android" + shell + " useTime:" + (l1 - l));

                RadarSensor.getInstance().tapxy(locationOnScreen[0], locationOnScreen[1]);
                Tlog.v(TAG, " tapxy in thread:  useTime:" + (System.currentTimeMillis() - l1));
            }
        });

        long l1 = System.currentTimeMillis();
        RadarSensor.getInstance().tapxy(locationOnScreen[0], locationOnScreen[1]);
        Tlog.v(TAG, " tapxy in ui:  useTime:" + (System.currentTimeMillis() - l1));
    }

    public void toastclick(View view) {
        Toast.makeText(getApplicationContext(), "点击了toastButton", Toast.LENGTH_SHORT).show();
    }


}
