package cn.com.startai.radarwall;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.com.swain.baselib.log.Tlog;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final int RC_FAILED = -1;
    private static final int RC_OK = 0; // 成功
    private static final int RC_INVALID_ARGUMENT = 2;
    private static final int RC_NOT_CONNECTED = 3;
    private static final int RC_ALREADY_CONNECTED = 4;
    private static final int RC_IN_PROGRESS = 5;
    private static final int RC_COMMUNICATION_FAILED = 6;
    private static final int RC_INVALID_RESPONSE = 7;
    private static final int RC_CONNECTION_CLOSED = 8;
    private static final int RC_ADDRESS_BIND_FAILED = 9;

    private String TAG = "radar";

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        executorService = Executors.newFixedThreadPool(70);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connect = RC_FAILED;
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private int connect = RC_FAILED;

    public void connect(View view) {
        if (connect == RC_OK) {
            Toast.makeText(getApplicationContext(), " already connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (executorService == null) {
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int initialize = Initialize();
                Tlog.v(TAG, " Initialize " + initialize);

                connect = connect("192.168.1.62", 4010);
                Tlog.v(TAG, " connect " + connect);
            }
        });
    }


    public void reqData(View view) {
        if (connect != RC_OK) {
            Toast.makeText(getApplicationContext(), " please connect", Toast.LENGTH_SHORT).show();
            return;
        }
        if (executorService == null) {
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                char[] chars = new char[320];
                int result = acquirePositionDataArray(chars, 320);
                Tlog.v(TAG, " acquirePositionData result:" + result);
                if (result == 0) {
                    for (int i = 0; i < 320; i++) {
                        Tlog.v(TAG, " index =" + i + " : " + (int) chars[i]);
                    }
                }
            }
        });
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native int Initialize();

    public native void Uninitialize();

    public native int connect(String ip, int port);

    public native int connect(String ip, int port, String remoteIp, int remotePort);

    public native void disconnect();

    public native boolean isConnected();

    public native int setIntegrationTime(int time);

    public native int setTwiceIntegrationTime(int time1, int time2);

    public native int setLD(int gear);

    public native int[] acquirePositionData();

    public native int acquirePositionDataArray(char[] chars, int length);

}
