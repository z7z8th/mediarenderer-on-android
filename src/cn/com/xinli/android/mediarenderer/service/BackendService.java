package cn.com.xinli.android.mediarenderer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cn.com.xinli.android.mediarenderer.R;
import cn.com.xinli.android.mediarenderer.UpnpApp;
import cn.com.xinli.android.mediarenderer.UpnpSingleton;

/**
 * only for upnp brower can look for this device
 * @author chen
 *
 */
public class BackendService extends Service {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		getApplicationContext().bindService(
                new Intent(this, MediaRendererServiceImpl.class),
                UpnpSingleton.getInstance().getServiceConnection(),
                Context.BIND_AUTO_CREATE
        );
		
		UpnpApp application = (UpnpApp)getApplication();
		application.showNotification(getText(R.string.service_start).toString());
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getApplicationContext().unbindService(UpnpSingleton.getInstance().getServiceConnection());
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
