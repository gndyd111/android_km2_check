package com.example.km2mapchecklib;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.example.km2mapchecklib.CarModel;
import com.example.km2mapchecklib.InAccessBaseRtk;
import com.example.km2mapchecklib.MapManager;

public class Km2Entrance {

    private InAccessBaseRtk obj = null;

    private Km2Entrance(){}

    private static class Km2EntranceHolder{
        private final static Km2Entrance instance = new Km2Entrance();
    }

    public static Km2Entrance getInstance(){
        return Km2EntranceHolder.instance;
    }

    public Boolean ParseMap()
    {
        try {
            if (MapManager.getInstance().Load() == 0) {
                return false;
            }

            if(CarModel.getInstance().Init() == false) {
                return false;
            }
        }
        catch (Exception e){}
        return true;
    }

    public Boolean ReadRtk(Context context)
    {
        //GlobalConfigVar.getInstance().localIp = GlobalConfigVar.getInstance().getEthernetIp(context);
        String str = GlobalConfigVar.getInstance().localIp;
        int nRet = 0;
        if(ConfigFile.getInstance().readMapConfig.get("RtkType").equals("1"))//4G
        {
            obj = new AccessBasertkBy4G(ConfigFile.getInstance().readMapConfig.get("4G_Mac"),
                    ConfigFile.getInstance().readMapConfig.get("4G_Qybs"));
            nRet = obj.ReadBaseRtkData();
        }
        else if(ConfigFile.getInstance().readMapConfig.get("RtkType").equals("2"))//vpn
        {
            obj = new AccessBasertkByWifi(ConfigFile.getInstance().readMapConfig.get("SvrIp"),
                    ConfigFile.getInstance().readMapConfig.get("NetPort"), context);

            nRet = obj.ReadBaseRtkData();
        }
        else if(ConfigFile.getInstance().readMapConfig.get("RtkType").equals("3"))//serialport
        {
            int nBaud = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("SerialBaudValue"));
            //int nBaud = 115200;
            obj = new AccessBasertkBySerialPort(ConfigFile.getInstance().readMapConfig.get("SerialPortValue"),
                    nBaud);
            nRet = obj.ReadBaseRtkData();
        }
        else if(ConfigFile.getInstance().readMapConfig.get("RtkType").equals("4"))
        {
            nRet = 1;
        }

        if(nRet == 0)
            return false;

        return true;
    }

    public Boolean Open()
    {
        if(ConfigFile.getInstance().readMapConfig.get("RtkType").equals("4") == false) {
            if (ProcessCenter.getInstance().Start("ttyS0", 115200) == false)
                return false;
        }
        else
        {
            if (ProcessCenter.getInstance().Start(ConfigFile.getInstance().readMapConfig.get("HostIP"),
                    ConfigFile.getInstance().readMapConfig.get("HostPort")) == false)
                return false;
        }

        return true;
    }

    public void Exit()
    {
        if(obj != null)
            obj.StopRead();
        SensorManager.getInstance().Close();
    }

    public int ZoomIn()
    {
        return MapView.ZoomIn();
    }

    public int ZoomOut()
    {
        return MapView.ZoomOut();
    }

    public void RecordTrack(boolean bStartRecord)
    {
        if(bStartRecord && GlobalConfigVar.getInstance().mRecordTrack == false)
        {
            GlobalConfigVar.getInstance().mTrackFilename = GlobalConfigVar.getInstance().getStringDate();
        }
        GlobalConfigVar.getInstance().mRecordTrack = bStartRecord;
        if(bStartRecord == false)
        {
            GlobalConfigVar.getInstance().mTrackFilename = null;
        }
    }
}
