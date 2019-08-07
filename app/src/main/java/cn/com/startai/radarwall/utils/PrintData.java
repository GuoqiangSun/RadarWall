package cn.com.startai.radarwall.utils;

import cn.com.swain.baselib.log.Tlog;

/**
 * author Guoqiang_Sun
 * date 2019/7/30
 * desc
 */
public class PrintData {

    public static void print(String TAG, char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            Tlog.v(TAG, " index(" + i + ")=" + (int) chars[i]);
        }
    }

    public static void println(String TAG, char[] chars, String head) {
        StringBuilder sb = new StringBuilder(320 * 50);
        sb.append(head);
        for (int i = 0; i < chars.length; i++) {
            sb.append("(").append(i).append(")=").append((int) chars[i]).append(" ");
//            if (i == chars.length / 2) {
//                Tlog.v(TAG, sb.toString());
//                sb = new StringBuilder(320 / 2 * 50);
//                sb.append(head);
//            }
        }
        Tlog.v(TAG, sb.toString());
    }

    public static void println(String TAG, int[] chars, String head) {
        StringBuilder sb = new StringBuilder(320 / 2 * 50);
        sb.append(head);
        for (int i = 0; i < chars.length; i++) {
            sb.append("(").append(i).append(")=").append(chars[i]).append(" ");
//            if (i == chars.length / 2) {
//                Tlog.v(TAG, sb.toString());
//                sb = new StringBuilder(320 / 2 * 50);
//                sb.append(head);
//            }
        }
        Tlog.v(TAG, sb.toString());
    }

}
