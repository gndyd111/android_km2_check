package com.example.km2mapchecklib;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReadTrackGpsDataForDebug extends Thread{
    private static int mResult = 1;
    private String mIp;
    private int mUdpPort;
    private Boolean isReadData = true;
    private DatagramSocket socket = null;
    private int maxPacksize = 1024;
    private int nBuffLen = 0;
    private int buffersize = 8192;
    private byte[]mBuffer = new byte[buffersize + 1];
    private Boolean isValid = false;
    private LocationData locationData = new LocationData();

    private ReadTrackGpsDataForDebug()
    {
    }

    private final static ReadTrackGpsDataForDebug instance = new ReadTrackGpsDataForDebug();

    public static ReadTrackGpsDataForDebug getInstance(){
        return instance;
    }

    public void InitParam(String ip, String port)
    {
        isReadData = true;
        isValid = false;
        mIp = ip;
        mUdpPort = Integer.parseInt(port);
        try
        {
            socket = new DatagramSocket(mUdpPort);
        }
        catch (Exception e)
        {}
    }

    public Boolean isValidData()
    {
        return isValid;
    }

    @Override
    public void run()
    {
        super.run();

        if(mUdpPort > 0)
            Open();
    }

    private void Open() {
        try {
            while (isReadData) {
                byte[] recv = new byte[maxPacksize];
                DatagramPacket datagramPacket = new DatagramPacket(recv, recv.length);
                socket.receive(datagramPacket);
                isValid = true;

                if(nBuffLen + datagramPacket.getLength() > buffersize)
                {
                    int newStartPos = buffersize - maxPacksize;

                    byte[]tempBuffer = new byte[maxPacksize];
                    System.arraycopy(mBuffer,newStartPos,tempBuffer,0,maxPacksize);
                    mBuffer = new byte[buffersize + 1];
                    System.arraycopy(tempBuffer,0,mBuffer,0,maxPacksize);

                    nBuffLen = maxPacksize;
                }
                System.arraycopy(recv, 0, mBuffer, nBuffLen , datagramPacket.getLength());

                nBuffLen += datagramPacket.getLength();
                if(nBuffLen > 100)
                {
                    while(SearchPack(nBuffLen))
                    {;}
                }
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

    private Boolean SearchPack(int buffLen) {
        String gpsString = new String(mBuffer);

        int gpggaIndex = gpsString.indexOf("$GPGGA");
        int gprmcIndex = gpsString.indexOf("$GPRMC");
        int pashrIndex = gpsString.indexOf("$PASHR");

        if(gpggaIndex < 0 || gprmcIndex < 0 || pashrIndex < 0)
            return false;

        int xhIndexOfPashr = -1;
        int endIndexOfGpgga = gpsString.indexOf("\r\n", gpggaIndex);
        String jyfOfPashr = null;

        if(endIndexOfGpgga >= 0)
        {
            int endIndexOfGprmc = gpsString.indexOf("\r\n",endIndexOfGpgga);
            if(endIndexOfGprmc >= 0)
            {
                int startIndexOfPashr = gpsString.indexOf("$PASHR",endIndexOfGprmc);
                if(startIndexOfPashr >= 0) {
                    xhIndexOfPashr = gpsString.indexOf("*", startIndexOfPashr);
                    if(xhIndexOfPashr > 0)
                    {
                        jyfOfPashr = gpsString.substring(xhIndexOfPashr + 1);//找到GNTRA语句中星号后面的剩余字符串
                        jyfOfPashr = jyfOfPashr.trim();
                        if(jyfOfPashr.length() >= 2)//说明有校验符  即找到完整的三条语句
                        {
                            mBuffer = new byte[buffersize + 1];
                            //处理剩余数据
                            String leftString = gpsString.substring(xhIndexOfPashr + 3);
                            int nLeftLen = buffLen - (xhIndexOfPashr + 3);//剩余有效字符个数
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
                        String leftString = gpsString.substring(xhIndexOfPashr + 3);
                        return false;
                    }
                }
            }
        }
        String subGpsString = gpsString.substring(gpggaIndex, xhIndexOfPashr + 3);
        String gpggaStr = null, gprmcStr = null, pashrStr = null;
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
            if(arrGpsString[i].contains("$PASHR")) {
                if(pashrStr == null)
                    pashrStr = arrGpsString[i];
            }
        }
        ProcessPack(gpggaStr, gprmcStr, pashrStr);
        if(gpggaStr == null || gprmcStr == null || pashrStr == null)
            return false;

        return true;
    }

    private void ProcessPack(String gpggaStr, String gprmcStr, String pashrStr)
    {
        try {
            if (Verify(gpggaStr) && Verify(gprmcStr) /*&& Verify(pashrStr)*/) {
                locationData.Reset();
                if (!ParseGpgga(gpggaStr))
                    return;

                if (!ParseGprmc(gprmcStr))
                    return;

                if (!ParsePashr(pashrStr))
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

    private Boolean ParseGpgga(String gpgga)
    {
        String[] arrGpgga = gpgga.split(",");

        int index1 = gpgga.indexOf(",");
        if (index1 < 0) return false;
        if (arrGpgga.length < 10) return false;
        //时间
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

    private Boolean ParsePashr(String pashr)
    {
        String[] arrPashr = pashr.split(",");
        if(arrPashr.length < 10)return false;

        //航向
        locationData.heading = Double.parseDouble(arrPashr[2]);
        //俯仰
        locationData.pitch = Double.parseDouble(arrPashr[5]);
        //横滚
        locationData.roll = Double.parseDouble(arrPashr[4]);

        return true;
    }

    private double getCoor(double data)
    {
        int value = (int)(data/100);
        return value + (data - value*100)/60;
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

    private void Close()
    {
        try {
            if(socket !=null)
                socket.close();
        }
        catch (Exception e)
        {
            Log.e("CloseBaseRtkErrorTrack", e.getLocalizedMessage(), e);
        }
    }

    public void StopReadGps()
    {
        isReadData = false;
        Close();
    }
}
