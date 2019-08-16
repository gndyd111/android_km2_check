package com.example.km2mapchecklib;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.io.Proj4FileReader;

import java.io.IOException;

public class Proj4 {
   // private String PROJ_WGS84 = "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84 +units=degress";
   private CoordinateReferenceSystem wgs84;
   private CoordinateReferenceSystem target;
   private CRSFactory crsFactory = new CRSFactory();
   private CRSFactory targetFactory = new CRSFactory();

   private CoordinateTransform transform;
   private CoordinateTransformFactory ctf = new CoordinateTransformFactory();

   private final double DEG_TO_RAD = .0174532925199432958;

    public void Init(double longitude, double latitude)
    {
        //src
        Proj4FileReader proj4FileReader = new Proj4FileReader();
        String[] paramStr = new String[0];
        String str = "";
        try {
            str = "+proj=longlat +datum=WGS84 +no_defs";
        } catch (Exception e) {
            e.printStackTrace();
        }
        wgs84 = crsFactory.createFromParameters("WGS84", str);

        //dest
        String proj4 = String.format("+proj=tmerc +lat_0=%.8f +lon_0=%.8f +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs", latitude, longitude);
        target = targetFactory.createFromParameters("target", proj4);

        transform = ctf.createTransform(wgs84, target);
    }

    public void Transform(/*double lon, double lat*/LocationData locationData)
    {
        if(transform != null)
        {
            /*ProjCoordinate projCoordinate = new ProjCoordinate(lon, lat);
            transform.transform(projCoordinate, projCoordinate);

            ProcessCenter.getInstance().locationData.longitude = projCoordinate.x;
            ProcessCenter.getInstance().locationData.latitude = projCoordinate.y;*/
            ProjCoordinate projCoordinate = new ProjCoordinate(locationData.longitude, locationData.latitude);
            transform.transform(projCoordinate, projCoordinate);

            locationData.longitude = projCoordinate.x;
            locationData.latitude = projCoordinate.y;
        }
    }
}
