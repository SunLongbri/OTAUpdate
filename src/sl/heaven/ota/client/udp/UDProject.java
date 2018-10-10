package sl.heaven.ota.client.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDProject {
    public static void main(String args[]) {
        //传入0表示让操作系统分配一个端口号
        try (
                DatagramSocket socket = new DatagramSocket(7878)) {
            InetAddress host = InetAddress.getByName("255.255.255.255");
            //指定包要发送的目的地
            String param = "Are You Espressif IOT Smart Device?";
            byte[] buffer = param.getBytes();
            DatagramPacket request = new DatagramPacket(buffer, 0, buffer.length, host, 1025);
            //为接受的数据包创建空间
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
            socket.send(request);
            System.out.println("====> send");


            socket.receive(response);
            System.out.println("====> receive");


            String result = new String(response.getData(), 0, response.getLength(), "ASCII");
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
