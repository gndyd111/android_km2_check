package com.example.km2mapchecklib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.ProxyInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.WIFI_SERVICE;

public class GlobalConfigVar {
    public double fBackAntennaHeight = 1.83;
    public double fSlopeAngle = 5.74;
    public double baseLon = 0;
    public double baseLat = 0;
    public String buffer ;
    public String localIp = null;
    public boolean mRecordTrack = false;
    public String mTrackFilename = null;

    private final double FLOAT_LL_TOLERANCE	= 0.000005;
    private final double STOP_SPEED = 0.2;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private GlobalConfigVar(){}

    private static class GlobalConfigVarHolder{
        private final static GlobalConfigVar instance = new GlobalConfigVar();
    }

    public static GlobalConfigVar getInstance(){return GlobalConfigVarHolder.instance;}

    public double GetStopSpeed(){return STOP_SPEED;}

    public void ManualSetStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public Boolean IsPointInPolygon(final LONLAT llpt, List<LONLAT> listll, int nCount)
    {
        int nCross = 0;

        for (int i = 0; i < nCount; i++)
        {
            final LONLAT p1 = listll.get(i);
            final LONLAT p2 = listll.get((i + 1) % nCount);

            double tempDiffLat = p1.lat - p2.lat;
            if(tempDiffLat > -FLOAT_LL_TOLERANCE && tempDiffLat < FLOAT_LL_TOLERANCE)
                continue;

            if (llpt.lat < Math.min(p1.lat, p2.lat))
                continue;

            if (llpt.lat >= Math.max(p1.lat, p2.lat))
                continue;

            double x = (llpt.lat - p1.lat) * (p2.lon - p1.lon) / (p2.lat - p1.lat) + p1.lon;

            if ( x > llpt.lon )
                nCross++;
        }

        return (nCross % 2 == 1);
    }

    public String ReturnMac()
    {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ;//mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddress();
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    public String byte2hex(byte b[]) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    public  String str2HexStr(String str)
    {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public String getEthernetIp(Context context) {
        String mEthIpAddress = "";

        //if (!isUsingStaticIp(context))
        {
            try {
                Map<String, String> ipMaps = new HashMap<String, String>();

                //获取ETHERNET_SERVICE参数
                String ETHERNET_SERVICE = (String) Context.class.getField("ETHERNET_SERVICE").get(null);
                // 根据类名获得具体的类
                Class<?> ethernetManagerClass = Class.forName("android.net.EthernetManager");
                //获取ethernetManager服务对象
                Object ethernetManager = context.getSystemService(ETHERNET_SERVICE);
                //获取在EthernetManager中的抽象类mService成员变量
                Field mService = ethernetManagerClass.getDeclaredField("mService");
                // 设置访问权限
                mService.setAccessible(true);
                //获取抽象类的实例化对象
                Object mServiceObject = mService.get(ethernetManager);
                Class<?> iEthernetManagerClass = Class.forName("android.net.IEthernetManager");
                Method[] methods = iEthernetManagerClass.getDeclaredMethods();

                for (Method ms : methods) {
                    String methodName = ms.getName();
                    if ("getConfiguration".equals(methodName)) {
                        Object getConfiguration = ms.invoke(mServiceObject);
                        String str = getConfiguration.toString();
                        String[] arr = str.split("\n");
                        if (arr != null && arr.length >= 2) {
                            String[] subarr1 = arr[1].split(":");
                            String[] subarr2 = subarr1[1].split(" ");
                            if (subarr2[3].contains("/")) {
                                String[] subarr3 = subarr2[3].split("/");
                                mEthIpAddress = subarr3[0];
                            } else
                                mEthIpAddress = subarr2[3];
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mEthIpAddress;
    }

    private boolean isUsingStaticIp(Context context) {
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo=wifiManager.getDhcpInfo();
        if(dhcpInfo.leaseDuration==0){//静态IP配置方式
            return true;
        }else{                         //动态IP配置方式
            return false;
        }
    }

    private  String getMacAddress() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    public String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }
}
