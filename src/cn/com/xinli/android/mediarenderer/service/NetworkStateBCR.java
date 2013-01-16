package cn.com.xinli.android.mediarenderer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkStateBCR extends BroadcastReceiver {

	public final static String TAG = "NetworkStateChangeBCR";

	private static boolean firstConnect = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		// important! this sax driver is necessary for cling stack
//		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		
		Log.d(TAG, "action = " + action);
		
		// to debug ethernet/wifi interface
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo =  connectivityManager.getActiveNetworkInfo();
		
		if (activeNetInfo != null ){
			
			if (firstConnect) {
				
				firstConnect = false;
			    //do whatever you want when wifi is active and connected to a hotspot
				Intent serviceIntent = new Intent(context,BackendService.class);
				serviceIntent.putExtra("original_intent", intent);
				context.startService(serviceIntent);
				
				Log.d(TAG, "service stared");
			}
			  
		} else {
			if (activeNetInfo == null){
				firstConnect = true;
				
				Intent serviceIntent = new Intent(context,BackendService.class);
				serviceIntent.putExtra("original_intent", intent);
				context.stopService(serviceIntent);
				
				Log.d(TAG, "service closed");
			}
		}

	}
	

}
