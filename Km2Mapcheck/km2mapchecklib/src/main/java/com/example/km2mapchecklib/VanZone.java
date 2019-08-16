package com.example.km2mapchecklib;

import java.util.List;

public class VanZone {
    public String id;
    public int type;
    public int nPointCount;
    public List<LONLAT> listPoints;

    public LONLAT GetNewLonlat()
    {
        return new LONLAT();
    }
}
