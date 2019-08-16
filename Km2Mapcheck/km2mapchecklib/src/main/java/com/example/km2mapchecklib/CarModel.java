package com.example.km2mapchecklib;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class CarModel {

    private double mAntennaAngle = 0;
    private List<FeaturePoint> mStaticPt = new ArrayList<>();
    public List<LONLAT> mDynamicPt = new ArrayList<>();
    private LONLAT mBackAntennaPt = new LONLAT();
    private int mCarHeadPtsNums = 32;
    private String mFilePath = "car.car";
    private Boolean mIsCheck = false;

    private CarModel(){}

    private static class CarModelHolder{
        private final static CarModel instance = new CarModel();
    }

    private class FeaturePoint{
        public double angle;
        public double distance;
    }

    public static CarModel getInstance(){return CarModelHolder.instance;}

    public Boolean Init(/*byte[] rBytes*/) throws Exception
    {
        byte[] rBytes = null;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), mFilePath);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FileInputStream inputStream = new FileInputStream(file);
            rBytes = new byte[inputStream.available()];
            inputStream.read(rBytes);
            inputStream.close();
        }
        else
            return false;

        String str = new String(rBytes);
        String[] arr = str.split("\r\n");
        mAntennaAngle = Double.parseDouble(arr[0]);

        for(int i = 1 ; i < arr.length /*- 1*/; i++)
        {
            if(i < (arr.length - 1)) {
                FeaturePoint fp = new FeaturePoint();
                String[] subArr = arr[i].split(",");
                double tempAngle = Double.parseDouble(subArr[0]);
                fp.angle = tempAngle * (PI / 180);
                fp.distance = Double.parseDouble(subArr[1]) / 100000.0;

                mStaticPt.add(fp);

                LONLAT ll = new LONLAT();
                ll.lat = 0.0;
                ll.lon = 0.0;
                mDynamicPt.add(ll);
            }
            else if(i == (arr.length - 1))
            {
                String[] subArr = arr[i].split(",");
                double tempHeight = Double.parseDouble(subArr[1]);
                if(tempHeight == 0)
                    GlobalConfigVar.getInstance().fBackAntennaHeight = 1.6;
                else
                    GlobalConfigVar.getInstance().fBackAntennaHeight = tempHeight;
            }
        }

        String rtkType = (String)ConfigFile.getInstance().readMapConfig.get("RtkType");
        if(rtkType.equals("4") == false)
            mIsCheck = false;
        else
        {
            String trackType = (String)ConfigFile.getInstance().readMapConfig.get("TrackType");
            if(trackType.equals("2"))
                mIsCheck = true;
            else
                mIsCheck = false;
        }
        return true;
    }

    public void SetAntenna(double lon, double lat)
    {
        mBackAntennaPt.lon = lon;
        mBackAntennaPt.lat = lat;
    }

    public void GetAntenna(LONLAT ll)
    {
        if(ll != null)
        {
            ll.lon = mBackAntennaPt.lon;
            ll.lat = mBackAntennaPt.lat;
        }
    }

    public int GetPointCount()
    {
        return mStaticPt.size();
    }

    public void CalcFeaturePoint(double heading)
    {
        double rotateAngle = (360.0 - mAntennaAngle + heading)*(PI / 180);

        for(int i = 0; i < mCarHeadPtsNums; i++)
        {
            double dbAngleRad = mStaticPt.get(i).angle;
            dbAngleRad -= rotateAngle;

            while(dbAngleRad < 0.0) {
                dbAngleRad += 2*PI;
            }

            if(!ProcessCenter.getInstance().mIsSpqb )
            {
                mDynamicPt.get(i).lon = mBackAntennaPt.lon + (cos(dbAngleRad) * mStaticPt.get(i).distance);
                mDynamicPt.get(i).lat = mBackAntennaPt.lat + (sin(dbAngleRad) * mStaticPt.get(i).distance);
            }
            else
            {
                double slopeAngle = GlobalConfigVar.getInstance().fSlopeAngle;
                double antennaHeight = GlobalConfigVar.getInstance().fBackAntennaHeight;
                double tanValue = Math.tan(slopeAngle * (Math.PI / 180));
                double cosValue = Math.cos(slopeAngle * (Math.PI / 180));

                double tempX = Math.cos((90 - heading) * (Math.PI / 180));
                double tempY = Math.sin((90 - heading) * (Math.PI / 180));
                if (mIsCheck == false) {
                    mDynamicPt.get(i).lon = mBackAntennaPt.lon + (cos(dbAngleRad) * mStaticPt.get(i).distance) + antennaHeight * tanValue * cosValue * tempX;
                    mDynamicPt.get(i).lat = mBackAntennaPt.lat + (sin(dbAngleRad) * mStaticPt.get(i).distance) + antennaHeight*tanValue*cosValue*tempY;
                }
                else {
                    mDynamicPt.get(i).lon = mBackAntennaPt.lon + (cos(dbAngleRad) * mStaticPt.get(i).distance);
                    mDynamicPt.get(i).lat = mBackAntennaPt.lat + (sin(dbAngleRad) * mStaticPt.get(i).distance);
                }
            }
        }
    }
}
