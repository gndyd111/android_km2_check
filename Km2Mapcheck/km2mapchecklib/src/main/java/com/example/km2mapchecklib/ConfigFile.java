package com.example.km2mapchecklib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.System.in;

public class ConfigFile {
    private Properties mProp = new Properties();
    private String mConfigFile = "RtkConfig";
    private String[] arrKey = {"RtkType", "4G_WebUrl", "4G_Qybs", "4G_Mac","SvrIp","NetPort","SerialPort","SerialPortValue","SerialBaud","SerialBaudValue","HostIP","HostPort","TrackType"};

    public Map<String, String> readMapConfig = new HashMap<String, String>();
    public Map<String, String> writeMapConfig = new HashMap<String, String>();

    private ConfigFile(){}

    private static class GlobalConfigFileHolder{
        private final static ConfigFile instance = new ConfigFile();
    }

    public static ConfigFile getInstance(){return GlobalConfigFileHolder.instance;}

    public Boolean getValue(Context context){
        readMapConfig.clear();

        FileInputStream in = null;
        BufferedReader read = null;
        try
        {
            in = context.openFileInput(mConfigFile);
            read = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while((line = read.readLine()) != null)
            {
                String[] str = line.split(",");
                readMapConfig.put(str[0], str[1]);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            if(read !=null)
            {
                try
                {
                    read.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if(readMapConfig.get("RtkType") == null || readMapConfig.get("RtkType").isEmpty())
            return false;
        else
        {
            if(readMapConfig.get("RtkType").equals("1"))//4G
            {
                if(readMapConfig.get("4G_WebUrl") == null || readMapConfig.get("4G_WebUrl").isEmpty())
                    return false;

                if(readMapConfig.get("4G_Qybs") == null || readMapConfig.get("4G_Qybs").isEmpty())
                    return false;

                if(readMapConfig.get("4G_Mac") == null || readMapConfig.get("4G_Mac").isEmpty())
                    return false;
            }
            else if(readMapConfig.get("RtkType").equals("2"))//VPN
            {
                if(readMapConfig.get("SvrIp") == null || readMapConfig.get("SvrIp").isEmpty())
                    return false;

                if(readMapConfig.get("NetPort") == null || readMapConfig.get("NetPort").isEmpty())
                    return false;
            }
            else if(readMapConfig.get("RtkType").equals("3"))//SerialPort
            {
                if(readMapConfig.get("SerialPort") == null || readMapConfig.get("SerialPort").isEmpty())
                    return false;

                if(readMapConfig.get("SerialBaud") == null || readMapConfig.get("SerialBaud").isEmpty())
                    return false;

                if(readMapConfig.get("SerialPortValue") == null || readMapConfig.get("SerialPortValue").isEmpty())
                    return false;

                if(readMapConfig.get("SerialBaudValue") == null || readMapConfig.get("SerialBaudValue").isEmpty())
                    return false;
            }
            else if(readMapConfig.get("RtkType").equals("4"))//TrackDebug
            {
                if(readMapConfig.get("HostIP") == null || readMapConfig.get("HostIP").isEmpty())
                    return false;

                if(readMapConfig.get("HostPort") == null || readMapConfig.get("HostPort").isEmpty())
                    return false;

                if(readMapConfig.get("TrackType") == null || readMapConfig.get("TrackType").isEmpty())
                    return false;
            }
        }

        return true;
    }

    public Boolean setValue(Context context) {
            /*mProp.load(context.openFileInput(mConfigFile));
            mProp.setProperty (key,value);
            FileOutputStream fos = context.openFileOutput(mConfigFile,Context.MODE_PRIVATE);
            mProp.store(fos, null);
            fos.flush();*/
        if(!writeMapConfig.containsKey("RtkType"))
            return false;
        else
        {
            if(writeMapConfig.get("RtkType") == null || writeMapConfig.get("RtkType").isEmpty())
                return false;
            else
            {
                if(writeMapConfig.get("RtkType").equals("1"))//4G
                {
                    if(writeMapConfig.get("4G_WebUrl") == null || writeMapConfig.get("4G_WebUrl").isEmpty())
                        return false;

                    if(writeMapConfig.get("4G_Qybs") == null || writeMapConfig.get("4G_Qybs").isEmpty())
                        return false;

                    if(writeMapConfig.get("4G_Mac") == null || writeMapConfig.get("4G_Mac").isEmpty())
                        return false;
                }
                else if(writeMapConfig.get("RtkType").equals("2"))//VPN
                {
                    if(writeMapConfig.get("SvrIp") == null || writeMapConfig.get("SvrIp").isEmpty())
                        return false;

                    if(writeMapConfig.get("NetPort") == null || writeMapConfig.get("NetPort").isEmpty())
                        return false;
                }
                else if(writeMapConfig.get("RtkType").equals("3"))//SerialPort
                {
                    if(writeMapConfig.get("SerialPort") == null || writeMapConfig.get("SerialPort").isEmpty())
                        return false;

                    if(writeMapConfig.get("SerialBaud") == null || writeMapConfig.get("SerialBaud").isEmpty())
                        return false;

                    if(writeMapConfig.get("SerialPortValue") == null || writeMapConfig.get("SerialPortValue").isEmpty())
                        return false;

                    if(writeMapConfig.get("SerialBaudValue") == null || writeMapConfig.get("SerialBaudValue").isEmpty())
                        return false;
                }
                else if(writeMapConfig.get("RtkType").equals("4"))//TrackDebug
                {
                    if(writeMapConfig.get("HostIP") == null || writeMapConfig.get("HostIP").isEmpty())
                        return false;

                    if(writeMapConfig.get("HostPort") == null || writeMapConfig.get("HostPort").isEmpty())
                        return false;

                    if(writeMapConfig.get("TrackType") == null || writeMapConfig.get("TrackType").isEmpty())
                        return false;
                }
            }
        }

        FileOutputStream out = null;
        BufferedWriter writer = null;
        try
        {
            out = context.openFileOutput(mConfigFile, MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            for (Map.Entry<String, String> entry : writeMapConfig.entrySet()) {
                String str = entry.getKey() + "," + entry.getValue() + "\r\n";
                writer.write(str);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally {
            try {
                if (writer != null)
                    writer.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return true;
    }
}
