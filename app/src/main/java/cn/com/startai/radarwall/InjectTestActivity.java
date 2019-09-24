package cn.com.startai.radarwall;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import cn.com.startai.radarwall.utils.InjectInputPointManager;
import cn.com.swain.baselib.alg.LinearFunction;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.display.ScreenUtils;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/9/4
 * desc
 */
public class InjectTestActivity extends AppCompatActivity {
    private int[] locationOnScreen = new int[2];
    private Button toastBtn;
    ExecutorService executorService;
    private String TAG = "InjectTestActivity";
    private EditText fromX;
    private EditText fromY;
    private EditText toX;
    private EditText toY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject);
        toastBtn = findViewById(R.id.toast_btn);
        fromX = findViewById(R.id.from_x);
        fromY = findViewById(R.id.from_y);
        toX = findViewById(R.id.to_x);
        toY = findViewById(R.id.to_y);
        final TextView mTxt = findViewById(R.id.screen_txt);
        final ListView lst = findViewById(R.id.lst);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] locationOnScreen = ScreenUtils.getLocationOnScreen(lst);
                mTxt.append("; lsv X:" + locationOnScreen[0] + "Y:" + locationOnScreen[1]);
            }
        }, 3000);
        PointF screenWH19 = ScreenUtils.getScreenWH19(getApplicationContext());
        mTxt.setText("screen W:" + screenWH19.x + "H:" + screenWH19.y);


        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        final int SIZE = 100 * 10;
        List<String> data = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            data.add(String.valueOf(i));
        }
        lst.setAdapter(new ArrayAdapter<String>(this, R.layout.item_txt, data));
        executorService = RadarSensor.getInstance().getExecutorService();

        InjectInputPointManagerIsRun = InjectInputPointManager.getInstance().isRun();
        if (!InjectInputPointManagerIsRun) {
            InjectInputPointManager.getInstance().start();
        }
    }

    boolean InjectInputPointManagerIsRun;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!InjectInputPointManagerIsRun) {
            InjectInputPointManager.getInstance().stop();
        }
    }

    private final PointS startPointS = new PointS();
    private final PointS endPointS = new PointS();
    private final PointS curPointS = new PointS();
    private final LinearFunction function = new LinearFunction();

    public void move(View view) {

        if (InjectInputPointManager.getInstance().isInput()) {
            Toast.makeText(getApplicationContext(), "isInput please retry", Toast.LENGTH_SHORT).show();
            return;
        }

        String fxs = fromX.getText().toString();
        try {
            startPointS.x = Integer.parseInt(fxs);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "fromX input error", Toast.LENGTH_SHORT).show();
            return;
        }

        String fyx = fromY.getText().toString();
        try {
            startPointS.y = Integer.parseInt(fyx);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "fromY input error", Toast.LENGTH_SHORT).show();
            return;
        }

        String txs = toX.getText().toString();
        try {
            endPointS.x = Integer.parseInt(txs);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "toX input error", Toast.LENGTH_SHORT).show();
            return;
        }

        String tys = toY.getText().toString();
        try {
            endPointS.y = Integer.parseInt(tys);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "toY input error", Toast.LENGTH_SHORT).show();
            return;
        }
        InjectInputPointManager.getInstance().offer(nullPoint);

        function.calculationLinearFunction(startPointS, endPointS);

//                float max = Math.max(Math.abs(endPointS.y - startPointS.y), Math.abs(endPointS.x - startPointS.x));

        float l;
        float d;
        if (function.k == 0) {
            l = endPointS.y - startPointS.y;
        } else {
            l = (endPointS.x - startPointS.x);
        }
        d = l / 10;

        for (int i = 0; i < 11; i++) {
            if (function.k == 0) {
                if (function.b == startPointS.x) {
                    curPointS.x = startPointS.x;
                    curPointS.y = startPointS.y + d * i;
                } else if (function.b == startPointS.y) {
                    curPointS.x = startPointS.x + d * i;
                    curPointS.y = startPointS.y;
                } else {
                    curPointS.x = startPointS.x + d * i;
                    curPointS.y = startPointS.y + d * i;
                }
            } else {
                curPointS.x = startPointS.x + d * i;
                curPointS.y = function.calculationY(curPointS.x);
            }
            InjectInputPointManager.getInstance().offer(curPointS);
        }

        InjectInputPointManager.getInstance().offer(nullPoint);

    }

    private PointS toastPoint = new PointS();
    private PointS nullPoint = new PointS(-1f, -1f);

    public void Inject(View view) {
        ScreenUtils.getLocationOnScreen(toastBtn, toastPoint);
        InjectInputPointManager.getInstance().offer(nullPoint);
        InjectInputPointManager.getInstance().offer(toastPoint);
        InjectInputPointManager.getInstance().offer(nullPoint);
    }


    public void toastclick(View view) {
        Toast.makeText(getApplicationContext(), "点击了toastButton", Toast.LENGTH_LONG).show();
    }

    public void inputTab(View view) {
        ScreenUtils.getLocationOnScreen(toastBtn, locationOnScreen);
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

    public void exeIM(View view) {
        ScreenUtils.getLocationOnScreen(toastBtn, locationOnScreen);
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

                        InputEvent inputEventDownXy = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_DOWN,
                                locationOnScreen[0], locationOnScreen[1], 0);
                        Object invoke = injectInputEvent.invoke(im, inputEventDownXy, 2);
                        Tlog.v(TAG, " invoke injectInputEvent : " + invoke);

                        InputEvent inputEventUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_B);
                        injectInputEvent.invoke(im, inputEventUp, 2);

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

    public void exeShell(View view) {
        ScreenUtils.getLocationOnScreen(toastBtn, locationOnScreen);
        inputShell(locationOnScreen);
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

}
