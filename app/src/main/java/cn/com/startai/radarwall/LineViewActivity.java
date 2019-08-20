package cn.com.startai.radarwall;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.veken.chartview.bean.ChartBean;
import com.veken.chartview.drawtype.DrawBgType;
import com.veken.chartview.drawtype.DrawConnectLineType;
import com.veken.chartview.drawtype.DrawLineType;
import com.veken.chartview.view.LineChartView;
import com.victory.chartview.CircleIndicatorView;
import com.victory.chartview.ScrollChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LineViewActivity extends AppCompatActivity {


    private String TAG = "radar";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_view);

        initLineView();

        initView();
        //加个延时设置数据，防止view未绘制完成的情况下就设置数据，正常业务不会出现这种情况，因为会有网络加载数据的过程
        handler.sendEmptyMessageDelayed(0, 1000);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            initData();
        }
    };

    private ScrollChartView scrollChartView;
    private CircleIndicatorView circleIndicatorView;
    private TextView tvTime;
    private TextView tvData;


    private void initView() {
        scrollChartView = findViewById(R.id.scroll_chart_main);
        circleIndicatorView = findViewById(R.id.civ_main);
        tvTime = findViewById(R.id.tv_time);
        tvData = findViewById(R.id.tv_data);

        final Button btnLine = findViewById(R.id.btn_line);
        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scrollChartView.getLineType() == ScrollChartView.LineType.LINE) {
                    scrollChartView.setLineType(ScrollChartView.LineType.ARC);
                    scrollChartView.invalidateView();
                    btnLine.setText("折线");
                } else {
                    scrollChartView.setLineType(ScrollChartView.LineType.LINE);
                    scrollChartView.invalidateView();
                    btnLine.setText("曲线");
                }
            }
        });
    }

    private void initData() {
        int length = 32;
        final List<String> timeList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            timeList.add(i + ":00");
        }

        final List<Double> dataList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            dataList.add((double) new Random().nextInt(100));
        }
        scrollChartView.setData(timeList, dataList);
        scrollChartView.setOnScaleListener(new ScrollChartView.OnScaleListener() {
            @Override
            public void onScaleChanged(int position) {
                tvTime.setText(timeList.get(position));
                tvData.setText(dataList.get(position) + "");
                ScrollChartView.Point point = scrollChartView.getList().get(position);
                circleIndicatorView.setCircleY(point.y);
            }
        });

        //滚动到目标position
        scrollChartView.smoothScrollTo(dataList.size() - 1);
    }





    private void initLineView() {
        LineChartView  lineChartView = findViewById(R.id.chart_view);
        lineChartView.setyLableText("折线图");
//设置点击背景（可以为图片，也可以为一个颜色背景，大小根据textAndClickBgMargin设置）
        lineChartView.setDrawBgType(DrawBgType.DrawBitmap);
//设置图片资源
        lineChartView.setShowPicResource(R.mipmap.click_icon);
//连接线为虚线（也可以为实现）
        lineChartView.setDrawConnectLineType(DrawConnectLineType.DrawDottedLine);
        lineChartView.setClickable(true);
//是否需要画连接线
        lineChartView.setNeedDrawConnectYDataLine(true);
//连接线的颜色
        lineChartView.setConnectLineColor(getResources().getColor(R.color.default_color));
//是否需要背景
        lineChartView.setNeedBg(true);
//画曲线图（也可以为折线图）
        lineChartView.setDrawLineType(DrawLineType.Draw_Curve);

        ArrayList<ChartBean> lineChartBeanList = null;
        lineChartBeanList = new ArrayList<>();
        lineChartView.setDefaultTextSize(24);
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            ChartBean lineChartBean = new ChartBean();
            lineChartBean.setValue(String.valueOf(random.nextInt(10000)));
            lineChartBean.setDate(String.valueOf(i));
            lineChartBeanList.add(lineChartBean);
        }
        lineChartView.setData(lineChartBeanList);

    }


}
