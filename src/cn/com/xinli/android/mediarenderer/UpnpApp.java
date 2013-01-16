package cn.com.xinli.android.mediarenderer;

import android.app.Application;

public class UpnpApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// important! this sax driver is necessary for cling stack
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
				
		initSingletons();

	}
	
	protected void initSingletons(){
		// Initialize the instance of MySingleton
		UpnpSingleton.initInstance(this.getApplicationContext());
	}

	public void customAppMethod(){
		// Custom application method
	}
	
}
