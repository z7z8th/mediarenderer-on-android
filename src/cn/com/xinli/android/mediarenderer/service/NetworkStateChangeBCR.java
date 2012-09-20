package cn.com.xinli.android.mediarenderer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkStateChangeBCR extends BroadcastReceiver {

	public final static String TAG = "NetworkStateChangeBCR";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		
		Log.d(TAG, "action = " + action);

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
		{
		  NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		  Log.d(TAG, "info.getState() = " + info.getState());
		  if (info.getState().equals(NetworkInfo.State.CONNECTED))
		  {
		    //do whatever you want when wifi is active and connected to a hotspot
			  Intent serviceIntent = new Intent(context,MediaRendererService.class);
			  serviceIntent.putExtra("original_intent", intent);
			  context.startService(serviceIntent);
		  }
		  else{
			  Intent serviceIntent = new Intent(context,MediaRendererService.class);
			  serviceIntent.putExtra("original_intent", intent);
			  context.stopService(serviceIntent);
		  }
		}

	}

}
