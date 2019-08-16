package com.example.km2mapchecklib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewDebug;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class MapManager {
    private List<UnitMapData> listUnitMapData ;
    private Map<String, VanProject> mapProject = new HashMap<String, VanProject>();
    private String mFilePath = "map.dat";

    //静态内部类 单例模式
    private MapManager()
    {}

    private static class MapManagerHolder
    {
        private final static MapManager instance=new MapManager();
    }

    public static MapManager getInstance(){
        return MapManagerHolder.instance;
    }

    public int Load(/*byte[] rBytes*/) throws Exception
    {
        String str = "$GPGGA,031418.400,3143.03617000,N,11715.18197400,E,4,,,25.6110,M,-0.700,M,1.2,0333*4E\n" +
                "$GPRMC,031418.400,A,3143.03617000,N,11715.18197400,E,2.11,292.81,190717,,W,D,S*44\n" +
                "$PASHR,031418.400,294.74,T,-0.51,0.11,-0.01,0.502,0.502,0.175,1*0C\n" +
                "$KSXT,20170719031418.40,117.25303290,31.71726950,25.6110,294.74,0.11,292.81,3.9,-0.51,3,3,16,16,49.049,46.001,-15.231,-3.589,1.480,-0.256,,,*";
        byte[]arr = str.getBytes();
        int len2 = arr.length;
        int len3 = str.length();
        int dex = str.indexOf("$PASHR");
        int dex2 = str.indexOf("*",dex);
        String str3 = str.substring(0,dex2);
        int indx = str.indexOf("$PASHR");
        String substr = str.substring(0,indx);

        byte[] rBytes = null;
        String ss = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), mFilePath);
        if(file.exists() == false)
            return 0;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FileInputStream inputStream = new FileInputStream(file);
            rBytes = new byte[inputStream.available()];
            inputStream.read(rBytes);
            inputStream.close();
        }
        else
            return 0;

        byte[] bLon = Utils.subBytes(rBytes,8,8);
        GlobalConfigVar.getInstance().baseLon  = Utils.byteArrayToDouble(bLon,0);

        byte[] bLat = Utils.subBytes(rBytes,16,8);
        GlobalConfigVar.getInstance().baseLat  = Utils.byteArrayToDouble(bLat,0);

        byte[] ArrByteObj = Utils.subBytes(rBytes, 128, 4);
        int objNums = Utils.byteArrayToInt(ArrByteObj);

        byte[] ArrByteField = Utils.subBytes(rBytes, 132,4);
        int fieldNums = Utils.byteArrayToInt(ArrByteField);

        byte[] ArrByteTotalData = Utils.subBytes(rBytes, 136, 28 * objNums);
        int len = ArrByteTotalData.length;

        for (int i = 0; i< objNums;i++)
        {
            byte[] btFieldNo = Utils.subBytes(ArrByteTotalData, 0 + 28 * i , 2);
            String sFieldNo = Utils.ByteToString(btFieldNo);

            byte[] btProjectId = Utils.subBytes(ArrByteTotalData, 2 + 28 * i , 6);
            String sPorjectId = Utils.ByteToString(btProjectId);

            byte[] btZoneId = Utils.subBytes(ArrByteTotalData, 8 + 28 * i , 6);
            String sZoneId = Utils.ByteToString(btZoneId);

            byte[] btZoneType = Utils.subBytes(ArrByteTotalData, 14 + 28 * i , 6);
            String sZoneType = Utils.ByteToString(btZoneType);

            byte[] btZonePointNum = Utils.subBytes(ArrByteTotalData, 20 + 28 * i , 4);
            int nZonePointNum = Utils.byteArrayToInt(btZonePointNum);

            byte[] btDataOffset = Utils.subBytes(ArrByteTotalData, 24 + 28 * i , 4);
            int nDataOffset = Utils.byteArrayToInt(btDataOffset);

            VanZone vz = new VanZone();
            vz.id = sZoneId;
            vz.nPointCount = nZonePointNum;
            vz.type = Integer.parseInt(sZoneType);
            vz.listPoints = new ArrayList<>();
            for(int j = 0; j< vz.nPointCount;j++)
            {
                LONLAT lonlat = vz.GetNewLonlat();
                byte[]btlon = Utils.subBytes(rBytes, 128 + nDataOffset + 16*j,8);
                double dlon = Utils.bytesArrayToDouble(btlon);

                byte[]btlat = Utils.subBytes(rBytes, 128 + nDataOffset + 8 + 16*j,8);
                double dlat = Utils.bytesArrayToDouble(btlat);

                lonlat.lon = dlon;
                lonlat.lat = dlat;

                vz.listPoints.add(lonlat);
            }
            FillProject(sPorjectId,Integer.parseInt(sFieldNo),vz);
        }
        int n = mapProject.size();
        return 1;
    }

    private void FillProject(String sProjectId, int nFieldNo, VanZone zone)
    {
        if(!mapProject.containsKey(sProjectId))
        {
            VanProject vanProject = new VanProject(sProjectId);
            mapProject.put(sProjectId, vanProject);

            vanProject.Insert(nFieldNo, zone);
        }
        else {
            VanProject vanProject = mapProject.get(sProjectId);
            vanProject.Insert(nFieldNo, zone);
        }
    }

    public VanProject GetProject(int projectId)
    {
        return mapProject.get(projectId);
    }

    public List<VanField> GetFields()
    {
        List<VanField> list = new ArrayList();
        for (Map.Entry<String, VanProject> entry : mapProject.entrySet()) {
            VanProject vanProject = entry.getValue();
            List<VanField> templist = vanProject.GetFieldList();
            list.addAll(list.size(),templist);
        }
        return list;
    }

    private class UnitMapData implements Serializable
    {
        char []fieldNo = new char[2];
        char []projectId = new char[6];
        char []zoneId = new char[6];
        char []zoneType = new char[6];
        int zonePointNum ;
        int dataOffset;
    }
}
