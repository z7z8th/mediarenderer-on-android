package cn.com.xinli.android.mediarenderer.display;

import android.util.Log;
import cn.com.xinli.android.mediarenderer.upnp.DefMediaPlayer;

public class MediaPlayerHandler implements DisplayHandler{
	
	final static String TAG = "MediaPlayerHandler";
	
	@Override
	public void onNoMedia(DefMediaPlayer player) {
		Log.d(TAG,"onNoMedia");
	}
	
	@Override
	public void onPlay(DefMediaPlayer player) {
		Log.d(TAG,"onPlay");
	}

	@Override
	public void onStop(DefMediaPlayer player) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onStop");

	}
	
	

}
