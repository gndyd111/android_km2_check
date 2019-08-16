package com.example.km2mapchecklib;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LLRECT {
    private double left;
    private double top;
    private double right;
    private double bottom;

    public LLRECT()
    {
        left   = 180.0;
        right  = -180.0;
        top    = -90.0;
        bottom = 90.0;
    }

    public LLRECT(double Left, double Top, double Right, double Bottom)
    {
        left   = Left;
        top    = Top;
        right  = Right;
        bottom = Bottom;
    }

    public LLRECT(final LLRECT llRect)
    {
        left   = llRect.left;
        right  = llRect.right;
        top    = llRect.top;
        bottom = llRect.bottom;
    }

    public void Union(double lon, double lat)
    {
        this.left	 = min(this.left, lon);
        this.bottom = min(this.bottom, lat);
        this.right	 = max(this.right, lon);
        this.top	 = max(this.top, lat);
    }

    public void Union(final LONLAT lonlat)
    {
        this.left	 = min(this.left, lonlat.lon);
        this.bottom = min(this.bottom, lonlat.lat);
        this.right	 = max(this.right, lonlat.lon);
        this.top	 = max(this.top, lonlat.lat);
    }

    public Boolean IsPointInRect(final LONLAT point)
    {
        double val = 0.00001;
        return (point.lon >= (left-val) && point.lon <= (right+val) &&
                point.lat >= (bottom-val) && point.lat <= (top+val));
    }
}
