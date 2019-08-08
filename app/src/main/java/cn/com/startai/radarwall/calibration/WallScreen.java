package cn.com.startai.radarwall.calibration;

import android.graphics.PointF;

import cn.com.swain.baselib.display.LinearFunction;
import cn.com.swain.baselib.display.MathUtils;
import cn.com.swain.baselib.display.Screen;
import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/8/5
 * desc
 */
public class WallScreen {

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
    private PointF mPhonePointA, // a 点
            mPhonePointB, // b 点
            mPhonePointC,// c 点
            mPhonePointD;// d 点

    private Screen mPhoneScreen, // 手机屏幕
            mPhoneCollectScreen;// 手机校准屏幕

    // 手机屏幕在墙上的各点
    private PointF mWallPointA, // a 点
            mWallPointB, // b 点
            mWallPointC,// c 点
            mWallPointD,// d 点
            mPointWallScreenA;//墙上的屏幕A

    private PointF
            mWallPhoneScreenRate; // 墙上屏幕 比 手机屏幕的比例

    private Screen mWallScreen,
            mWallCollectScreen; //墙上较准屏幕

    public WallScreen(PointF mPointA,
                      PointF mPointB,
                      PointF mPointC,
                      PointF mPointD,
                      PointF mPhoneScreen) {
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
        Tlog.v(TAG_SIN, " PointPhoneScreenRect :" + this.mPhoneScreen.toString());

        this.mWallPointA = new PointF();
        this.mWallPointB = new PointF();
        this.mWallPointC = new PointF();
        this.mWallPointD = new PointF();
        this.mPointWallScreenA = new PointF();

        this.mWallScreen = new Screen();
        this.mWallCollectScreen = new Screen();

        this.mWallPhoneScreenRate = new PointF();
    }

    private double InclinationAngle;

    public void setWallPoint(PointF[] pointFS) {

        // 校准点的在墙上的坐标
        mWallPointA.set(pointFS[0]);
        mWallPointB.set(pointFS[1]);
        mWallPointC.set(pointFS[2]);
        mWallPointD.set(pointFS[3]);

        float angle = MathUtils.angle(mWallPointA, mWallPointB, new PointF(mWallPointA.x, mWallPointB.y));
        float angle2 = MathUtils.angle(mWallPointA, mWallPointD, new PointF(mWallPointD.x, mWallPointA.y));
        InclinationAngle = (angle+ angle2) / 2;

        MathUtils.rotate(mWallPointA, mWallPointB, InclinationAngle);
        MathUtils.rotate(mWallPointA, mWallPointC, InclinationAngle);
        MathUtils.rotate(mWallPointA, mWallPointD, InclinationAngle);

        Tlog.v(TAG_SIN, " WallPointA: " + mWallPointA.toString());
        Tlog.v(TAG_SIN, " WallPointB: " + mWallPointB.toString());
        Tlog.v(TAG_SIN, " WallPointC: " + mWallPointC.toString());
        Tlog.v(TAG_SIN, " WallPointD: " + mWallPointD.toString());

        calculationWallScreen();
    }

    private void calculationWallScreen() {

        mWallCollectScreen.width = Math.abs(mWallPointD.x - mWallPointA.x);
        mWallCollectScreen.height = Math.abs(mWallPointB.y - mWallPointA.y);
        Tlog.v(TAG_SIN, " WallCollectScreen:" + mWallCollectScreen.toString());

        mWallPhoneScreenRate.x = mWallCollectScreen.width / mPhoneCollectScreen.width;
        mWallPhoneScreenRate.y = mWallCollectScreen.height / mPhoneCollectScreen.height;
        Tlog.v(TAG_SIN, " WallPhoneScreenRate:" + mWallPhoneScreenRate.toString());

        mWallScreen.width = mWallPhoneScreenRate.x * mPhoneScreen.width;
        mWallScreen.height = mWallPhoneScreenRate.y * mPhoneScreen.height;
        Tlog.v(TAG_SIN, " WallScreen:" + mWallScreen.toString());

        mPointWallScreenA.x = mWallPointA.x - mWallPhoneScreenRate.x * mPhonePointA.x;
        mPointWallScreenA.y = mWallPointA.y - mWallPhoneScreenRate.y * mPhonePointA.y;

    }

    private final PointF[] pointFsVirtualScreenRect = new PointF[]{new PointF(), new PointF(),
            new PointF(), new PointF()};

    // 墙上的屏幕点
    public PointF[] calculationVirtualScreenRect() {

        pointFsVirtualScreenRect[0].set(mPointWallScreenA);
        pointFsVirtualScreenRect[1].set(mPointWallScreenA.x,
                mPointWallScreenA.y + mWallScreen.height);
        pointFsVirtualScreenRect[2].set(mPointWallScreenA.x + mWallScreen.width,
                mPointWallScreenA.y + mWallScreen.height);
        pointFsVirtualScreenRect[3].set(mPointWallScreenA.x + mWallScreen.width,
                mPointWallScreenA.y);

        return pointFsVirtualScreenRect;
    }

    private final PointF[] pointFsVirtualScreen = new PointF[]{new PointF(), new PointF(),
            new PointF(), new PointF()};

    public PointF[] calculationVirtualScreen() {

        pointFsVirtualScreen[0].set(mWallPointA.x - mWallPhoneScreenRate.x * mPhonePointA.x,
                mWallPointA.y - mWallPhoneScreenRate.y * mPhonePointA.y);
        pointFsVirtualScreen[1].set(mWallPointB.x - mWallPhoneScreenRate.x * mPhonePointB.x,
                mWallPointB.y - mWallPhoneScreenRate.y * mPhonePointB.y);
        pointFsVirtualScreen[2].set(mWallPointC.x - mWallPhoneScreenRate.x * mPhonePointC.x,
                mWallPointC.y - mWallPhoneScreenRate.y * mPhonePointC.y);
        pointFsVirtualScreen[3].set(mWallPointD.x - mWallPhoneScreenRate.x * mPhonePointD.x,
                mWallPointD.y - mWallPhoneScreenRate.y * mPhonePointD.y);

        return pointFsVirtualScreen;
    }

    private final PointF[] pointFsVertexInScreen = new PointF[]{new PointF(), new PointF(),
            new PointF(), new PointF()};

    // 转换墙上的校准点在手机屏幕上的位置
    public PointF[] calculationVertexInScreen() {
        calculationInScreenNoRate(mWallPointA, pointFsVertexInScreen[0]);
        calculationInScreenNoRate(mWallPointB, pointFsVertexInScreen[1]);
        calculationInScreenNoRate(mWallPointC, pointFsVertexInScreen[2]);
        calculationInScreenNoRate(mWallPointD, pointFsVertexInScreen[3]);

//        calculationInScreen(mWallPointA, pointFsVertexInScreen[0]);
//        calculationInScreen(mWallPointB, pointFsVertexInScreen[1]);
//        calculationInScreen(mWallPointC, pointFsVertexInScreen[2]);
//        calculationInScreen(mWallPointD, pointFsVertexInScreen[3]);
//        float angle = MathUtils.angle(pointFsVertexInScreen[0], pointFsVertexInScreen[1], mPhonePointB);
//        float angle1 = MathUtils.angle(pointFsVertexInScreen[0], pointFsVertexInScreen[2], mPhonePointC);
//        float angle2 = MathUtils.angle(pointFsVertexInScreen[0], pointFsVertexInScreen[3], mPhonePointD);
//        InclinationAngle = (angle + angle1 + angle2) / 3;
//        MathUtils.rotate(pointFsVertexInScreen[0], pointFsVertexInScreen[1], InclinationAngle);
//        MathUtils.rotate(pointFsVertexInScreen[0], pointFsVertexInScreen[2], InclinationAngle);
//        MathUtils.rotate(pointFsVertexInScreen[0], pointFsVertexInScreen[3], InclinationAngle);

        Tlog.v(TAG_SIN, " aa: " + pointFsVertexInScreen[0].toString());
        Tlog.v(TAG_SIN, " bb: " + pointFsVertexInScreen[1].toString());
        Tlog.v(TAG_SIN, " cc: " + pointFsVertexInScreen[2].toString());
        Tlog.v(TAG_SIN, " dd: " + pointFsVertexInScreen[3].toString());

        return pointFsVertexInScreen;
    }

    public void calculationInScreen(PointF wallP, PointF screenP) {
        calculationInScreenNoRate(wallP, screenP);
        MathUtils.rotate(pointFsVertexInScreen[0], screenP, InclinationAngle);
    }

    private void calculationInScreenNoRate(PointF wallP, PointF screenP) {
        screenP.x = (wallP.x - mPointWallScreenA.x) / mWallPhoneScreenRate.x;
        screenP.y = (wallP.y - mPointWallScreenA.y) / mWallPhoneScreenRate.y;
    }

}
