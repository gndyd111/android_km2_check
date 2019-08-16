package com.example.km2mapchecklib;

import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class AccessBasertkBySerialPort implements InAccessBaseRtk{
    private String mPort;
    private int mBaud;
    private InputStream mInput;

    private SerialPort mSerialPort;
    private Boolean isReadData = true;
    private static int mResult = 1;

    private SerialPort mSerialPort2;
    private String mPort2 = "/dev/ttyS0";
    private int mBaud2 = 115200;
    private OutputStream mOut2;

    public AccessBasertkBySerialPort(String port, int baud)
    {
        mPort = "/dev/" + port;//ttyS6
        mBaud = baud;
    }

    //@Override
    public int ReadBaseRtkData()
    {
        try {
            mSerialPort = new SerialPort(new File(mPort), mBaud, 0);
            mInput = mSerialPort.getInputStream();

            mSerialPort2 = new SerialPort(new File(mPort2), mBaud2, 0);
            mOut2 = mSerialPort2.getOutputStream();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    /*mSerialPort = new SerialPort(new File(mPort), mBaud, 0);
                    mInput = mSerialPort.getInputStream();

                    mSerialPort2 = new SerialPort(new File(mPort2),mBaud2,0);
                    mOut2 = mSerialPort2.getOutputStream();*/
                    // 获取数据
                   int readLen = 0;
                   while (isReadData)
                   {
                       byte[] readBytes = new byte[1024];
                       readLen = mInput.read(readBytes);
                       if(readLen > 0)
                       {
                           mOut2.write(readBytes);
                           mOut2.flush();
                       }
                       Thread.sleep(1000);
                   }
                }
                catch(Exception e)
                {
                    mResult = 0;
                }
                finally {
                    Close();
                }
            }
        }).start();

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
            if (mInput != null) {
                mInput.close();
            }

            if (mSerialPort2 != null) {
                mSerialPort2.close();
                mSerialPort2 = null;
            }
            if (mOut2 != null) {
                mOut2.close();
            }
        }
        catch (Exception e)
        {
            Log.e("CloseBaseRtkError", e.getLocalizedMessage(), e);
        }
    }
}
