package cn.com.startai.radarwall.calibration;

import android.graphics.PointF;

import java.io.Serializable;
import java.util.Arrays;

import cn.com.swain.baselib.display.LinearFunction;
import cn.com.swain.baselib.display.MathUtils;
import cn.com.swain.baselib.display.PointS;
import cn.com.swain.baselib.display.Screen;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/5
 * desc
 */
public class WallScreen implements Serializable {

    private String TAG_SIN = "wallScreen";

    /**
     * |
     * Y
     * |
     * --x-- a      d
     * <p>
     * <p>
     * --x-- b      c
     */
    // 手机屏幕的校准点
    private PointS mPhonePointA, // a 点
            mPhonePointB, // b 点
            mPhonePointC,// c 点
            mPhonePointD;// d 点

    private Screen mPhoneScreen, // 手机屏幕
            mPhoneCollectScreen;// 手机校准屏幕

    // 手机屏幕在墙上的各点
    private PointS mWallPointA, // a 点
            mWallPointB, // b 点
            mWallPointC,// c 点
            mWallPointD,// d 点
            mPointWallScreenA;//墙上的屏幕A

    private PointS mWallPhoneScreenRate; // 墙上屏幕 比 手机屏幕的比例

    private Screen mWallScreen,
            mWallCollectScreen; //墙上较准屏幕

    public WallScreen() {
        init();
    }

    public WallScreen(PointS mPointA,
                      PointS mPointB,
                      PointS mPointC,
                      PointS mPointD,
                      PointS mPhoneScreen) {
        set(mPointA, mPointB, mPointC, mPointD, mPhoneScreen);
        init();
    }

    public void set(PointS mPointA,
                    PointS mPointB,
                    PointS mPointC,
                    PointS mPointD,
                    PointS mPhoneScreen) {
        this.mPhonePointA = mPointA;
        this.mPhonePointB = mPointB;
        this.mPhonePointC = mPointC;
        this.mPhonePointD = mPointD;

        this.mPhoneScreen = new Screen(mPhoneScreen);
        this.mPhoneCollectScreen = new Screen();
        this.mPhoneCollectScreen.width = Math.abs(mPhonePointD.x - mPhonePointA.x);
        this.mPhoneCollectScreen.height = Math.abs(mPhonePointB.y - mPhonePointA.y);

        Tlog.v(TAG_SIN, " PhonePointA: " + mPointA.toString());
        Tlog.v(TAG_SIN, " PhonePointB: " + mPointB.toString());
        Tlog.v(TAG_SIN, " PhonePointC: " + mPointC.toString());
        Tlog.v(TAG_SIN, " PhonePointD: " + mPointD.toString());
        Tlog.v(TAG_SIN, " PointPhoneScreenRect :" + mPhoneScreen.toString());
    }

    private void init() {
        this.mWallPointA = new PointS();
        this.mWallPointB = new PointS();
        this.mWallPointC = new PointS();
        this.mWallPointD = new PointS();
        this.mPointWallScreenA = new PointS();

        this.mWallScreen = new Screen();
        this.mWallCollectScreen = new Screen();

        this.mWallPhoneScreenRate = new PointS();
    }

    private float angleAC;
    private float angleAD;
    private float angleAB;

    public String getMsg() {
//        return "bevel:" + bevel + " atan:" + atan;
//        return "angleAD:" + angleAD + " angleAB:" + angleAB;
//        return "rateX:" + mWallPhoneScreenRate.x + "-rateY:" + mWallPhoneScreenRate.y;
        return msg;
    }

    String msg = "";

    private boolean n; // true 表示逆时针倾斜 false 表示顺时针倾斜

    public void setWallPoint(PointS[] pointFS) {

        // 校准点的在墙上的坐标
        mWallPointA.set(pointFS[0]);
        mWallPointB.set(pointFS[1]);
        mWallPointC.set(pointFS[2]);
        mWallPointD.set(pointFS[3]);

        Tlog.v(TAG_SIN, " WallPointA: " + mWallPointA.toString());
        Tlog.v(TAG_SIN, " WallPointB: " + mWallPointB.toString());
        Tlog.v(TAG_SIN, " WallPointC: " + mWallPointC.toString());
        Tlog.v(TAG_SIN, " WallPointD: " + mWallPointD.toString());

        if (mWallPointB.x > mWallPointA.x) {
            n = true;
        } else {
            n = false;
        }

        // 计算墙上校准屏幕的宽高
        float widthDA = (float) MathUtils.calculationBevel(mWallPointD, mWallPointA);
        float widthCB = (float) MathUtils.calculationBevel(mWallPointC, mWallPointB);
        mWallCollectScreen.width = (widthDA + widthCB) / 2;
        float heightBA = (float) MathUtils.calculationBevel(mWallPointB, mWallPointA);
        float heightCD = (float) MathUtils.calculationBevel(mWallPointC, mWallPointD);
        mWallCollectScreen.height = (heightBA + heightCD) / 2;
        Tlog.v(TAG_SIN, " WallCollectScreen:" + mWallCollectScreen.toString());

        // 计算墙上屏幕 和 手机屏幕的比例
        mWallPhoneScreenRate.x = mWallCollectScreen.width / mPhoneCollectScreen.width;
        mWallPhoneScreenRate.y = mWallCollectScreen.height / mPhoneCollectScreen.height;
        Tlog.v(TAG_SIN, " WallPhoneScreenRate:" + mWallPhoneScreenRate.toString());

        // 计算墙上屏幕的宽高
        mWallScreen.width = mWallPhoneScreenRate.x * mPhoneScreen.width;
        mWallScreen.height = mWallPhoneScreenRate.y * mPhoneScreen.height;
        Tlog.v(TAG_SIN, " WallScreen:" + mWallScreen.toString());


        // 计算墙上 AD  AB的倾斜角
        float angle = MathUtils.angle(mWallPointA, mWallPointD, new PointS(mWallPointD.x, mWallPointA.y));
        float angle1 = MathUtils.angle(mWallPointB, mWallPointC, new PointS(mWallPointC.x, mWallPointB.y));
        angleAD = (angle + angle1) / 2;
        float angle2 = MathUtils.angle(mWallPointA, mWallPointB, new PointS(mWallPointA.x, mWallPointB.y));
        float angle3 = MathUtils.angle(mWallPointD, mWallPointC, new PointS(mWallPointD.x, mWallPointC.y));
        angleAB = (angle2 + angle3) / 2;
        // AD AB倾斜角的平均值
        angleAC = (angleAD + angleAB) / 2;

        PointS zPointF = new PointS(0, 0);
//        float sqrt = (mWallPhoneScreenRate.x + mWallPhoneScreenRate.y) / 2;

        PointS sqrt = this.mWallPhoneScreenRate;
        float[] floata = calculationStart(zPointF, sqrt, mPhonePointA, mWallPointA, angleAC);
        float[] floatb = calculationStart(zPointF, sqrt, mPhonePointB, mWallPointB, angleAC);
        float[] floatc = calculationStart(zPointF, sqrt, mPhonePointC, mWallPointC, angleAC);
        float[] floatd = calculationStart(zPointF, sqrt, mPhonePointD, mWallPointD, angleAC);
        mPointWallScreenA.x = (floata[0] + floatb[0] + floatc[0] + floatd[0]) / 4;
        mPointWallScreenA.y = (floata[1] + floatb[1] + floatc[1] + floatd[1]) / 4;
        msg = "a:" + (int) floata[0] + (int) floata[1] +
                "b:" + (int) floatb[0] + (int) floatb[1] +
                "c:" + (int) floatc[0] + (int) floatc[1] +
                "d:" + (int) floatd[0] + (int) floatd[1];

        mWidthFunction.calculationLinearFunction(mWallPointA, mWallPointD);
        float kW = mWidthFunction.k;
        mWidthFunction.calculationLinearFunction(mWallPointB, mWallPointC);
        kW += mWidthFunction.k;
        mWidthFunction.calculationLinearFunction(kW / 2, mPointWallScreenA);

        mHeightFunction.calculationLinearFunction(mWallPointA, mWallPointB);
        float kH = mHeightFunction.k;
        mHeightFunction.calculationLinearFunction(mWallPointD, mWallPointC);
        kH += mHeightFunction.k;
        mHeightFunction.calculationLinearFunction(kH / 2, mPointWallScreenA);

        pWidthFunction.setK(mWidthFunction.k);
        pHeightFunction.setK(mHeightFunction.k);
    }

    // 已知边长 ，角度，结束点的坐标，求起点的xy
    private float[] calculationStart(PointS zPointF, PointS sqrt, PointS phonePoint, PointS wallPoint, float mInclinationAngle) {
//        float bevel = (float) (MathUtils.calculationBevel(zPointF, phonePoint) * sqrt);
        PointS n = new PointS(phonePoint.x * sqrt.x, phonePoint.y * sqrt.y);
        float bevel = (float) MathUtils.calculationBevel(zPointF, n);
        // 屏幕原点到校准点的夹角
        float atan = (float) MathUtils.atan(phonePoint.y / phonePoint.x);
        return calculationStart((atan - mInclinationAngle), bevel, wallPoint);
    }

    // 已知 结束点,角度 和斜边边长 ,求开始点
    private float[] calculationStart(float mAngle, float bevel, PointS endPoint) {
        float[] xy = new float[2];
        double vY = MathUtils.sin(mAngle) * bevel;
        double vX = MathUtils.cos(mAngle) * bevel;
        xy[0] = (float) (endPoint.x - vX);
        xy[1] = (float) (endPoint.y - vY);
        return xy;
    }

    private final LinearFunction mWidthFunction = new LinearFunction();
    private final LinearFunction mHeightFunction = new LinearFunction();

    private final PointS[] pointFsVirtualScreenRect = new PointS[]{new PointS(), new PointS(),
            new PointS(), new PointS()};

    // 墙上的屏幕点
    public PointS[] calculationVirtualScreenRect() {

        pointFsVirtualScreenRect[0].set(mPointWallScreenA);
        pointFsVirtualScreenRect[1].set(mPointWallScreenA.x,
                mPointWallScreenA.y + mWallScreen.height);
        pointFsVirtualScreenRect[2].set(mPointWallScreenA.x + mWallScreen.width,
                mPointWallScreenA.y + mWallScreen.height);
        pointFsVirtualScreenRect[3].set(mPointWallScreenA.x + mWallScreen.width,
                mPointWallScreenA.y);

        return pointFsVirtualScreenRect;
    }

    private final PointS[] mVirtualScreen = new PointS[]{new PointS(), new PointS(),
            new PointS(), new PointS()};

    public PointS[] calculationVirtualScreen() {
        mVirtualScreen[0].set(mPointWallScreenA);

        PointS A = new PointS();
        A.set(mPointWallScreenA.x, -mPointWallScreenA.y);

        PointS tmpPointF = new PointS();

        tmpPointF.set(pointFsVirtualScreenRect[1].x, -pointFsVirtualScreenRect[1].y);
        MathUtils.rotate(A, tmpPointF, mVirtualScreen[1], angleAB, !n);
        mVirtualScreen[1].x = pointFsVirtualScreenRect[1].x - mVirtualScreen[1].x + pointFsVirtualScreenRect[1].x;
        mVirtualScreen[1].y = -mVirtualScreen[1].y;

        tmpPointF.set(pointFsVirtualScreenRect[2].x, -pointFsVirtualScreenRect[2].y);
        MathUtils.rotate(A, tmpPointF, mVirtualScreen[2], angleAC, !n);
        mVirtualScreen[2].x = pointFsVirtualScreenRect[2].x - mVirtualScreen[2].x + pointFsVirtualScreenRect[2].x;
        mVirtualScreen[2].y = pointFsVirtualScreenRect[2].y - (tmpPointF.y - mVirtualScreen[2].y);

        tmpPointF.set(pointFsVirtualScreenRect[3].x, -pointFsVirtualScreenRect[3].y);
        MathUtils.rotate(A, tmpPointF, mVirtualScreen[3], angleAD, !n);
        mVirtualScreen[3].y = pointFsVirtualScreenRect[3].y - (tmpPointF.y - mVirtualScreen[3].y);

        return mVirtualScreen;
    }

    private final PointS[] pointFsVertexInScreen = new PointS[]{new PointS(), new PointS(),
            new PointS(), new PointS()};

    private float[] uv = new float[8];

    private final PointS middlePointF = new PointS();

    // 转换墙上的校准点在手机屏幕上的位置
    public PointS[] calculationVertexInScreen() {
        Arrays.fill(uv, 0f);

        calculationInScreen(mWallPointA, pointFsVertexInScreen[0]);
        calculationInScreen(mWallPointB, pointFsVertexInScreen[1]);
        calculationInScreen(mWallPointC, pointFsVertexInScreen[2]);
        calculationInScreen(mWallPointD, pointFsVertexInScreen[3]);

        uv[0] = mPhonePointA.x - pointFsVertexInScreen[0].x;
        uv[1] = mPhonePointA.y - pointFsVertexInScreen[0].y;

        uv[2] = mPhonePointB.x - pointFsVertexInScreen[1].x;
        uv[3] = mPhonePointB.y - pointFsVertexInScreen[1].y;

        uv[4] = mPhonePointC.x - pointFsVertexInScreen[2].x;
        uv[5] = mPhonePointC.y - pointFsVertexInScreen[2].y;

        uv[6] = mPhonePointD.x - pointFsVertexInScreen[3].x;
        uv[7] = mPhonePointD.y - pointFsVertexInScreen[3].y;

        middlePointF.x = ((pointFsVertexInScreen[3].x - pointFsVertexInScreen[0].x)
                + (pointFsVertexInScreen[2].x - pointFsVertexInScreen[1].x))
                / 2 + pointFsVertexInScreen[0].x;

        middlePointF.y = ((pointFsVertexInScreen[1].y - pointFsVertexInScreen[0].y)
                + (pointFsVertexInScreen[2].y - pointFsVertexInScreen[3].y))
                / 2 + pointFsVertexInScreen[0].y;

        Tlog.v(TAG_SIN, " aa: " + pointFsVertexInScreen[0].toString());
        Tlog.v(TAG_SIN, " bb: " + pointFsVertexInScreen[1].toString());
        Tlog.v(TAG_SIN, " cc: " + pointFsVertexInScreen[2].toString());
        Tlog.v(TAG_SIN, " dd: " + pointFsVertexInScreen[3].toString());

        return pointFsVertexInScreen;
    }

    private final LinearFunction pWidthFunction = new LinearFunction();
    private final LinearFunction pHeightFunction = new LinearFunction();
    private final PointS intersection = new PointS();

    public void calculationInScreen(PointS wallP, PointS screenP) {

        pHeightFunction.calculationLinearFunction(wallP);
        mWidthFunction.intersection(pHeightFunction, this.intersection);
        double bevelX = MathUtils.calculationBevel(mPointWallScreenA, intersection);
        screenP.x = (float) (bevelX / mWallScreen.width * mPhoneScreen.width);

        pWidthFunction.calculationLinearFunction(wallP);
        mHeightFunction.intersection(pWidthFunction, this.intersection);
        double bevelY = MathUtils.calculationBevel(mPointWallScreenA, intersection);
        screenP.y = (float) (bevelY / mWallScreen.height * mPhoneScreen.height);

//        c(screenP);
    }

    private final int limit = 10;

    private void c(PointF screenP) {
        if (screenP.x < (middlePointF.x - limit) && screenP.y < (middlePointF.y - limit)) {
            screenP.x += uv[0];
            screenP.y += uv[1];
        } else if (screenP.x < (middlePointF.x - limit) && screenP.y > (middlePointF.y + limit)) {
            screenP.x += uv[2];
            screenP.y += uv[3];
        } else if (screenP.x > (middlePointF.x + limit) && screenP.y > (middlePointF.y + limit)) {
            screenP.x += uv[4];
            screenP.y += uv[5];
        } else if (screenP.x > (middlePointF.x + limit) && screenP.y < (middlePointF.y - limit)) {
            screenP.x += uv[6];
            screenP.y += uv[7];
        }
    }

}
