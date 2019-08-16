package com.example.zsww111.km2mapcheck;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.Toast;

import com.example.km2mapchecklib.CbManger;
import com.example.km2mapchecklib.ConfigFile;
import com.example.km2mapchecklib.Km2Entrance;
import com.example.km2mapchecklib.RtkConfigActivity;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
       // TextView tv = (TextView) findViewById(R.id.sample_text);
       // tv.setText(stringFromJNI());
       // WifiManager manager  = (WifiManager) this
           //     .(Con);
        final EditText editText1 = (EditText)findViewById(R.id.HZZH1);
        editText1.setFocusable(false);
        Boolean ret = ConfigFile.getInstance().getValue(this);
        if(!ret)
        {
            Toast.makeText(this,"先配置参数", Toast.LENGTH_SHORT).show();
        }
        else {
            CbManger.getInstance().SetNotify(new CbManger.callbackInterface() {
                @Override
                public void Notify(String str) {

                    if(str.contains("HZZH1"))
                    {
                        editText1.setText(str);
                    }
                }
            });

            Km2Entrance.getInstance().ParseMap();

            //Km2Entrance.getInstance().ReadBaseRtk_SerialPort("ttyS6", 115200);
            Km2Entrance.getInstance().ReadRtk(this);///////////////////////////////主程序要加this
            Km2Entrance.getInstance().Open();
        }
        Button btn = (Button) findViewById(R.id.btn_settings);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RtkConfigActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
