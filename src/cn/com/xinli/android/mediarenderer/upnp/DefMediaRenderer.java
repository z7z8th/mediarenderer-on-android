package cn.com.xinli.android.mediarenderer.upnp;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import cn.com.xinli.android.mediarenderer.MainActivity;
import cn.com.xinli.android.mediarenderer.R;
import cn.com.xinli.android.mediarenderer.display.DisplayHandler;
import cn.com.xinli.android.mediarenderer.service.MediaRendererServiceImpl;
import cn.com.xinli.android.mediarenderer.state.MediaRendererNoMediaPresent;
import cn.com.xinli.android.mediarenderer.state.MediaRendererStateMachine;
import cn.com.xinli.android.mediarenderer.upnp.AAVTransportService;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

public class DefMediaRenderer extends FragmentActivity{

	public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 2000;

    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single service
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    protected Map<UnsignedIntegerFourBytes, DefMediaPlayer> mediaPlayers;

    protected ServiceManager<DefConnectionManagerService> connectionManager;
    protected LastChangeAwareServiceManager<DefAVTransportService> avTransport;
    protected LastChangeAwareServiceManager<DefAudioRenderingControl> renderingControl;

    protected LocalDevice device;

    protected DisplayHandler displayHandler;
    
    public NotificationManager mNM;

	// pulsate every 1 second, indicating a relatively high degree of urgency
	private static final int NOTIFICATION_LED_ON_MS = 100;
	private static final int NOTIFICATION_LED_OFF_MS = 1000;
	private static final int NOTIFICATION_ARGB_COLOR = 0xff0088ff; // cyan
	
	private AndroidUpnpService upnpService;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            
            upnpService.getRegistry().addDevice(getDevice());
            
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };
    

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
//	public DefMediaRenderer(Context context, int numberOfPlayers) {
		
    	this.displayHandler = new DisplayHandler(this);
    	
//    	int numberOfPlayers = getIntent().getIntExtra("numberOfPlayers", 0);

        // This is the backend which manages the actual player instances
        mediaPlayers = new DefMediaPlayers(this,
        		1,
                avTransportLastChange,
                renderingControlLastChange
        ){
            // These overrides connect the player instances to the output/display
            @Override
            protected void onPlayerPlay(DefMediaPlayer player) {
                getDisplayHandler().onPlay(player);
            }

            @Override
            protected void onPlayerStop(DefMediaPlayer player) {
                getDisplayHandler().onStop(player);
            }
            
            @Override
            protected void onPlayerPaused(DefMediaPlayer player) {
                getDisplayHandler().onPaused(player);
            }
            
        };

        // The connection manager doesn't have to do much, HTTP is stateless
        /*LocalService<DefConnectionManagerService> connectionManagerService = binder.read(DefConnectionManagerService.class);
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new DefConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);*/
        LocalService<ConnectionManagerService> connectionManagerService =
	            new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
	
        connectionManagerService.setManager(
	    		new DefaultServiceManager<ConnectionManagerService>(connectionManagerService, null) {
	    	        @Override
	    	        protected ConnectionManagerService createServiceInstance() throws Exception {
	    	            return new ConnectionManagerService();
	    	        }
	    	    }
	    );
        

        // The AVTransport just passes the calls on to the backend players
        LocalService<DefAVTransportService> avTransportService = binder.read(DefAVTransportService.class);
        avTransport =
                new LastChangeAwareServiceManager<DefAVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected DefAVTransportService createServiceInstance() throws Exception {
                        return new DefAVTransportService(avTransportLastChange, mediaPlayers);
                    }

					@Override
					protected int getLockTimeoutMillis() {
						return 2000;
					}
                    
                };
        avTransportService.setManager(avTransport);
       /* LocalService<AAVTransportService> avTransportService =
                new AnnotationLocalServiceBinder().read(AAVTransportService.class);
  
        avTransport =
                new LastChangeAwareServiceManager<AAVTransportService>(avTransportService,new AVTransportLastChangeParser()) {
                    
					@Override
                    protected AAVTransportService createServiceInstance() throws Exception {
                    	return new AAVTransportService(
                                MediaRendererStateMachine.class,   // All states
                                MediaRendererNoMediaPresent.class,  // Initial state
                                displayHandler
                        );
                    }
                    
                    @Override
					protected int getLockTimeoutMillis() {
						return 2000;
					}
                };
        avTransportService.setManager(avTransport);*/

        // The Rendering Control just passes the calls on to the backend players
        LocalService<DefAudioRenderingControl> renderingControlService = binder.read(DefAudioRenderingControl.class);
        renderingControl =
                new LastChangeAwareServiceManager<DefAudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected DefAudioRenderingControl createServiceInstance() throws Exception {
                        return new DefAudioRenderingControl(renderingControlLastChange, mediaPlayers);
                    }
                };
        renderingControlService.setManager(renderingControl);
        
        try {

            device = new LocalDevice(
                    new DeviceIdentity(UDN.uniqueSystemIdentifier("Xinli MediaRenderer")),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            "MediaRenderer on " + ModelUtil.getLocalHostName(false),
                            new ManufacturerDetails("Xinli", "http://www.xinli.com.cn/"),
                            new ModelDetails("Xinli MediaRenderer", "MediaRenderer on Android", "1", "http://www.xinli.com.cn/android/mediarenderer/")
                    ),
                    new Icon[]{createDefaultDeviceIcon()},
                    new LocalService[]{
                            avTransportService,
                            renderingControlService,
                            connectionManagerService
                    }
            );

        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        runLastChangePushThread();
        
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// show the icon in the status bar
		showNotification();
		
		getApplicationContext().bindService(
                new Intent(this, MediaRendererServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }
    
    @Override
	public void onDestroy() {
    	super.onDestroy();
		// Cancel the notification -- we use the same ID that we had used to
		// start it
		mNM.cancel(R.string.mediarenderer_service_notification_id);

		// Tell the user we stopped.
		Toast.makeText(this, R.string.mediarenderer_service_finished,
				Toast.LENGTH_SHORT).show();
//		upnpService.getControlPoint().getRegistry().removeDevice(getDevice());
		
		getApplicationContext().unbindService(serviceConnection);
	}
    
    private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.service_start);

		// Set the icon, scrolling text and timestamp
		// Notification notification = new Notification(R.drawable.stat_sample,
		// text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle("Title")
				.setContentText("Content")
				.setTicker(
						this.getResources().getQuantityString(
								R.plurals.session_notification_ticker, 1, 2))
				.setDefaults(
						Notification.DEFAULT_SOUND
								| Notification.DEFAULT_VIBRATE)
				.setLights(NOTIFICATION_ARGB_COLOR, NOTIFICATION_LED_ON_MS,
						NOTIFICATION_LED_OFF_MS)
				.setSmallIcon(R.drawable.stat_sample)
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis()).getNotification();

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.mediarenderer_service_notification_id, notification);

	}

    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                	Log.d("runLastChangePushThread", "runLastChangePushThread is running");
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange();
                        renderingControl.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.start();
    }

    public LocalDevice getDevice() {
        return device;
    }

    synchronized public DisplayHandler getDisplayHandler() {
        return displayHandler;
    }

    synchronized public void setDisplayHandler(DisplayHandler displayHandler) {
        this.displayHandler = displayHandler;
    }

    synchronized public Map<UnsignedIntegerFourBytes, DefMediaPlayer> getMediaPlayers() {
        return mediaPlayers;
    }

    synchronized public void stopAllMediaPlayers() {
        for (DefMediaPlayer mediaPlayer : mediaPlayers.values()) {
            TransportState state =
                mediaPlayer.getCurrentTransportInfo().getCurrentTransportState();
            if (!state.equals(TransportState.NO_MEDIA_PRESENT) ||
                    state.equals(TransportState.STOPPED)) {
//                MediaRenderer.APP.log(Level.FINE, "Stopping player instance: " + mediaPlayer.getInstanceId());
//                mediaPlayer.stop();
            	mediaPlayer.endOfMedia();
            }
        }
    }

    public ServiceManager<DefConnectionManagerService> getConnectionManager() {
        return connectionManager;
    }

    public ServiceManager<DefAVTransportService> getAvTransport() {
        return avTransport;
    }

    public ServiceManager<DefAudioRenderingControl> getRenderingControl() {
        return renderingControl;
    }

    protected Icon createDefaultDeviceIcon() {
//    	return null;
//        String iconPath = "mediarenderer.png";
        BitmapDrawable bitDw = ((BitmapDrawable) getResources().getDrawable(R.drawable.mediarenderer));
        Bitmap bitmap = bitDw.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        System.out.println("........length......"+imageInByte);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
        
        try {
            return new Icon(
                    "image/png",
                    48, 48, 8,
                    URI.create("icon.png"),
//                    DefMediaRenderer.class.getResourceAsStream(iconPath)
                    bis
            );
        } catch (IOException ex) {
            throw new RuntimeException("Could not load icon", ex);
        }
    }
}
