package cn.com.startai.radarwall.calibration;

import java.io.Serializable;
import java.util.Arrays;

import cn.com.swain.baselib.alg.LinearFunction;
import cn.com.swain.baselib.alg.MathUtils;
import cn.com.swain.baselib.alg.PointS;
import cn.com.swain.baselib.alg.Screen;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/5
 * desc
 * * |--x-- a      d
 * * y
 * * y
 * * |--x-- b      c
 */

public class WallScreen implements Serializable {

    private String TAG_SIN = "wallScreen";

    // 手机屏幕的校准点
    private final PointS mPhonePointA = new PointS(); // a 点
    private final PointS mPhonePointB = new PointS();  // b 点
    private final PointS mPhonePointC = new PointS(); // c 点
    private final PointS mPhonePointD = new PointS(); // d 点

    private final Screen mPhoneScreen = new Screen(); // 手机屏幕
    private final Screen mPhoneCollectScreen = new Screen();// 手机校准屏幕

    // 手机校准屏幕在墙上的各点
    private final PointS mWallPointA = new PointS(); // a 点
    private final PointS mWallPointB = new PointS(); // b 点
    private final PointS mWallPointC = new PointS();// c 点
    private final PointS mWallPointD = new PointS();// d 点
    private final PointS mWallScreenPointA = new PointS();//墙上的屏幕A

    private final PointS mWallPhoneScreenRate = new PointS(); // 墙上屏幕 比 手机屏幕的比例

    private final Screen mWallScreen = new Screen();//墙上手机屏幕
    private final Screen mWallCollectScreen = new Screen();//墙上较准屏幕

    WallScreen() {
    }

    public WallScreen(PointS mPointA,
                      PointS mPointB,
                      PointS mPointC,
                      PointS mPointD,
                      PointS mPhoneScreen) {
        set(mPointA, mPointB, mPointC, mPointD, mPhoneScreen);
    }

    public void set(PointS mPointA,
                    PointS mPointB,
                    PointS mPointC,
                    PointS mPointD,
                    PointS mPhoneScreen) {
        this.mPhonePointA.set(mPointA);
        this.mPhonePointB.set(mPointB);
        this.mPhonePointC.set(mPointC);
        this.mPhonePointD.set(mPointD);

        this.mPhoneScreen.set(mPhoneScreen);

        this.mPhoneCollectScreen.width = Math.abs(mPhonePointD.x - mPhonePointA.x);
        this.mPhoneCollectScreen.height = Math.abs(mPhonePointB.y - mPhonePointA.y);

        Tlog.v(TAG_SIN, " PhonePointA: " + mPointA.toString());
        Tlog.v(TAG_SIN, " PhonePointB: " + mPointB.toString());
        Tlog.v(TAG_SIN, " PhonePointC: " + mPointC.toString());
        Tlog.v(TAG_SIN, " PhonePointD: " + mPointD.toString());
        Tlog.v(TAG_SIN, " PointPhoneScreenRect :" + mPhoneScreen.toString());
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
        mWallCollectScreen.width = (widthDA * 0.6f + widthCB * 0.4f);
        float heightBA = (float) MathUtils.calculationBevel(mWallPointB, mWallPointA);
        float heightCD = (float) MathUtils.calculationBevel(mWallPointC, mWallPointD);
        mWallCollectScreen.height = (heightBA * 0.6f + heightCD * 0.4f);
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
        angleAD = angle * 0.5f + angle1 * 0.5f;
        float angle2 = MathUtils.angle(mWallPointA, mWallPointB, new PointS(mWallPointA.x, mWallPointB.y));
        float angle3 = MathUtils.angle(mWallPointD, mWallPointC, new PointS(mWallPointD.x, mWallPointC.y));
        angleAB = angle2 * 0.5f + angle3 * 0.5f;
        // AD AB倾斜角的平均值
        angleAC = (angleAD + angleAB) / 2;

        PointS zPointF = new PointS(0, 0);

        float[] floata = calculationStart(zPointF, mWallPhoneScreenRate, mPhonePointA, mWallPointA, angleAC);
        float[] floatb = calculationStart(zPointF, mWallPhoneScreenRate, mPhonePointB, mWallPointB, angleAC);
        float[] floatc = calculationStart(zPointF, mWallPhoneScreenRate, mPhonePointC, mWallPointC, angleAC);
        float[] floatd = calculationStart(zPointF, mWallPhoneScreenRate, mPhonePointD, mWallPointD, angleAC);
        mWallScreenPointA.x = (floata[0] + floatb[0] + floatc[0] + floatd[0]) / 4 * 0.5f;
        mWallScreenPointA.y = (floata[1] + floatb[1] + floatc[1] + floatd[1]) / 4 * 0.5f;
        msg = "a:" + (int) floata[0] + "-" + (int) floata[1] +
                "b:" + (int) floatb[0] + "-" + (int) floatb[1] +
                "c:" + (int) floatc[0] + "-" + (int) floatc[1] +
                "d:" + (int) floatd[0] + "-" + (int) floatd[1];

        float[] floata2 = calculationStart2(zPointF, mWallPhoneScreenRate, mPhonePointA, mWallPointA, angleAC);
        float[] floatb2 = calculationStart2(zPointF, mWallPhoneScreenRate, mPhonePointB, mWallPointB, angleAC);
        float[] floatc2 = calculationStart2(zPointF, mWallPhoneScreenRate, mPhonePointC, mWallPointC, angleAC);
        float[] floatd2 = calculationStart2(zPointF, mWallPhoneScreenRate, mPhonePointD, mWallPointD, angleAC);
        mWallScreenPointA.x += (floata2[0] + floatb2[0] + floatc2[0] + floatd2[0]) / 4 * 0.5f;
        mWallScreenPointA.y += (floata2[1] + floatb2[1] + floatc2[1] + floatd2[1]) / 4 * 0.5f;


        mWidthFunction.calculationLinearFunction(mWallPointA, mWallPointD);
        float kW = mWidthFunction.k;
        mWidthFunction.calculationLinearFunction(mWallPointB, mWallPointC);
        kW += mWidthFunction.k;
        mWidthFunction.calculationLinearFunction(kW / 2, mWallScreenPointA);

        mHeightFunction.calculationLinearFunction(mWallPointA, mWallPointB);
        float kH = mHeightFunction.k;
        mHeightFunction.calculationLinearFunction(mWallPointD, mWallPointC);
        kH += mHeightFunction.k;
        mHeightFunction.calculationLinearFunction(kH / 2, mWallScreenPointA);

        pWidthFunction.setK(mWidthFunction.k);
        pHeightFunction.setK(mHeightFunction.k);
    }

    // 已知边长 ，角度，结束点的坐标，求起点的xy
    private float[] calculationStart(PointS zPointF, PointS rate, PointS phonePoint, PointS wallPoint, float mInclinationAngle) {
        PointS n = new PointS(phonePoint.x * rate.x, phonePoint.y * rate.y);
        float bevel = (float) MathUtils.calculationBevel(zPointF, n);
        // 屏幕原点到校准点的夹角
        float atan = (float) MathUtils.atan(phonePoint.x / phonePoint.y);
        return calculationStart((atan + mInclinationAngle), bevel, wallPoint);
    }

    public static float[] calculationStart(float mAngle, float bevel, PointS endPoint) {
        float[] xy = new float[2];
        xy[0] = (float) (endPoint.x - MathUtils.sin(mAngle) * bevel);
        xy[1] = (float) (endPoint.y - MathUtils.cos(mAngle) * bevel);
        return xy;
    }

    // 已知边长 ，角度，结束点的坐标，求起点的xy
    private float[] calculationStart2(PointS zPointF, PointS sqrt, PointS phonePoint, PointS wallPoint, float mInclinationAngle) {
//        float bevel = (float) (MathUtils.calculationBevel(zPointF, phonePoint) * sqrt);
        PointS n = new PointS(phonePoint.x * sqrt.x, phonePoint.y * sqrt.y);
        float bevel = (float) MathUtils.calculationBevel(zPointF, n);
        // 屏幕原点到校准点的夹角
        float atan = (float) MathUtils.atan(phonePoint.y / phonePoint.x);
        return calculationStart2((atan - mInclinationAngle), bevel, wallPoint);
    }

    // 已知 结束点,角度 和斜边边长 ,求开始点
    private float[] calculationStart2(float mAngle, float bevel, PointS endPoint) {
        float[] xy = new float[2];
        xy[0] = (float) (endPoint.x - MathUtils.cos(mAngle) * bevel);
        xy[1] = (float) (endPoint.y - MathUtils.sin(mAngle) * bevel);
        return xy;
    }

    private final LinearFunction mWidthFunction = new LinearFunction();
    private final LinearFunction mHeightFunction = new LinearFunction();

    private final PointS[] pointFsVirtualScreenRect = new PointS[]{new PointS(), new PointS(),
            new PointS(), new PointS()};

    // 墙上的屏幕点
    public PointS[] calculationVirtualScreenRect() {

        pointFsVirtualScreenRect[0].set(mWallScreenPointA);
        pointFsVirtualScreenRect[1].set(mWallScreenPointA.x,
                mWallScreenPointA.y + mWallScreen.height);
        pointFsVirtualScreenRect[2].set(mWallScreenPointA.x + mWallScreen.width,
                mWallScreenPointA.y + mWallScreen.height);
        pointFsVirtualScreenRect[3].set(mWallScreenPointA.x + mWallScreen.width,
                mWallScreenPointA.y);

        return pointFsVirtualScreenRect;
    }

    private final PointS[] mVirtualScreen = new PointS[]{new PointS(), new PointS(),
            new PointS(), new PointS()};

    public PointS[] calculationVirtualScreen() {
        mVirtualScreen[0].set(mWallScreenPointA);

        PointS A = new PointS();
        A.set(mWallScreenPointA.x, -mWallScreenPointA.y);

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

    private final float[] uv = new float[8];

    private final PointS middlePointS = new PointS();

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

        for (int i = 0; i < uv.length; i++) {
            uv[i] /= 2;
        }

        middlePointS.x = ((pointFsVertexInScreen[3].x - pointFsVertexInScreen[0].x)
                + (pointFsVertexInScreen[2].x - pointFsVertexInScreen[1].x))
                / 2 + pointFsVertexInScreen[0].x;

        middlePointS.y = ((pointFsVertexInScreen[1].y - pointFsVertexInScreen[0].y)
                + (pointFsVertexInScreen[2].y - pointFsVertexInScreen[3].y))
                / 2 + pointFsVertexInScreen[0].y;

        // 计算偏差向量后再重新计算一次
        calculationInScreen(mWallPointA, pointFsVertexInScreen[0]);
        calculationInScreen(mWallPointB, pointFsVertexInScreen[1]);
        calculationInScreen(mWallPointC, pointFsVertexInScreen[2]);
        calculationInScreen(mWallPointD, pointFsVertexInScreen[3]);

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
        double bevelX = MathUtils.calculationBevel(mWallScreenPointA, intersection);
        screenP.x = (float) (bevelX / mWallScreen.width * mPhoneScreen.width);

        pWidthFunction.calculationLinearFunction(wallP);
        mHeightFunction.intersection(pWidthFunction, this.intersection);
        double bevelY = MathUtils.calculationBevel(mWallScreenPointA, intersection);
        screenP.y = (float) (bevelY / mWallScreen.height * mPhoneScreen.height);

        scale(screenP);
    }

    private final int limit = 10;

    private void scale(PointS screenP) {
        if (screenP.x < (middlePointS.x - limit) && screenP.y < (middlePointS.y - limit)) {
            screenP.x += uv[0];
            screenP.y += uv[1];
        } else if (screenP.x < (middlePointS.x - limit) && screenP.y > (middlePointS.y + limit)) {
            screenP.x += uv[2];
            screenP.y += uv[3];
        } else if (screenP.x > (middlePointS.x + limit) && screenP.y > (middlePointS.y + limit)) {
            screenP.x += uv[4];
            screenP.y += uv[5];
        } else if (screenP.x > (middlePointS.x + limit) && screenP.y < (middlePointS.y - limit)) {
            screenP.x += uv[6];
            screenP.y += uv[7];
        }
    }

}
