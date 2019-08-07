package cn.com.startai.radarwall.calibration;

import android.graphics.PointF;

import java.util.Arrays;
import java.util.Comparator;

import cn.com.swain.baselib.display.MathUtils;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/1
 * desc
 */
public class VertexUtil {

    private PointF[] mPointF1;
    private int pointFAIndex;

    private PointF[] mPointF2;
    private int pointFBIndex;

    private PointF[] mPointF3;
    private int pointFCIndex;

    private PointF[] mPointF4;
    private int pointFDIndex;

    private final int MAX_SIZE = 70; // 最小不的低于70
    private final int REMOVE = 10; // 移除 前后的个数

    private PointF[] pointFS = new PointF[4];

    public PointF[] getPointFS() {
        return pointFS;
    }

    public PointF getPointF(int index) {
        return pointFS[index % 4];
    }

    public PointF getA() {
        return pointFS[0];
    }

    public PointF getB() {
        return pointFS[1];
    }

    public PointF getC() {
        return pointFS[2];
    }

    public PointF getD() {
        return pointFS[3];
    }

    public VertexUtil() {
        mPointF1 = new PointF[MAX_SIZE];
        mPointF2 = new PointF[MAX_SIZE];
        mPointF3 = new PointF[MAX_SIZE];
        mPointF4 = new PointF[MAX_SIZE];
        for (int i = 0; i < 4; i++) {
            pointFS[i] = new PointF();
        }
    }

    public void resetIndex(int i) {
        switch (i) {
            case 1:
                pointFAIndex = 0;
                pointFS[0].x = 0;
                pointFS[0].y = 0;
                break;
            case 2:
                pointFBIndex = 0;
                pointFS[1].x = 0;
                pointFS[1].y = 0;
                break;
            case 3:
                pointFCIndex = 0;
                pointFS[2].x = 0;
                pointFS[2].y = 0;
                break;
            case 4:
                pointFDIndex = 0;
                pointFS[3].x = 0;
                pointFS[3].y = 0;
                break;
            default:
                pointFAIndex = 0;
                pointFBIndex = 0;
                pointFCIndex = 0;
                pointFDIndex = 0;
                pointFS[0].x = 0;
                pointFS[0].y = 0;
                pointFS[1].x = 0;
                pointFS[1].y = 0;
                pointFS[2].x = 0;
                pointFS[2].y = 0;
                pointFS[3].x = 0;
                pointFS[3].y = 0;
                break;
        }
    }

    private String TAG = "VertexUtil";

    public int add(PointF mPointF, int i) {
        switch (i) {
            case -1:
                Tlog.v(TAG, " pointF1 -1 ");
                return -1;
            case 1:
                addA(mPointF);
                if (pointFAIndex >= MAX_SIZE) {
                    calculationPoint(mPointF1, pointFS[0]);
                    Tlog.v(TAG, " pointF1 " + pointFS[0].toString());
                    return 1;
                }
                break;
            case 2:
                addB(mPointF);
                if (pointFBIndex >= MAX_SIZE) {
                    calculationPoint(mPointF2, pointFS[1]);
                    Tlog.v(TAG, " pointF2 " + pointFS[1].toString());
                    return 2;
                }
                break;
            case 3:
                addC(mPointF);
                if (pointFCIndex >= MAX_SIZE) {
                    calculationPoint(mPointF3, pointFS[2]);
                    Tlog.v(TAG, " pointF3 " + pointFS[2].toString());
                    return 3;
                }
                break;
            case 4:
                addD(mPointF);
                if (pointFDIndex >= MAX_SIZE) {
                    calculationPoint(mPointF4, pointFS[3]);
                    Tlog.v(TAG, " pointF4 " + pointFS[3].toString());
                    return 4;
                }
                break;
            default:
                Tlog.e(TAG, " add pointF finish");
                if (pointFAIndex >= MAX_SIZE
                        && pointFBIndex >= MAX_SIZE
                        && pointFCIndex >= MAX_SIZE
                        && pointFDIndex >= MAX_SIZE) {
                    return 5;
                }
                break;
        }
        return -1;
    }

    public void calculationABCD() {

        Tlog.v(TAG, " pointF1 " + pointFStoString());
        MathUtils.sortABCD(pointFS);
        Tlog.v(TAG, "reSort area :" + pointFStoString());

    }

    private void calculationPoint(PointF[] pointFB, PointF pointF) {
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;

        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        int availableSize = MAX_SIZE - REMOVE * 2;
        PointF[] tmp = new PointF[availableSize]; // 去除前后十个点

        System.arraycopy(pointFB, REMOVE, tmp, 0, availableSize);

        Arrays.sort(tmp, new Comparator<PointF>() {
            @Override
            public int compare(PointF o1, PointF o2) {
                return Float.compare(o1.x * o1.y, o2.x * o2.y);
            }
        });
        // 排序后，去除前面最小十个点 和 后面最大十个点
        for (int i = REMOVE; i < (availableSize - REMOVE); i++) {
            pointF.x += tmp[i].x;
            pointF.y += tmp[i].y;

            if (minX > tmp[i].x) {
                minX = tmp[i].x;
            } else if (maxX < tmp[i].x) {
                maxX = tmp[i].x;
            }

            if (minY > tmp[i].y) {
                minY = tmp[i].y;
            } else if (maxY < tmp[i].y) {
                maxY = tmp[i].y;
            }
        }

        pointF.x = (pointF.x - minX - maxX) / (availableSize - REMOVE * 2 - 2);
        pointF.y = (pointF.y - minY - maxY) / (availableSize - REMOVE * 2 - 2);
    }

    public void addA(PointF mPointF) {
        mPointF1[pointFAIndex % MAX_SIZE] = new PointF(mPointF.x, mPointF.y);
        pointFAIndex++;
    }

    public void addB(PointF mPointF) {
        mPointF2[pointFBIndex % MAX_SIZE] = new PointF(mPointF.x, mPointF.y);
        pointFBIndex++;
    }

    public void addC(PointF mPointF) {
        mPointF3[pointFCIndex % MAX_SIZE] = new PointF(mPointF.x, mPointF.y);
        pointFCIndex++;
    }

    public void addD(PointF mPointF) {
        mPointF4[pointFDIndex % MAX_SIZE] = new PointF(mPointF.x, mPointF.y);
        pointFDIndex++;
    }

    public String pointFStoString() {
        return " A  " + pointFS[0].toString()
                + "B  " + pointFS[1].toString()
                + "C  " + pointFS[2].toString()
                + "D  " + pointFS[3].toString();
    }


}
