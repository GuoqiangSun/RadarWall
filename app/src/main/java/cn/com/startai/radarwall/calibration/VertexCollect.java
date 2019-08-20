package cn.com.startai.radarwall.calibration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import cn.com.startai.radarwall.RadarSensor;
import cn.com.swain.baselib.alg.MathUtils;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/1
 * desc
 */
public class VertexCollect implements Serializable {

    private String TAG = "VertexCollect";

    private final int REMOVE = 20; // 移除 前后的个数

    private final int MAX_FPS = RadarSensor.MAX_FPS;

    // 最小不的低于MAX_FPS
    // REMOVE*4 表示统计前后的点不要，排序后的前后点不要
    private final int MAX_SIZE = MAX_FPS * 3 + REMOVE * 4;

    private final PointS[] mPointS1 = new PointS[MAX_SIZE];
    private int PointSAIndex;

    private final PointS[] mPointS2 = new PointS[MAX_SIZE];
    private int PointSBIndex;

    private final PointS[] mPointS3 = new PointS[MAX_SIZE];
    private int PointSCIndex;

    private final PointS[] mPointS4 = new PointS[MAX_SIZE];
    private int PointSDIndex;


    /**
     * 四个顶点的数组
     * <p>
     * 0 ==A
     * 1 ==B
     * 2 ==C
     * 3 ==D
     */
    private final PointS[] PointSS = new PointS[]
            {
                    new PointS(), new PointS(),
                    new PointS(), new PointS()
            };

    public PointS[] getPointSS() {
        return PointSS;
    }

    public PointS getPointS(int index) {
        return PointSS[index % 4];
    }

    public PointS getA() {
        return PointSS[0];
    }

    public PointS getB() {
        return PointSS[1];
    }

    public PointS getC() {
        return PointSS[2];
    }

    public PointS getD() {
        return PointSS[3];
    }

    VertexCollect() {
    }

    public void resetIndex(int i) {
        switch (i) {
            case 1:
                PointSAIndex = 0;
                PointSS[0].x = 0;
                PointSS[0].y = 0;
                break;
            case 2:
                PointSBIndex = 0;
                PointSS[1].x = 0;
                PointSS[1].y = 0;
                break;
            case 3:
                PointSCIndex = 0;
                PointSS[2].x = 0;
                PointSS[2].y = 0;
                break;
            case 4:
                PointSDIndex = 0;
                PointSS[3].x = 0;
                PointSS[3].y = 0;
                break;
            default:
                PointSAIndex = 0;
                PointSBIndex = 0;
                PointSCIndex = 0;
                PointSDIndex = 0;
                PointSS[0].x = 0;
                PointSS[0].y = 0;
                PointSS[1].x = 0;
                PointSS[1].y = 0;
                PointSS[2].x = 0;
                PointSS[2].y = 0;
                PointSS[3].x = 0;
                PointSS[3].y = 0;
                break;
        }
    }

    public int add(PointS mPointS, int i) {
        switch (i) {
            case -1:
                Tlog.v(TAG, " PointFSerial1 -1 ");
                return -1;
            case 1:
                addA(mPointS);
                if (PointSAIndex >= MAX_SIZE) {
                    calculationPoint(mPointS1, PointSS[0]);
                    Tlog.v(TAG, " PointS1 " + PointSS[0].toString());
                    return 1;
                }
                break;
            case 2:
                addB(mPointS);
                if (PointSBIndex >= MAX_SIZE) {
                    calculationPoint(mPointS2, PointSS[1]);
                    Tlog.v(TAG, " PointS2 " + PointSS[1].toString());
                    return 2;
                }
                break;
            case 3:
                addC(mPointS);
                if (PointSCIndex >= MAX_SIZE) {
                    calculationPoint(mPointS3, PointSS[2]);
                    Tlog.v(TAG, " PointS3 " + PointSS[2].toString());
                    return 3;
                }
                break;
            case 4:
                addD(mPointS);
                if (PointSDIndex >= MAX_SIZE) {
                    calculationPoint(mPointS4, PointSS[3]);
                    Tlog.v(TAG, " PointS4 " + PointSS[3].toString());
                    return 4;
                }
                break;
            default:
                Tlog.e(TAG, " add PointS finish");
                if (PointSAIndex >= MAX_SIZE
                        && PointSBIndex >= MAX_SIZE
                        && PointSCIndex >= MAX_SIZE
                        && PointSDIndex >= MAX_SIZE) {
                    return 5;
                }
                break;
        }
        return -1;
    }

    public boolean collectFinish() {
        return PointSAIndex >= MAX_SIZE
                && PointSBIndex >= MAX_SIZE
                && PointSCIndex >= MAX_SIZE
                && PointSDIndex >= MAX_SIZE;
    }

    public void calculationABCD() {

        Tlog.v(TAG, " sortABCDByMiddle  " + PointStoString());
        MathUtils.sortABCDByMiddle(PointSS);
        Tlog.v(TAG, " sortABCDByMiddle :" + PointStoString());

    }

    private void calculationPoint(PointS[] mPointSs, PointS mPointS) {
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;

        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        int availableSize = MAX_SIZE - REMOVE * 2;
        PointS[] tmp = new PointS[availableSize];
        // 去除前后点
        System.arraycopy(mPointSs, REMOVE, tmp, 0, availableSize);

        Arrays.sort(tmp, new Comparator<PointS>() {
            @Override
            public int compare(PointS o1, PointS o2) {
                return Float.compare(o1.x * o1.y, o2.x * o2.y);
            }
        });
        // 排序后，去除前面 和 后面
        for (int i = REMOVE; i < (availableSize - REMOVE); i++) {
            mPointS.x += tmp[i].x;
            mPointS.y += tmp[i].y;

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

        mPointS.x = (mPointS.x - minX - maxX) / (availableSize - REMOVE * 2 - 2);
        mPointS.y = (mPointS.y - minY - maxY) / (availableSize - REMOVE * 2 - 2);
    }

    public void addA(PointS mPoints) {
        mPointS1[PointSAIndex % MAX_SIZE] = new PointS(mPoints);
        PointSAIndex++;
    }

    public void addB(PointS mPoints) {
        mPointS2[PointSBIndex % MAX_SIZE] = new PointS(mPoints);
        PointSBIndex++;
    }

    public void addC(PointS mPoints) {
        mPointS3[PointSCIndex % MAX_SIZE] = new PointS(mPoints);
        PointSCIndex++;
    }

    public void addD(PointS mPoints) {
        mPointS4[PointSDIndex % MAX_SIZE] = new PointS(mPoints);
        PointSDIndex++;
    }

    private String PointStoString() {
        return "VertexCollect[ A  " + PointSS[0].toString()
                + "B  " + PointSS[1].toString()
                + "C  " + PointSS[2].toString()
                + "D  " + PointSS[3].toString()
                + " ]";
    }


}
