package cn.com.xinli.android.mediarenderer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;
import cn.com.xinli.android.mediarenderer.UpnpApp;
import cn.com.xinli.android.mediarenderer.UpnpSingleton;

public class MusicPlayerService extends Service {
	
	private static final String TAG = "MusicPlayerService";
	
	private MediaPlayer mPlayer;
	private OnPreparedListener preparedListener = new OnPreparedListener() {
		
		@Override
		public void onPrepared(MediaPlayer mp) {
			UpnpSingleton.getInstance().setMusicMediaPlayer(mp);
		}
	};
	private OnCompletionListener completionListener = new OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			UpnpApp application = (UpnpApp)getApplication();
			application.showNotification("Service on");
		}
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null) return -1;
		String s= intent.getStringExtra("uri");
		if(s == null) return -1;
		Uri uri = Uri.parse(s);
		String title = intent.getStringExtra("title");
		mPlayer = MediaPlayer.create(this, uri);
		
		UpnpApp application = (UpnpApp)getApplication();
		application.showNotification(title);
		
		mPlayer.setOnPreparedListener(preparedListener);
		mPlayer.setOnCompletionListener(completionListener);
		
		mPlayer.start();
		
		return super.onStartCommand(intent, flags, startId);
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPlayer.stop();
		
		UpnpApp application = (UpnpApp)getApplication();
		application.showNotification("Service on");
	}



	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
