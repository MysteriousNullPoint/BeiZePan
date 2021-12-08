package com.example.cupboard;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class Socket {
    /**
     * 发送消息给门禁
     * xx000000 录入标签ID
     * xx000001 录入管理ID
     * xx000002 确认门禁标签
     * xx000003 打开门禁摄像头
     * xx000004 取消本次门禁操作
     * xx000005 初始化门禁数据
     * xx000006 关闭门禁摄像头
     * xx000007 发送门禁ADMIN ID
     * xx000008 删除标签ID
     * xx000009 删除管理员ID
     */
    public void sendData(List<String> messageList){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建客户端Socket，指定服务器的IP地址和端口
                    //创建数据包
                    java.net.Socket socket = new java.net.Socket("192.168.43.105",8000);
                    //获取输出流，向服务器发送数据
                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    for(String message : messageList){
                        pw.write(message);
                        pw.flush();}
                    //关闭输出流
                    socket.shutdownOutput();

                    //关闭资源
                    pw.close();
                    os.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
