package com.example.km2mapchecklib;

import android.annotation.SuppressLint;
import android.icu.math.BigDecimal;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.example.km2mapchecklib.CarState.STATE_BACKWARD;
import static com.example.km2mapchecklib.CarState.STATE_FORWARD;
import static com.example.km2mapchecklib.CarState.STATE_STOP;

enum CarState{
    STATE_FORWARD,
    STATE_BACKWARD,
    STATE_STOP
}

public class ProcessCenter extends Observable  implements Observer {
    public LocationData locationData;
    private Proj4 mProj4 = new Proj4();

    public Boolean mIsSpqb = false;
    private List<VanField> mFieldList;
    private List<LocationData> mGpsList = new ArrayList<>();
    private int mPointCount = 0;
    private String[] mArrPtZoneId;
    private double mStopSpeed = 0;
    private CarState mCarState = STATE_STOP;
    private CarState mHistoryState = STATE_STOP;
    private double mForwardDist = 0;
    private double mBackwardDist = 0;
    private LONLAT mStopPos = new LONLAT();
    private int mBuffSize = 5;
    private double mGpsOriginalLongitude = 0;
    private double mGpsOriginalLatitude = 0;
    private double mGpsOriginalAltitude = 0;
    private final int RECIVEDATA = 1;

    private ProcessCenter() {
    }

    private static class ProcessCenterHolder {
        private final static ProcessCenter instance = new ProcessCenter();
    }

    public static ProcessCenter getInstance() {
        return ProcessCenterHolder.instance;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //  Log.i("gh3st", "connect status:" + msg.what);
            switch (msg.what) {
                case RECIVEDATA:
                        locationData = new LocationData();
                        locationData = (LocationData) msg.obj;

                        mGpsOriginalLongitude = locationData.longitude;
                        mGpsOriginalLatitude = locationData.latitude;
                        mGpsOriginalAltitude = locationData.altitude;

                        Process();
                    break;
            }
        }
    };

    @Override
    public void update(Observable o, Object arg) {
        /*locationData = (LocationData) arg;

        mGpsOriginalLongitude = locationData.longitude;
        mGpsOriginalLatitude = locationData.latitude;
        mGpsOriginalAltitude = locationData.altitude;

        Process();*/
    }

    public void Init() {
        mProj4.Init(GlobalConfigVar.getInstance().baseLon, GlobalConfigVar.getInstance().baseLat);
        mFieldList = MapManager.getInstance().GetFields();
        if (mFieldList != null) {
            mPointCount = CarModel.getInstance().GetPointCount();
            mArrPtZoneId = new String[mPointCount];
        }
        mStopSpeed = GlobalConfigVar.getInstance().GetStopSpeed();

        /*double xx = 2.254866400847205E8;
        double yy = 22.23232;
        yy = Math.round(yy*100)/100.0;
        int zz = (int)yy;
        BigDecimal bd = new BigDecimal(Double.toString(xx));
       // String str = bd.();
        int k = 5;*/
    }

    public Boolean Start(String gpsPort, int gpsBaud) {
        Init();
        //SensorManager.getInstance().addObserver(this);
        return SensorManager.getInstance().Open(gpsPort, gpsBaud, mHandler);
    }

    public Boolean Start(String ip, String port)
    {
        Init();
        return SensorManager.getInstance().Open(ip, port, mHandler);
    }

    public void Stop() {
        //SensorManager.getInstance().deleteObserver(this);
        SensorManager.getInstance().Close();
    }

    private void Process() {
        if (locationData.quality == 0)
            return;

        locationData.fieldNo = 0;
        locationData.porojectNo = "X";

        mProj4.Transform(/*locationData.longitude, locationData.latitude*/locationData);

        LocationData temp = new LocationData();
        temp.longitude = locationData.longitude;
        temp.latitude = locationData.latitude;
        if (mGpsList.size() >= mBuffSize)
            mGpsList.remove(0);
        mGpsList.add(temp);

        double x = temp.longitude;
        double y = temp.latitude;
        CarModel.getInstance().SetAntenna(/*locationData.longitude, locationData.latitude*/x,y);
        CarModel.getInstance().CalcFeaturePoint(locationData.heading);
        //LONLAT ll = new LONLAT();
        //CarModel.getInstance().GetAntenna(ll);
        //locationData.longitude = ll.lon;
        //locationData.latitude = ll.lat;

        JudgeFeaturePoints();
        CalcOther();
        ProcessResult(temp);
    }

    private void JudgeFeaturePoints() {
        for (int i = 0; i < mPointCount; i++) {
            mArrPtZoneId[i] = "0";
        }

        int nFieldCount = mFieldList.size();
        if (nFieldCount == 0) return;

        for (int i = 0; i < mPointCount; i++) {
            for (int nFieldIndex = 0; nFieldIndex < nFieldCount; ++nFieldIndex) {
                LONLAT ll = CarModel.getInstance().mDynamicPt.get(i);
                if (!mFieldList.get(nFieldIndex).GetMBR().IsPointInRect(ll))
                    continue;

                int nZoneIndex = 0;
                int nZoneCount = mFieldList.get(nFieldIndex).GetZoneCount();
                for (nZoneIndex = 0; nZoneIndex < nZoneCount; ++nZoneIndex) {
                    VanZone vanZone = mFieldList.get(nFieldIndex).GetZone(nZoneIndex);
                    Boolean ret = GlobalConfigVar.getInstance().IsPointInPolygon(ll, vanZone.listPoints, vanZone.nPointCount);
                    if (ret) {
                        mArrPtZoneId[i] = vanZone.id;
                        locationData.fieldNo = mFieldList.get(nFieldIndex).GetNo();
                        locationData.porojectNo = mFieldList.get(nFieldIndex).GetProject().GetId();

                        if (locationData.porojectNo.compareTo("20300") == 0 && i == 0)
                            mIsSpqb = true;
                        if (locationData.porojectNo.compareTo("20300") == 0 && vanZone.id.compareTo("20102") == 0 && (i == 12 || i == 24 || i == 28))
                            mIsSpqb = false;
                        if (locationData.porojectNo.compareTo("20300") != 0 && i == 0)
                            mIsSpqb = false;

                        break;
                    }
                }
                if (nZoneIndex < nZoneCount) {
                    break;
                }
            }
        }
    }

    private void CalcOther() {
        LocationData fstGps = null;
        LocationData lstGps = null;
        CarState carState = STATE_STOP;
        if (locationData.speed > mStopSpeed) {
            carState = STATE_FORWARD;
            if (mGpsList.size() >= mBuffSize) {
                fstGps = mGpsList.get(0);
                lstGps = mGpsList.get(mGpsList.size() - 1);

                double tan = CalcXYAnagle((lstGps.latitude + 10000) * 100, (lstGps.longitude + 10000) * 100,
                        (fstGps.latitude + 10000) * 100, (fstGps.longitude + 10000) * 100) + 180;
                if (tan > 360)
                    tan -= 360;

                double dbTmp = Math.abs(tan - locationData.heading);
                if (dbTmp > 180)
                    dbTmp = 360 - dbTmp;

                if (dbTmp > 90) {
                    carState = STATE_BACKWARD;
                    //Log.i("后退","STATE_BACKWARD");
                }
            }
        }

        try {
            double fsty = 0;
            double fstx = 0;
            double lsty = 0;
            double lstx = 0;
            double stopx = 0;
            double stopy = 0;
            double currx = 0;
            double curry = 0;
            if (mCarState != carState) {
                if (STATE_STOP == carState) {
                } else {
                    /*if (fstGps != null && lstGps != null) {
                        if (STATE_FORWARD == carState) {
                            fsty = Math.round(fstGps.latitude * 100) / 100.0;
                            fstx = Math.round(fstGps.longitude * 100) / 100.0;
                            lsty = Math.round(lstGps.latitude * 100) / 100.0;
                            lstx = Math.round(lstGps.longitude * 100) / 100.0;

                            mForwardDist += Math.sqrt((fsty - lsty) * (fsty - lsty) + (fstx - lstx) * (fstx - lstx));
                        } else if (STATE_BACKWARD == carState) {
                            fsty = Math.round(fstGps.latitude * 100) / 100.0;
                            fstx = Math.round(fstGps.longitude * 100) / 100.0;
                            lsty = Math.round(lstGps.latitude * 100) / 100.0;
                            lstx = Math.round(lstGps.longitude * 100) / 100.0;
                            mBackwardDist += Math.sqrt((fsty - lsty) * (fsty - lsty) + (fstx - lstx) * (fstx - lstx));
                        }
                    }*/
                }
                mCarState = carState;
            }

            if (mStopPos.lon != 0 && mStopPos.lat != 0) {
                if (mCarState == STATE_FORWARD) {
                    stopx = Math.round(mStopPos.lon * 100) / 100.0;
                    stopy = Math.round(mStopPos.lat * 100) / 100.0;
                    currx = Math.round(locationData.longitude * 100) / 100.0;
                    curry = Math.round(locationData.latitude * 100) / 100.0;

                    mForwardDist += Math.sqrt((stopy - curry) * (stopy - curry) + (stopx - currx) * (stopx - currx));
                } else if (mCarState == STATE_BACKWARD) {
                    stopx = Math.round(mStopPos.lon * 100) / 100.0;
                    stopy = Math.round(mStopPos.lat * 100) / 100.0;
                    currx = Math.round(locationData.longitude * 100) / 100.0;
                    curry = Math.round(locationData.latitude * 100) / 100.0;
                    mBackwardDist += Math.sqrt((stopy - curry) * (stopy - curry) + (stopx - currx) * (stopx - currx));
                }
            }

            mStopPos.lon = Math.round(locationData.longitude * 100) / 100.0;
            mStopPos.lat = Math.round(locationData.latitude * 100) / 100.0;

            if (STATE_STOP != carState) {
                mHistoryState = carState;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private double CalcXYAnagle(double startx, double starty, double endx, double endy) {
        if (endx == startx) {
            return endy >= starty ? 0 : 180;
        } else if (endy == starty) {
            return endx > startx ? 90 : 270;
        } else if (endx > startx && endy > starty) {
            return Math.atan(Math.abs((endy - starty) / (endx - startx))) * 180 / Math.PI;
        } else if (endx < startx && endy > starty) {
            return 180 - Math.atan(Math.abs((endy - starty) / (startx - endx))) * 180 / Math.PI;
        } else if (endx < startx && endy < starty) {
            return 180 + Math.atan(Math.abs((starty - endy) / (startx - endx))) * 180 / Math.PI;
        } else {
            return 360 - Math.atan(Math.abs((starty - endy) / (endx - startx))) * 180 / Math.PI;
        }
    }

    private void ProcessResult(LocationData temp) {
        updateLocation(temp);
        FillGpsString();
        FillSensorString();
        if(ConfigFile.getInstance().readMapConfig.get("RtkType").equals("4") && ConfigFile.getInstance().readMapConfig.get("TrackType") == "2")
            FillTrackStringForWxcheck();
    }

    private void FillGpsString() {
        String Date = GetDate();
        String Time = GetTime();

        String GpsString = "$HZZH1," + Date + "," + Time;

        String Heading = String.format("%.2f", locationData.heading);
        GpsString += "," + Heading;

        String Pitch = String.format("%.2f", locationData.pitch);
        GpsString += "," + Pitch;

        String Roll = String.format("%.2f", locationData.roll);
        GpsString += "," + Roll;

        String X = String.format("%.10f", locationData.longitude);
        GpsString += "," + X;

        String Y = String.format("%.10f", locationData.latitude);
        GpsString += "," + Y;

        String Z = String.format("%.3f", locationData.altitude);
        GpsString += "," + Z;
     /*double s2 = locationData.altitude;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.000");
        String str = df.format(locationData.altitude);*/

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.0000000000");
        String OriginalX = df.format(mGpsOriginalLongitude);
        GpsString += "," + OriginalX;

        String OriginalY = String.format("%.10f", mGpsOriginalLatitude);
        GpsString += "," + OriginalY;

        String OriginalZ = String.format("%.3f", mGpsOriginalAltitude);
        GpsString += "," + OriginalZ;

        String Speed = String.format("%.5f", locationData.speed);
        GpsString += "," + Speed;

        if (mCarState == STATE_STOP)
            GpsString += ",0,0";
        else if (mCarState == STATE_FORWARD)
            GpsString += ",1,0";
        else if (mCarState == STATE_BACKWARD)
            GpsString += ",0,1";
        else
            GpsString += ",0,0";

        //String forward = String.format("%.3f", mForwardDist);
        //String backward = String.format("%.3f",mBackwardDist);
        java.text.DecimalFormat dfForward = new java.text.DecimalFormat("0.000");
        String forward = dfForward.format(mForwardDist);

        java.text.DecimalFormat dfBackward = new java.text.DecimalFormat("0.000");
        String backward = dfBackward.format(mBackwardDist);

        GpsString += "," + forward + "," + backward;
        GpsString += "," + String.valueOf(locationData.quality) + "*FF";

        CbManger.getInstance().GetCbObject().Notify(GpsString);
    }

    private void FillSensorString() {
        String Date = GetDate();
        String Time = GetTime();

        String SensorString = "$HZZH2," + Date + "," + Time;

        for (int i = 0; i < mPointCount; i++) {
            String temp;
            temp = locationData.porojectNo + "_" + String.valueOf(locationData.fieldNo) + "_" + mArrPtZoneId[i];
            SensorString += "," + temp;
        }

        SensorString += "," + String.valueOf(locationData.quality) + "*FF";

        CbManger.getInstance().GetCbObject().Notify(SensorString);
    }

    private void FillTrackStringForWxcheck()
    {
        String TrackString = "$HZZH3," + locationData.extendStr;
        CbManger.getInstance().GetCbObject().Notify(TrackString);
    }

    private String GetDate()
    {
        String tempMonth;
        if (locationData.time.wMonth < 10)
            tempMonth = "0" + String.valueOf(locationData.time.wMonth);
        else
        tempMonth = String.valueOf(locationData.time.wMonth);

        String tempDay;
        if (locationData.time.wDay < 10)
            tempDay = "0" + String.valueOf(locationData.time.wDay);
        else
            tempDay = String.valueOf(locationData.time.wDay);

        String Date = String.valueOf(locationData.time.wYear) + tempMonth + tempDay;

        return Date;
    }

    private String GetTime()
    {
        String tempHour;
        if (locationData.time.wHour + 8 < 10)
            tempHour = "0" + String.valueOf(locationData.time.wHour + 8);
        else
            tempHour = String.valueOf(locationData.time.wHour + 8);

        String tempMinute ;
        if (locationData.time.wMinute< 10)
            tempMinute = "0" + String.valueOf(locationData.time.wMinute);
        else
            tempMinute = String.valueOf(locationData.time.wMinute);

        String tempSecond ;
        if (locationData.time.wSecond< 10)
            tempSecond = "0" + String.valueOf(locationData.time.wSecond);
        else
            tempSecond = String.valueOf(locationData.time.wSecond);

        String Time = tempHour + tempMinute + tempSecond;

        return Time;
    }

    private void updateLocation(LocationData locationData)
    {
        setChanged();
        double x = locationData.longitude;
        double y = locationData.latitude;
        notifyObservers(locationData);
    }

    public void test()
    {
        //mProj4.Init(115.73537787, 33.80100575);
       // double latitude = getCoor(Double.parseDouble("3347.99908623"));
        //double longitude = getCoor(Double.parseDouble("11544.15741777"));
        //mProj4.Transform(longitude,latitude);

        //Init(115.73537787, 33.80100575);
        Start("ttyS0", 115200);

        locationData = new LocationData();
        locationData.latitude = getCoor(Double.parseDouble("3347.99702683"));//"3347.99791739"
        locationData.longitude = getCoor(Double.parseDouble("11544.15282825"));//"11544.15829206"
        locationData.heading = 268.3;
        //contiue之后
        //locationData.latitude = getCoor(Double.parseDouble("3347.99791739"));
        //locationData.longitude = getCoor(Double.parseDouble("11544.15829206"));
        //第一条 "11544.15282825"  "3347.99702683"
        //locationData.latitude = getCoor(Double.parseDouble("3347.99702683"));
        //locationData.longitude = getCoor(Double.parseDouble("11544.15282825"));
        locationData.quality = 4;
        Process();
    }

    private double getCoor(double data)
    {
        int value = (int)(data/100);
        return value + (data - value*100)/60;
    }
}

