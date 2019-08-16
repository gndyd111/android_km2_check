package com.example.km2mapchecklib;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class RtkConfigActivity extends AppCompatActivity {

    FrameLayout fl_container;
    int nSelectRtkType = 0;
    int nSelectTrackType = 0;
    Boolean bReadResult = false;
    //LinearLayout.LayoutParams lp;
    //LayoutInflater inflater;

    //RelativeLayout.LayoutParams rp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        bReadResult = ConfigFile.getInstance().getValue(this);

        fl_container = (FrameLayout)findViewById(R.id.fl_container);

        RadioGroup rbGroup = (RadioGroup)findViewById(R.id.radioGroup_mode);
        RadioButton rb4G = (RadioButton) findViewById(R.id.network);
        RadioButton rbVpn = (RadioButton) findViewById(R.id.vpn);
        RadioButton rbPort = (RadioButton) findViewById(R.id.port);
        RadioButton rbTrack = (RadioButton) findViewById(R.id.trackdebug);

        if(bReadResult)
        {
            String type = ConfigFile.getInstance().readMapConfig.get("RtkType");
            if(type.equals("1"))
            {
                nSelectRtkType = 1;
                fl_container.removeAllViewsInLayout();
                View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.sub4g, fl_container, false);
                fl_container.addView(inflateView);

                EditText mac = (EditText)findViewById(R.id.Mac);
                EditText url = (EditText)findViewById(R.id.webAddress);
                EditText qybs = (EditText)findViewById(R.id.Qybs);

                rb4G.setChecked(true);
                url.setText(ConfigFile.getInstance().readMapConfig.get("4G_WebUrl"));
                qybs.setText(ConfigFile.getInstance().readMapConfig.get("4G_Qybs"));
                mac.setText(ConfigFile.getInstance().readMapConfig.get("4G_Mac"));
            }
            else if(type.equals("2"))
            {
                nSelectRtkType = 2;
                fl_container.removeAllViewsInLayout();
                View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.vpn, fl_container, false);
                fl_container.addView(inflateView);
                rbVpn.setChecked(true);

                EditText ip = (EditText)findViewById(R.id.IpAddr);
                EditText port = (EditText)findViewById(R.id.netPort);
                EditText staticIp = (EditText)findViewById(R.id.StaticIp) ;

                ip.setText(ConfigFile.getInstance().readMapConfig.get("SvrIp"));
                port.setText(ConfigFile.getInstance().readMapConfig.get("NetPort"));
                staticIp.setText(GlobalConfigVar.getInstance().localIp);
            }
            else if(type.equals("3"))
            {
                nSelectRtkType = 3;
                fl_container.removeAllViewsInLayout();
                View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.serialport, fl_container, false);
                fl_container.addView(inflateView);
                rbPort.setChecked(true);

                Spinner pt = (Spinner)findViewById(R.id.serialPort);
                Spinner baud = (Spinner)findViewById(R.id.serialPortBaud);

                int index = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("SerialPort"));
                pt.setSelection(index);

                index = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("SerialBaud"));
                baud.setSelection(index);
            }
            else if(type.equals("4"))
            {
                nSelectRtkType = 4;
                fl_container.removeAllViewsInLayout();
                View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.trackdebug, fl_container, false);
                fl_container.addView(inflateView);
                rbTrack.setChecked(true);

                RadioGroup rbTrackGroup = (RadioGroup)findViewById(R.id.radioGroup_track);
                rbTrackGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.debug) {
                            nSelectTrackType = 1;
                        }
                        else if(checkedId == R.id.check) {
                            nSelectTrackType = 2;
                        }
                    }
                });
                //
                int trackversion = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("TrackType"));
                if(trackversion == 1) {
                    RadioButton rbdebug = (RadioButton) findViewById(R.id.debug);
                    rbdebug.setChecked(true);
                }
                else if(trackversion == 2) {
                    RadioButton rbcheck = (RadioButton) findViewById(R.id.check);
                    rbcheck.setChecked(true);
                }

                EditText ip = (EditText)findViewById(R.id.tracklocalip);
                EditText port = (EditText)findViewById(R.id.tracknetport);
                ip.setText(ConfigFile.getInstance().readMapConfig.get("HostIP"));
                port.setText(ConfigFile.getInstance().readMapConfig.get("HostPort"));
            }
        }

        rbGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.network)
                {
                    nSelectRtkType = 1;
                    fl_container.removeAllViewsInLayout();
                    View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.sub4g, fl_container, false);
                    fl_container.addView(inflateView);

                    EditText mac = (EditText)findViewById(R.id.Mac);
                    mac.setText(GlobalConfigVar.getInstance().ReturnMac());
                    if(bReadResult)
                    {
                        String type = ConfigFile.getInstance().readMapConfig.get("RtkType");
                        if(type.equals("1"))
                        {
                            EditText url = (EditText)findViewById(R.id.webAddress);
                            EditText qybs = (EditText)findViewById(R.id.Qybs);

                            url.setText(ConfigFile.getInstance().readMapConfig.get("4G_WebUrl"));
                            qybs.setText(ConfigFile.getInstance().readMapConfig.get("4G_Qybs"));
                            mac.setText(ConfigFile.getInstance().readMapConfig.get("4G_Mac"));
                        }
                    }
                }
                else if(checkedId == R.id.vpn)
                {
                    nSelectRtkType = 2;
                    fl_container.removeAllViewsInLayout();
                    View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.vpn, fl_container, false);
                    fl_container.addView(inflateView);

                    EditText staticIp = (EditText)findViewById(R.id.StaticIp) ;
                    staticIp.setText(GlobalConfigVar.getInstance().getEthernetIp(RtkConfigActivity.this));
                    if(bReadResult) {
                        if (ConfigFile.getInstance().readMapConfig.get("RtkType").equals("2")) {
                            EditText ip = (EditText)findViewById(R.id.IpAddr);
                            EditText port = (EditText)findViewById(R.id.netPort);

                            ip.setText(ConfigFile.getInstance().readMapConfig.get("SvrIp"));
                            port.setText(ConfigFile.getInstance().readMapConfig.get("NetPort"));
                            staticIp.setText(GlobalConfigVar.getInstance().localIp);
                        }
                    }
                }
                else if(checkedId == R.id.port)
                {
                    nSelectRtkType = 3;
                    fl_container.removeAllViewsInLayout();
                    View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.serialport, fl_container, false);
                    fl_container.addView(inflateView);

                    if(bReadResult) {
                        if (ConfigFile.getInstance().readMapConfig.get("RtkType").equals("3")) {
                            Spinner pt = (Spinner)findViewById(R.id.serialPort);
                            Spinner baud = (Spinner)findViewById(R.id.serialPortBaud);

                            int index = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("SerialPort"));
                            pt.setSelection(index);

                            index = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("SerialBaud"));
                            baud.setSelection(index);
                        }
                    }
                }
                else if(checkedId == R.id.trackdebug)
                {
                    nSelectRtkType = 4;
                    fl_container.removeAllViewsInLayout();
                    View inflateView = LayoutInflater.from(RtkConfigActivity.this).inflate(R.layout.trackdebug, fl_container, false);
                    fl_container.addView(inflateView);

                    RadioGroup rbTrackGroup = (RadioGroup)findViewById(R.id.radioGroup_track);
                    rbTrackGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            if (checkedId == R.id.debug) {
                                nSelectTrackType = 1;
                            }
                            else if(checkedId == R.id.check) {
                                nSelectTrackType = 2;
                            }
                        }
                    });

                    if(bReadResult) {
                        if (ConfigFile.getInstance().readMapConfig.get("RtkType").equals("4")) {
                            EditText ip = (EditText)findViewById(R.id.tracklocalip);
                            EditText port = (EditText)findViewById(R.id.tracknetport);

                            ip.setText(ConfigFile.getInstance().readMapConfig.get("HostIP"));
                            port.setText(ConfigFile.getInstance().readMapConfig.get("HostPort"));

                            int trackversion = Integer.parseInt(ConfigFile.getInstance().readMapConfig.get("TrackType"));
                            if(trackversion == 1) {
                                RadioButton rbdebug = (RadioButton) findViewById(R.id.debug);
                                rbdebug.setChecked(true);
                            }
                            else if(trackversion == 2) {
                                RadioButton rbcheck = (RadioButton) findViewById(R.id.check);
                                rbcheck.setChecked(true);
                            }
                        }
                    }
                }
            }
        });

        Button btnSave = (Button)findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nSelectRtkType == 0)
                {
                    Toast.makeText(RtkConfigActivity.this, "请先选择转发类型", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(nSelectRtkType == 1)
                    {
                        EditText mac = (EditText)findViewById(R.id.Mac);
                        EditText url = (EditText)findViewById(R.id.webAddress);
                        EditText qybs = (EditText)findViewById(R.id.Qybs);

                        String sUrl = url.getText().toString();
                        String sQybs = qybs.getText().toString();
                        String sMac = mac.getText().toString();
                        if(sUrl.isEmpty() || sQybs.isEmpty() || sMac.isEmpty())
                            Toast.makeText(RtkConfigActivity.this, "配置项不能为空", Toast.LENGTH_SHORT).show();
                        else {
                            ConfigFile.getInstance().writeMapConfig.put("RtkType", "1");
                            ConfigFile.getInstance().writeMapConfig.put("4G_WebUrl", sUrl);
                            ConfigFile.getInstance().writeMapConfig.put("4G_Qybs", sQybs);
                            ConfigFile.getInstance().writeMapConfig.put("4G_Mac", sMac);

                            if(ConfigFile.getInstance().setValue(RtkConfigActivity.this))
                                Toast.makeText(RtkConfigActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(nSelectRtkType == 2)
                    {
                        EditText ip = (EditText)findViewById(R.id.IpAddr);
                        EditText port = (EditText)findViewById(R.id.netPort);

                        String IP = ip.getText().toString();
                        String NETPORT = port.getText().toString();
                        if(IP.isEmpty() || NETPORT.isEmpty())
                            Toast.makeText(RtkConfigActivity.this, "配置项不能为空", Toast.LENGTH_SHORT).show();
                        else
                        {
                            ConfigFile.getInstance().writeMapConfig.put("RtkType","2");
                            ConfigFile.getInstance().writeMapConfig.put("SvrIp", IP);
                            ConfigFile.getInstance().writeMapConfig.put("NetPort", NETPORT);

                            if(ConfigFile.getInstance().setValue(RtkConfigActivity.this))
                                Toast.makeText(RtkConfigActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(nSelectRtkType == 3)
                    {
                        Spinner pt = (Spinner)findViewById(R.id.serialPort);
                        Spinner baud = (Spinner)findViewById(R.id.serialPortBaud);

                        long indexSelectPort = pt.getSelectedItemId();
                        long indexSelectBaud = baud.getSelectedItemId();
                        String selectPort = (String)pt.getSelectedItem();
                        String selectBaud = (String)baud.getSelectedItem();

                        ConfigFile.getInstance().writeMapConfig.put("RtkType","3");
                        ConfigFile.getInstance().writeMapConfig.put("SerialPort", String.valueOf(indexSelectPort));
                        ConfigFile.getInstance().writeMapConfig.put("SerialBaud", String.valueOf(indexSelectBaud));
                        ConfigFile.getInstance().writeMapConfig.put("SerialPortValue", selectPort);
                        ConfigFile.getInstance().writeMapConfig.put("SerialBaudValue", selectBaud);

                        if(ConfigFile.getInstance().setValue(RtkConfigActivity.this))
                            Toast.makeText(RtkConfigActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    }
                    else if(nSelectRtkType == 4)
                    {
                        EditText ip = (EditText)findViewById(R.id.tracklocalip);
                        EditText port = (EditText)findViewById(R.id.tracknetport);

                        String IP = ip.getText().toString();
                        String NETPORT = port.getText().toString();

                        if(IP.isEmpty() || NETPORT.isEmpty())
                            Toast.makeText(RtkConfigActivity.this, "配置项不能为空", Toast.LENGTH_SHORT).show();
                        else
                        {
                            ConfigFile.getInstance().writeMapConfig.put("RtkType","4");
                            ConfigFile.getInstance().writeMapConfig.put("HostIP", IP);
                            ConfigFile.getInstance().writeMapConfig.put("HostPort", NETPORT);
                            if(nSelectTrackType == 1)
                                ConfigFile.getInstance().writeMapConfig.put("TrackType", "1");
                            else if(nSelectTrackType == 2)
                                ConfigFile.getInstance().writeMapConfig.put("TrackType", "2");

                            if(ConfigFile.getInstance().setValue(RtkConfigActivity.this))
                                Toast.makeText(RtkConfigActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        //构造容器
        /*lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //构造LayoutInflater
        inflater = getLayoutInflater();*/
    }
}
