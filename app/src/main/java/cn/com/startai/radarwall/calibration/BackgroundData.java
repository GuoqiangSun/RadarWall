package cn.com.startai.radarwall.calibration;

import java.io.Serializable;

import cn.com.startai.radarwall.MainActivity;

/**
 * author Guoqiang_Sun
 * date 2019/7/30
 * desc
 */
public class BackgroundData implements Serializable {

    private final int maxNum = MainActivity.MAX_DISTANCE;
    private final int size = MainActivity.FRAME_DATA_SIZE;
    private final int fps = MainActivity.MAX_FPS;
    private final int fpsCycle = 3;

    private char[] avgBg = new char[size];
    private int[] SUM = new int[size];
    private int[] MIN = new int[size];
    private int[] MAX = new int[size];
    private int point = 0;

    public char[] getAvgBg() {
        return avgBg;
    }


    public BackgroundData() {
        for (int i = 0; i < size; i++) {
            MIN[i] = Integer.MAX_VALUE;
            MAX[i] = Integer.MIN_VALUE;
            SUM[i] = 0;
        }
    }

    private final int NO_COUNT = fps / 2;

    public boolean setOneFrame(char[] chars) {
        if (++point <= NO_COUNT) { // 前面的不统计
            return false;
        }

        for (int j = 0; j < size; j++) {
            int aChar = chars[j];
            if (aChar < MIN[j]) {
                MIN[j] = aChar;
            }
            if (aChar > MAX[j]) {
                MAX[j] = aChar;
            }
            SUM[j] += aChar;

            if (point > (NO_COUNT + 2)) {
                avgBg[j] = (char) ((SUM[j] - MIN[j] - MAX[j]) / (point - NO_COUNT - 2));
            }

        }

        return countBGFinish();
    }


    public boolean countBGFinish() {
        return point > (fpsCycle * fps + NO_COUNT);
    }


}
