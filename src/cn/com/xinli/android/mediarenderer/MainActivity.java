package cn.com.xinli.android.mediarenderer;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import cn.com.xinli.android.mediarenderer.service.MediaRendererService;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static String TAG = "MainActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*Button close = (Button) findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent serviceIntent = new Intent(MainActivity.this,MediaRendererService.class);
		    	stopService(serviceIntent);
				
			}});*/
        
        // to debug ethernet/wifi interface
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // We can't listen to "is available" or simply "is switched on", we have to make sure it's connected            
        NetworkInterface ethernet = null;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : interfaces) {
                if (iface.getDisplayName().equals("eth0")) {
                    ethernet = iface;
                    break;
                }
            }
            Log.d(TAG, "wifiInfo.isConnected() = " + wifiInfo.isConnected());
            Log.d(TAG, "ethernet = " + ethernet);
            if (ethernet != null)
            	Log.d(TAG,"ethernet.isUp() = " + ethernet.isUp());
            
            if (!wifiInfo.isConnected() && (ethernet != null) && !ethernet.isUp()) {
                Toast.makeText(this, "WiFi state changed, trying to disable router", Toast.LENGTH_SHORT).show();
            } else {
            	Toast.makeText(this, "WiFi state changed, trying to enable router", Toast.LENGTH_SHORT).show();
            }
        } catch (SocketException sx) {
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
