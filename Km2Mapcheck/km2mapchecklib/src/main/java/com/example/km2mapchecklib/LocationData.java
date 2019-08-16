package com.example.km2mapchecklib;

public class LocationData {
    public double latitude = 0;
    public double longitude = 0;
    public double altitude = 0;
    public double speed = 0;
    public SYSTEMTIME time = new SYSTEMTIME();
    public int quality = 0;
    public double heading = 0;
    public double pitch = 0;
    public double roll = 0;
    public int fieldNo = 0;
    public String porojectNo;
    public String extendStr;

    public LocationData(){
    }

    public void Reset()
    {
        quality = 0;
        speed = 0;
        fieldNo = 0;
    }

    public class SYSTEMTIME {
        public int wYear;
        public int wMonth;
        public int wDayOfWeek;
        public int wDay;
        public int wHour;
        public int wMinute;
        public int wSecond;
        public int wMilliseconds;
    }
}
