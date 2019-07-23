package cn.com.startai.radarwall;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * author Guoqiang_Sun
 * date 2019/7/23
 * desc
 */
public class JavaTest {

    public static void main(String[] args) {
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
