package cn.com.xinli.android.mediarenderer.display;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.VideoView;
import cn.com.xinli.android.mediarenderer.R;
import cn.com.xinli.android.mediarenderer.upnp.DefMediaPlayer;
import cn.com.xinli.android.mediarenderer.upnp.DefMediaRenderer;

public class DisplayHandler extends Handler{
	
	private DefMediaRenderer MainActivity;
	private DefMediaPlayer player;
	
	public final static int SETAVTRANSPORTURI = 0;
	public final static int PLAYING = 1;
	public final static int STOPPED = 2;
	public final static int PAUSED = 3;
	public final static int SEEKING = 4;
	 
	public DisplayHandler(DefMediaRenderer a){
	  this.MainActivity = a;
	}

    public void onPlay(final DefMediaPlayer player){
    	MainActivity.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Log.d("DisplayHandler","onPlay");
		    	
		    	sendEmptyMessage(PLAYING);
				
			}});
    	
    }

    public void onStop(DefMediaPlayer player){
    	MainActivity.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Log.d("DisplayHandler","onStop");
		    	
				sendEmptyMessage(STOPPED);
				
			}});
    }
    
    public void onPaused(DefMediaPlayer player){
    	MainActivity.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Log.d("DisplayHandler","onPaused");
		    	
				sendEmptyMessage(PAUSED);
				
			}});
    }
    
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		
		VideoView videoView = (VideoView) MainActivity.findViewById(R.id.myvideoview);
		
		switch (msg.what) {
		case SETAVTRANSPORTURI:
			break;
		case PLAYING:
			// Playing
	        try{
	        	videoView.setVisibility(View.VISIBLE);
	        	
		        MediaController mediaController = new MediaController(MainActivity);
		        mediaController.setAnchorView(videoView);
//		        
		        mediaController.setMediaPlayer(videoView);
//		        // Set video link (mp4 format )
//		        Uri video = Uri.parse("http://192.168.1.103:5001/get/0$0$0/dingding.mp4");
		        videoView.setMediaController(mediaController);
//		        videoView.setVideoURI(video);
	        	
		        videoView.requestFocus();
		        videoView.start();
	        }
	        catch(Exception e){
	        	e.printStackTrace();
	        }
			Log.d("DisplayHandler","DisplayHandler Playing");
			break;

		case STOPPED:
			// Stopped
			if (videoView != null){
				videoView.stopPlayback();
				videoView.setVisibility(View.INVISIBLE);
			}
			
			Log.d("DisplayHandler","DisplayHandler Stopped");
			break;
		case PAUSED:
			if (videoView != null){
				videoView.pause();
			}
			break;
		case SEEKING:
			break;
		}
	}
    
    

}
