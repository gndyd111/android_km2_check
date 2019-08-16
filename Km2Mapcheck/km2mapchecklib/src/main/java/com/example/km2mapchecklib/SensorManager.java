package com.example.km2mapchecklib;

import android.os.Message;

import java.util.Observable;

enum ReadType{
    ReadByGpsBoard,
    ReadByTrackFile
}

public class SensorManager extends Observable {

    private android.os.Handler mHandler;
    private ReadType readType = ReadType.ReadByGpsBoard;

    private SensorManager(){}

    private static class SensorManagerHolder{
        private final static SensorManager instance = new SensorManager();
    }

    public static SensorManager getInstance(){return SensorManagerHolder.instance;}

    public boolean Open(String gpsPort, int gpsBaud, android.os.Handler handler)
    {
        try {
            readType = ReadType.ReadByGpsBoard;
            ReadGpsData.getInstance().InitParam(gpsPort, gpsBaud);
            ReadGpsData.getInstance().start();

            mHandler = handler;

            Thread.sleep(100);
        }
        catch (Exception e)
        {
            return false;
        }

        if(!ReadGpsData.getInstance().isValidData())
            return false;

        return true;
    }

    public boolean Open(String ip , String port, android.os.Handler handler)
    {
        try {
            readType = ReadType.ReadByTrackFile;
            String str = ConfigFile.getInstance().readMapConfig.get("TrackType");
            if(ConfigFile.getInstance().readMapConfig.get("TrackType").equals("1")) {//调试版
                ReadTrackGpsDataForDebug.getInstance().InitParam(ip, port);
                ReadTrackGpsDataForDebug.getInstance().start();
            }
            else if(ConfigFile.getInstance().readMapConfig.get("TrackType").equals("2")) {//无锡所检测
                ReadTrackGpsDataForCheck.getInstance().InitParam(ip, port);
                ReadTrackGpsDataForCheck.getInstance().start();
            }

            mHandler = handler;

            Thread.sleep(100);
        }
        catch (Exception e)
        {
            return false;
        }

        if(ConfigFile.getInstance().readMapConfig.get("TrackType").equals("1")) {//调试版
            if (!ReadTrackGpsDataForDebug.getInstance().isValidData())
                return false;
        }
        else if(ConfigFile.getInstance().readMapConfig.get("TrackType").equals("2")) {//无锡所检测
            if (!ReadTrackGpsDataForCheck.getInstance().isValidData())
                return false;
        }

        return true;
    }

    public void updateLocation(LocationData locationData)
    {
        //setChanged();
        //notifyObservers(locationData);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = locationData;
        mHandler.sendMessage(msg);
    }

    public void Close()
    {
        if(readType == ReadType.ReadByGpsBoard)
            ReadGpsData.getInstance().StopReadGps();
        else if(readType == ReadType.ReadByTrackFile) {
            if(ConfigFile.getInstance().readMapConfig.get("TrackType") == "1")
                ReadTrackGpsDataForDebug.getInstance().StopReadGps();
            else if(ConfigFile.getInstance().readMapConfig.get("TrackType") == "2")
                ReadTrackGpsDataForCheck.getInstance().StopReadGps();
        }
    }
}
