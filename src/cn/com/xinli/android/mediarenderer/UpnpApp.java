package cn.com.xinli.android.mediarenderer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import cn.com.xinli.android.mediarenderer.activity.StartupActivity;
import cn.com.xinli.android.mediarenderer.utils.Utils;

public class UpnpApp extends Application {
	
	private NotificationManager mNM;

    // pulsate every 1 second, indicating a relatively high degree of urgency
    private static final int NOTIFICATION_LED_ON_MS = 100;
    private static final int NOTIFICATION_LED_OFF_MS = 1000;
    private static final int NOTIFICATION_ARGB_COLOR = 0xff0088ff; // cyan

	@Override
	public void onCreate() {
		super.onCreate();
		
		// important! this sax driver is necessary for cling stack
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				
		initSingletons();

	}
	
	protected void initSingletons(){
		// Initialize the instance of MySingleton
		UpnpSingleton.initInstance(this.getApplicationContext());
	}

	public void customAppMethod(){
		// Custom application method
	}
	
	public void showNotification(String content) {
        // In this sample, we'll use the same text for the ticker and the
        // expanded notification
//        CharSequence text = getText(R.string.service_start);

        // Set the icon, scrolling text and timestamp
        // Notification notification = new Notification(R.drawable.stat_sample,
        // text, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this.getApplicationContext(), StartupActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle(getText(R.string.mediarenderer_title))
                        .setContentText(content)
                        .setTicker(
                                        this.getResources().getQuantityString(
                                                        R.plurals.session_notification_ticker, 1, 2))
                        .setDefaults(
                                        Notification.DEFAULT_SOUND
                                                        | Notification.DEFAULT_VIBRATE )
                        .setLights(NOTIFICATION_ARGB_COLOR, NOTIFICATION_LED_ON_MS,
                                        NOTIFICATION_LED_OFF_MS)
                        .setSmallIcon(R.drawable.mediarenderer)
                        .setContentIntent(contentIntent)
                        .build();
//                        .getNotification();
        
        CancelNotification();

        // Send the notification.
        // We use a layout id because it is a unique number. We use it later to
        // cancel.
        mNM.notify(R.string.mediarenderer_service_notification_id, notification);

	}
	
	public void CancelNotification(){
		mNM.cancel(R.string.mediarenderer_service_notification_id);
	}
	
}
