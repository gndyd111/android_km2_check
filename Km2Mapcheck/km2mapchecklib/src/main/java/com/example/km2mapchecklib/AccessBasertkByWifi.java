package com.example.km2mapchecklib;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android_serialport_api.SerialPort;

public class AccessBasertkByWifi  implements InAccessBaseRtk{
    private static int mResult = 1;
    private Boolean isReadData = true;
    private String mIp;
    private int mUdpPort;
    private Context context;

    private SerialPort mSerialPort;
    private String mPort = "/dev/ttyS0";
    private int mBaud = 115200;
    private OutputStream mOut;

    private DatagramSocket socket = null;
    //private int mRecvPort = 6001;
    //private int mSendPort = 6003;
    private String mSendCmd = "";

    public AccessBasertkByWifi(String ip, String port, Context cnt)
    {
        mIp = ip;
        mUdpPort = Integer.parseInt(port);
        context = cnt;
        try
        {
            socket = new DatagramSocket(6003);
            mSendCmd = String.format("#uLog,%s:%s\r\n",GlobalConfigVar.getInstance().localIp,"6003");
        }
        catch (Exception e)
        {}
    }
    //@Override
    public int ReadBaseRtkData() {
        try
        {
            mSerialPort = new SerialPort(new File(mPort), mBaud, 0);
            mOut = mSerialPort.getOutputStream();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] sendBytes = mSendCmd.getBytes();

                        InetAddress addr = InetAddress.getByName(mIp);
                        DatagramPacket dp = new DatagramPacket(sendBytes, sendBytes.length, addr, mUdpPort);
                        while (isReadData) {
                            socket.send(dp);
                            Thread.sleep(20000);
                        }
                    }
                    catch (Exception e)
                    {}
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        while(isReadData) {
                            Thread.sleep(1000);
                            byte[] message = new byte[1024];
                            DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                            socket.receive(datagramPacket);

                            String strMsg = new String(datagramPacket.getData()).trim();
                            int len = strMsg.length();
                            /*String bt = GlobalConfigVar.getInstance().byte2hex(datagramPacket.getData());
                            String bt2 = GlobalConfigVar.getInstance().str2HexStr(strMsg);
                            int len2 = bt.length();
                            int len3 = bt2.length();*/
                            mOut.write(datagramPacket.getData());
                            mOut.flush();
                        }
                    }
                    catch (Exception e)
                    {}
                    finally {
                        Close();
                    }
                }
            }).start();
        }
        catch (Exception e)
        {}
        return mResult;
    }

    @Override
    public void StopRead()
    {
        isReadData = false;
        mResult = 1;
        Close();
    }

    private void Close()
    {
        try {
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }

            if (mOut != null) {
                mOut.close();
            }

            if(socket !=null)
                socket.close();
        }
        catch (Exception e)
        {
            Log.e("CloseBaseRtkErrorVPN", e.getLocalizedMessage(), e);
        }
    }
}
