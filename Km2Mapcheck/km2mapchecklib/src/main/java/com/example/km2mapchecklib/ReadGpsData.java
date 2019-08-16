package com.example.km2mapchecklib;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import android.os.Handler;
import android_serialport_api.SerialPort;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.os.Build.VERSION_CODES.N;

public class ReadGpsData extends Thread{
    public SerialPort mSerialPort = null;
    private String devPort = null;
    private int devBaud = 0;
    private InputStream mInput = null;
    public OutputStream mOut = null;

    private int buffersize = 10240;
    private int maxPacksize = 3072;
    private byte[]mBuffer = new byte[buffersize + 1];
    private int nBuffLen = 0;
    private Boolean isReadData = true;
    private Boolean isValid = false;
    private final int RECEIVE_RTKDATA = 1;
    private File file = null;
    private String pashr = null;

    private SerialPort mSerialPort2;
    private String mPort2 = "/dev/ttyS4";
    private OutputStream mOut2;

    private LocationData locationData = new LocationData();

    private ReadGpsData()
    {}

    /*private static class ReadGpsDataHolder{
        private final static  ReadGpsData instance = new ReadGpsData();
    }*/

    private final static  ReadGpsData instance = new ReadGpsData();

    public static ReadGpsData getInstance(){
        return instance;
    }

    public void InitParam(String port, int baud)
    {
        devBaud = baud;
        devPort = "/dev/" + port;
        isReadData = true;
        isValid = false;
        powerOn("1");
        SystemClock.sleep(1000);

        try {
            mSerialPort = new SerialPort(new File(devPort), devBaud, 0);
            mInput = mSerialPort.getInputStream();
            mOut = mSerialPort.getOutputStream();

            mSerialPort2 = new SerialPort(new File(mPort2), 115200, 0);
            mOut2 = mSerialPort2.getOutputStream();
        }
        catch (Exception e){}
    }

    public Boolean IsSerialPortInit()
    {
        if(mSerialPort == null)
            return false;

        return  true;
    }

    private void sendData(String cmd) {
        try {
            String command = cmd + "\r\n";
            mOut.write(command.getBytes());
            mOut.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //下电(0) 默认   上电(1)
    public void powerOn(String value) {
        String powerPath = "/sys/class/misc/sunxi-gps/rf-ctrl/gnss_pwren_state";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(powerPath)));
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isValidData()
    {
        return isValid;
    }

    public void StopReadGps()
    {
        isReadData = false;
        Close();
    }

    @Override
    public void run()
    {
        super.run();

        if(!devPort.isEmpty() && devBaud > 0)
            Open();
    }

    private void Open()
    {
        try {
            //mSerialPort = new SerialPort(new File(devPort), devBaud, 0);
            //mInput = mSerialPort.getInputStream();
            //mOut = mSerialPort.getOutputStream();

            sendData("unlog");
            sendData("GPGGA com3 0.2");
            //sendData("GPGSA com3 0.2");
            //sendData("GPGSV com3 0.2");
            sendData("GPRMC com3 0.2");
            sendData("GPHDT com3 0.2");
            sendData("Gptra com3 onchanged");

            SystemClock.sleep(1000);

            int readLen = 0;
            while(isReadData)
            {
                byte[] readBytes = new byte[maxPacksize];//1024
                readLen = mInput.read(readBytes);
                //String s = new String(readBytes);
                isValid = true;

                if(readLen > 0) {
                    if (nBuffLen + readLen > buffersize) {
                        int newStartPos = buffersize - maxPacksize;//buffersize 8192  7168

                        byte[]tempBuffer = new byte[maxPacksize];
                        System.arraycopy(mBuffer,newStartPos,tempBuffer,0,maxPacksize);
                        mBuffer = new byte[buffersize + 1];
                        System.arraycopy(tempBuffer,0,mBuffer,0,maxPacksize);
                        //System.arraycopy(mBuffer, newStartPos, mBuffer, 0, maxPacksize);
                        nBuffLen = maxPacksize;
                    }
                        System.arraycopy(readBytes, 0, mBuffer, nBuffLen /*+ readLen*/, readLen);

                    nBuffLen += readLen;
                    if(nBuffLen > 100)
                    {
                        while(SearchPack(nBuffLen))
                        {;}
                    }
                }
                //else
                    //isValid = false;
            }
        }
        catch  (Exception e) {
            Log.e("ReadGpsError", e.getLocalizedMessage(), e);
            isValid = false;
        }
        finally {
            Close();
        }
    }

    private Boolean SearchPack(int buffLen)
    {
        String gpsString = new String(mBuffer);

        int gpggaIndex = gpsString.indexOf("$GPGGA");
        int gprmcIndex = gpsString.indexOf("$GPRMC");
        int gntraIndex = gpsString.indexOf("$GNTRA");

        if(gpggaIndex < 0 || gprmcIndex < 0 || gntraIndex < 0)
            return false;

        //int endIndexOfGntra = gpsString.indexOf("\r\n", gntraIndex);
        int endIndexOfGntra = -1;
        int xhIndexOfGntra = -1;
        int endIndexOfGpgga = gpsString.indexOf("\r\n", gpggaIndex);
        String jyfOfGntra = null;

        if(endIndexOfGpgga >= 0)
        {
            int endIndexOfGprmc = gpsString.indexOf("\r\n",endIndexOfGpgga);
            if(endIndexOfGprmc >= 0)
            {
                //endIndexOfGntra = gpsString.indexOf("\r\n", endIndexOfGprmc);
                int startIndexOfGntra = gpsString.indexOf("$GNTRA",endIndexOfGprmc);//保证了该条GNTRA语句在其他两个语句的后面
                if(startIndexOfGntra >= 0)
                {
                    xhIndexOfGntra = gpsString.indexOf("*", startIndexOfGntra);
                    if(xhIndexOfGntra > 0)
                    {
                        jyfOfGntra = gpsString.substring(xhIndexOfGntra + 1);//找到GNTRA语句中星号后面的剩余字符串
                        jyfOfGntra = jyfOfGntra.trim();
                        if(jyfOfGntra.length() >= 2)//说明有校验符  即找到完整的三条语句
                        {
                            mBuffer = new byte[buffersize + 1];
                            //处理剩余数据
                            String leftString = gpsString.substring(xhIndexOfGntra + 3);
                            int nLeftLen = buffLen - (xhIndexOfGntra + 3);//剩余有效字符个数
                            if(nLeftLen > 0)
                            {
                                String validLeftString = leftString.substring(0, nLeftLen);
                                byte[] leftBytes = validLeftString.getBytes();
                                System.arraycopy(leftBytes, 0, mBuffer, 0, leftBytes.length);
                                nBuffLen = leftBytes.length;
                            }
                            else if(nLeftLen == 0)//正好后面没有剩余数据了
                            {
                                nBuffLen = 0;
                            }
                        }
                        else
                            return false;
                    }
                    else {
                        String leftString = gpsString.substring(xhIndexOfGntra + 3);
                        return false;
                    }
                }
            }
        }

        String subGpsString = gpsString.substring(gpggaIndex, xhIndexOfGntra + 3);
        String gpggaStr = null, gprmcStr = null, gntraStr = null;
        String[] arrGpsString = subGpsString.split("\r\n");
        for(int i = 0; i < arrGpsString.length; i++)
        {
            if(arrGpsString[i].contains("$GPGGA")) {
                if(gpggaStr == null)
                    gpggaStr = arrGpsString[i];
            }
            if(arrGpsString[i].contains("$GPRMC")) {
                if(gprmcStr == null)
                    gprmcStr = arrGpsString[i];
            }
            if(arrGpsString[i].contains("$GNTRA")) {
                if(gntraStr == null)
                    gntraStr = arrGpsString[i];
            }
        }

        ProcessPack(gpggaStr, gprmcStr, gntraStr);
        if(gpggaStr == null || gprmcStr == null || gntraStr == null)
            return false;

        try {
            byte[] checkStr = subGpsString.getBytes();
            mOut2.write(checkStr);
            mOut2.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RecordTrackToFile(subGpsString);

        return true;
    }

    private void ProcessPack(String gpggaStr, String gprmcStr, String gntraStr)
    {
        try {
            pashr = "$PASHR";
            if (Verify(gpggaStr) && Verify(gprmcStr) && Verify(gntraStr)) {
                locationData.Reset();
                if (!ParseGpgga(gpggaStr))
                    return;

                if (!ParseGprmc(gprmcStr))
                    return;

                if (!ParseGntra(gntraStr))
                    return;

                if (locationData.quality != 0) {
                    SensorManager.getInstance().updateLocation(locationData);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Boolean Verify(String str)
    {
        if(!str.contains("*"))
            return false;

        int index = str.indexOf("*");
        String jym = str.substring(index + 1, str.length());

        int checkNum = Integer.valueOf(jym, 16);
        char c = str.charAt(1);
        int xor = c & 0xff;
        for (int i = 2; i < index; i++)
            xor ^= str.charAt(i) & 0xff;

        return (xor == checkNum);
    }

    private Boolean ParseGpgga(String gpgga)
    {
        String[] arrGpgga = gpgga.split(",");

        int index1 = gpgga.indexOf(",");
        if (index1 < 0) return false;
        if (arrGpgga.length < 10) return false;

        //时间
        pashr += "," + arrGpgga[1];
        double d = Double.parseDouble(arrGpgga[1]);
        int iTime = (int) d;
        locationData.time.wHour = iTime / 10000;
        locationData.time.wMinute = iTime / 100 % 100;
        locationData.time.wSecond = iTime % 100;
        locationData.time.wMilliseconds = (int) ((d - iTime) * 1000 + 0.5);

        //纬度
        double latitude = 0;
        latitude = getCoor(Double.parseDouble(arrGpgga[2]));

        //南纬为-
        if (arrGpgga[3].equals("S")) {
            latitude = -latitude;
        }
        locationData.latitude = latitude;

        //经度
        double longitude = 0;
        longitude = getCoor(Double.parseDouble(arrGpgga[4]));

        //西经为-
        if (arrGpgga[5].equals("W")) {
            longitude = -longitude;
        }
        locationData.longitude = longitude;

        //定位状态
        locationData.quality = Integer.parseInt(arrGpgga[6]);

        //卫星个数 arrGpgga[7]

        //水平精度因子 arrGpgga[8]

        //海拔高度
        locationData.altitude = Double.parseDouble(arrGpgga[9]);

        return true;
    }

    private Boolean ParseGprmc(String gprmc)
    {
        String[] arrGprmc = gprmc.split(",");

        if(arrGprmc.length < 10)return false;
        //速度
        locationData.speed = Double.parseDouble(arrGprmc[7]) * 1.852;

        //日期
        int date = Integer.parseInt(arrGprmc[9]);
        locationData.time.wDay = date / 10000;
        locationData.time.wMonth = date / 100 % 100;
        locationData.time.wYear = date % 100 + 2000;

        return true;
    }

    private Boolean ParseGntra(String gntra)
    {
        //航向
        String[] arrGntra = gntra.split(",");
        if(arrGntra.length < 8) return false;
        //航向
        pashr += "," + arrGntra[2] + ",T";
        locationData.heading = Double.parseDouble(arrGntra[2]);

        //横滚
        pashr += "," + arrGntra[4];
        locationData.roll = Double.parseDouble(arrGntra[4]);

        //俯仰
        pashr += "," + arrGntra[3] + ",-0.01,0.502,0.502,0.175,1*1B";
        locationData.pitch = Double.parseDouble(arrGntra[3]);

        return true;
    }

    private void RecordTrackToFile(String trackStr)
    {
        try {
            //还要把GNTRA数据转成PASHR
            int indx = trackStr.indexOf("$GNTRA");
            trackStr = trackStr.substring(0,indx);
            trackStr += pashr;

            trackStr = trackStr + "\r\n";
            if (GlobalConfigVar.getInstance().mRecordTrack
                    && GlobalConfigVar.getInstance().mTrackFilename != null)//写入轨迹
            {
                if (file == null)
                    file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), GlobalConfigVar.getInstance().mTrackFilename);

                FileOutputStream outputStream = new FileOutputStream(file,true);
                outputStream.write(trackStr.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            if(GlobalConfigVar.getInstance().mRecordTrack == false)
            {
                file = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
            if (mOut != null) {
                mOut.close();
            }

            if (mSerialPort2 != null) {
                mSerialPort2.close();
                mSerialPort2 = null;

                if (mOut2 != null) {
                    mOut2.close();
                }
            }
        }
        catch (Exception e)
        {
            Log.e("CloseGpsError", e.getLocalizedMessage(), e);
        }
    }

    private double getCoor(double data)
    {
        int value = (int)(data/100);
        return value + (data - value*100)/60;
    }

    /*public void writeToStream(byte[] bytes) {
        if (bytes.length > 0) {
            if (mOut != null) {
                try {
                    //mOut = mSerialPort.getOutputStream();
                    mOut.write(bytes);
                    //String str = "123";
                    //mOut.write(str.getBytes());
                    mOut.flush();
                } catch (IOException e) {
                    System.out.println("error" + e.getMessage());
                    try {
                        mOut.close();
                    } catch (IOException e1) {
                        System.out.println("error" + e1.getMessage());
                    }
                }
            }
        }
    }*/
}
