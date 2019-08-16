package com.example.km2mapchecklib;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
//import org.ksoap2.transport.AndroidHttpTransport;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;

public class AccessBasertkBy4G  /*extends AsyncTask<String, Integer, String>*/ implements InAccessBaseRtk {
    //private final String WEB_SERVER_URL = "http://www.china-xueche.com:5001/WebService1.asmx";
    //private final ExecutorService executorService = Executors.newFixedThreadPool(3);//限制线程池大小为3的线程池
    private final String NAMESPACE = "http://tempuri.org/";// 命名空间
    private final String METHODNAME = "ReadGpsDataNew";
    public String MAC;
    public String QYBS;
    private static int mResult = 1;
    private Boolean isReadData = true;

    private SerialPort mSerialPort;
    private String mPort = "/dev/ttyS0";
    private int mBaud = 115200;
    private OutputStream mOut;

    /*@Override
    protected String doInBackground(String... params) {
        return null;
    }

    @Override
    //此方法可以在主线程改变UI
    protected void onPostExecute(String result) {
    }*/
    public AccessBasertkBy4G(String mac, String qybs)
    {
        MAC = mac;
        QYBS = qybs;
    }
        //@Override
    public int ReadBaseRtkData()
    {
        try {
            mSerialPort = new SerialPort(new File(mPort), mBaud, 0);
            mOut = mSerialPort.getOutputStream();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SoapObject request = new SoapObject(NAMESPACE, METHODNAME);
                    // 设置需调用WebService接口需要传入的两个参数
                    request.addProperty("Qybs", QYBS);
                    request.addProperty("MacID", MAC);

                    //创建SoapSerializationEnvelope 对象，同时指定soap版本号
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER11);
                    //envelope.bodyOut = request;//由于是发送请求，所以是设置bodyOut
                    envelope.dotNet = true;//由于是.net开发的webservice，所以这里要设置为true
                    envelope.setOutputSoapObject(request);

                    String url = ConfigFile.getInstance().readMapConfig.get("4G_WebUrl");
                    HttpTransportSE httpTransportSE = new HttpTransportSE(url);
                    while(isReadData) {
                        try {
                            Thread.sleep(1000);
                            httpTransportSE.call(NAMESPACE + METHODNAME, envelope);

                            // 获取返回的数据
                            SoapObject object = (SoapObject) envelope.bodyIn;
                            // 获取返回的结果
                            String rtk = object.getProperty(0).toString();

                            if (rtk != null) {
                                if (rtk.equals("anyType{}") == false) {

                                    byte[] readBytes = Base64.decode(rtk.getBytes(), Base64.DEFAULT);//rtk.getBytes();
                                    //String str = byte2hex(readBytes);
                                    mOut.write(readBytes);
                                    mOut.flush();
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    Close();
                }
            }
        }).start();

        return mResult;
    }

    @Override
    public void StopRead()
    {
        isReadData = false;
        mResult = 1;
        Close();
    }

    private void Close()
    {
        try {
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }

            if (mOut != null) {
                mOut.close();
            }
        }
        catch (Exception e)
        {
            Log.e("CloseBaseRtkError4G", e.getLocalizedMessage(), e);
        }
    }
}
