package cn.com.startai.radarwall;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * author Guoqiang_Sun
 * date 2019/7/23
 * desc
 */
public class JavaTest {

    public static void main(String[] args) {
//        socket();
//        reserveBuf();
//        maxIndex();
//        sort();
//        mo();


//        double sqrt = Math.sqrt(Math.pow(3, 2) + Math.pow(4, 2));
//        System.out.println(sqrt);

//        count();

        recalculation(10, 0, 30);
    }

    private static void recalculation(float bx, float by, double d) {

        float x0 = bx * (float) cos(d)
                - by * (float) sin(d);

        float y0 = bx * (float) sin(d)
                + by * (float) cos(d);

        System.out.println("bx:" + bx + " by:" + by + " d:" + d);
        System.out.println(" x0:" + x0 + " y0:" + y0);

        PF[] pfs = new PF[]{new PF(0, 0), new PF(bx, by), new PF(x0, y0)};
        float angle;
        angle = Angle(pfs[0], pfs[1], pfs[2]);
        System.out.println(" angle:" + angle);
        angle = Angle(pfs[0], pfs[2], pfs[1]);
        System.out.println(" angle:" + angle);
        angle = Angle(pfs[1], pfs[0], pfs[2]);
        System.out.println(" angle:" + angle);
        angle = Angle(pfs[1], pfs[2], pfs[0]);
        System.out.println(" angle:" + angle);
        angle = Angle(pfs[2], pfs[0], pfs[1]);
        System.out.println(" angle:" + angle);
        angle = Angle(pfs[2], pfs[1], pfs[0]);
        System.out.println(" angle:" + angle);
    }

    public static double cos(double degree) {
        return Math.cos(degree / 180 * Math.PI);
    }

    public static double sin(double degree) {
        return Math.sin(degree / 180 * Math.PI);
    }


    public static float Angle(PF cen, PF first, PF second) {
        float dx1, dx2, dy1, dy2;
        float angle;

        dx1 = first.x - cen.x;
        dy1 = first.y - cen.y;

        dx2 = second.x - cen.x;

        dy2 = second.y - cen.y;

        float c = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1) * (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);

        if (c == 0) return -1;

        angle = (float) (Math.acos((dx1 * dx2 + dy1 * dy2) / c) / Math.PI * 180);


        return angle;
    }

    private static class PF {

        private PF() {
        }

        private PF(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x;
        public float y;
    }

    private static void count() {
        int point = 0;
        int fps = 16;
        int count = 0;
        for (int i = 0; i < 50; i++) {
            System.out.println("i:" + i);
            System.out.println("point:" + point);
            if (++point <= fps) {
                System.out.println("++point:" + point);
            } else {
                System.out.println("++point:" + point);
                count++;
                if (point >= fps * 2) {
                    System.err
                            .println("point>=bsize*2 " + point
                                    + " point-bsize:" + (point - fps)
                                    + " count:" + count);
                    break;
                }
            }
        }
    }

    private static void mo() {
        for (int i = 0; i < 4; i++) {
            System.out.println(" i % 4 == " + i % 4);
            System.out.println((i % 4) * 2);
            System.out.println((i % 4) * 2 + 1);
        }
    }

    private static void sort() {
        P[] pp = new P[4];
        pp[0] = new P(1, 1);
        pp[1] = new P(1, 2);
        pp[2] = new P(2, 2);
        pp[3] = new P(2, 1);
        Arrays.sort(pp, new Comparator<P>() {
            @Override
            public int compare(P o1, P o2) {
                return Float.compare(o1.x * o1.y, o2.x * o2.y);
            }
        });
        for (int i = 0; i < 4; i++) {
            System.out.println(pp[i].toString());
        }
    }

    private static class P {
        public P() {
        }

        public P(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x;
        public float y;

        @Override
        public String toString() {
            return "-x:" + x + " y:" + y;
        }
    }

    static int SIZE = 30;

    /**
     * 求buf最大值的下标
     */
    private static void maxIndex() {
        char[] buf = new char[SIZE];
        for (int i = 0; i < SIZE; i++) {
            buf[i] = (char) i;
        }

        for (int i = 0; i < SIZE; i++) {
            if (i == 3) {
                int u = 2;
                while (u > 0) {
                    i++;
                    int mBufDis = (int) buf[i];
                    if (mBufDis < 6) {
                        u++;
                        if (u > 2) {
                            u = 2;
                        }
                    } else {
                        u--;
                    }
                }

                continue;
            }
            System.out.println((int) buf[i]);
        }
    }


    public static void reserveBuf() {
        char[] chars = new char[3];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) i;
        }
        reserveBuf(chars);
        for (int i = 0; i < chars.length; i++) {
            System.out.println(i + " : " + (int) chars[i]);
        }
    }

    private static void reserveBuf(char[] chars) {
        char t;
        for (int i = 0; i < chars.length / 2; i++) {
            t = chars[i];
            chars[i] = chars[chars.length - 1 - i];
            chars[chars.length - 1 - i] = t;
        }
    }


    private static void socket() {
        //建立udp的服务 ，并且要监听一个端口。
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(4010);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }


        //准备空的数据包用于存放数据。
        byte[] buf = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length); // 1024
        //调用udp的服务接收数据
        while (true) {
            try {
                //receive是一个阻塞型的方法，没有接收到数据包之前会一直等待。 数据实际上就是存储到了byte的自己数组中了。
                socket.receive(datagramPacket);
                int length = datagramPacket.getLength();
                for (int i = 0; i < length; i++) {
                    System.out.print(Integer.toHexString(buf[i]) + " ,");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
