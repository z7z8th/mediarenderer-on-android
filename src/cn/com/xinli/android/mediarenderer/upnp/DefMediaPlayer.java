package cn.com.xinli.android.mediarenderer.upnp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import cn.com.xinli.android.mediarenderer.R;
import cn.com.xinli.android.mediarenderer.UpnpSingleton;

public class DefMediaPlayer extends Fragment 
	implements OnPreparedListener, OnErrorListener, OnCompletionListener{
	
	final private static String TAG = "DefMediaPlayer";
	private static final String PACKAGE_NAME = "cn.com.xinli.android.mediarenderer";
	private static final String ACTIVITY_NAME = "cn.com.xinli.android.mediarenderer.activity.PlayerActivity";

    private UnsignedIntegerFourBytes instanceId;
    private LastChange avTransportLastChange;
    private LastChange renderingControlLastChange;

    // We'll synchronize read/writes to these fields
    private volatile TransportInfo currentTransportInfo = new TransportInfo();
    private PositionInfo currentPositionInfo = new PositionInfo();
    private MediaInfo currentMediaInfo = new MediaInfo();
    private double storedVolume;
    
//    private VideoView mVideoView;
    private MediaPlayer mMediaPlayer;
    private float mVolume;
    
    private ViewGroup mRootView;
    private VideoView videoView;
    private ImageView imageView;
    private MediaController mediaController;
    
    private long trackDurationTime;
    
    /* FROM DIDL CONTENT
    <upnp:class>object.item.audioItem.musicTrack</upnp:class>
	<upnp:class>object.item.videoItem</upnp:class>
	<upnp:class>object.item.imageItem</upnp:class>
    */
    private final static String videoTypePrefix = "http-get:*:video/";
    private final static String audioTypePrefix = "http-get:*:audio/";
    private final static String imageTypePrefix = "http-get:*:image/";
    private final static int VIDEOTYPE = 0;
    private final static int AUDIOTYPE = 1;
    private final static int IMAGETYPE = 2;
    private static int upnpItemType = 0;
    private static String upnpItemId = "temp.jpg";
    
    File cacheDir;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
    	mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_videoview, container,false);
    	
    	imageView = (ImageView) mRootView.findViewById(R.id.myimageview);
    	videoView = (VideoView) mRootView.findViewById(R.id.myvideoview);
    	
		mediaController = new MediaController(getActivity());
    	
    	videoView.setKeepScreenOn(true);
    	videoView.setOnPreparedListener(this);
    	videoView.setOnErrorListener(this);
    	videoView.setOnCompletionListener(this);
    	
    	cacheDir = getActivity().getCacheDir();
    	
    	return mRootView;
	}

	public void init(UnsignedIntegerFourBytes instanceId,
            LastChange avTransportLastChange,
            LastChange renderingControlLastChange) {
    	Log.d(TAG,"DefMediaPlayer initialized");
		this.instanceId = instanceId;
		this.avTransportLastChange = avTransportLastChange;
		this.renderingControlLastChange = renderingControlLastChange;
		
    }

	public UnsignedIntegerFourBytes getInstanceId() {
        return instanceId;
    }

    public LastChange getAvTransportLastChange() {
        return avTransportLastChange;
    }

    public LastChange getRenderingControlLastChange() {
        return renderingControlLastChange;
    }
    
	synchronized public TransportInfo getCurrentTransportInfo() {
        return currentTransportInfo;
    }

    synchronized public PositionInfo getCurrentPositionInfo() {
    	
        currentPositionInfo =
                new PositionInfo(
                        1,
                        currentMediaInfo.getMediaDuration(),
                        currentMediaInfo.getCurrentURI(),
                        ModelUtil.toTimeString(videoView.getCurrentPosition()/1000),
                        ModelUtil.toTimeString(videoView.getCurrentPosition()/1000)
                );
        
        return currentPositionInfo;
    }

    synchronized public MediaInfo getCurrentMediaInfo() {
        return currentMediaInfo;
    }
    
    synchronized public void setVolume (float leftVolume, float rightVolume) {
	  mMediaPlayer.setVolume(leftVolume,rightVolume);
	  mVolume = leftVolume;
	}
    
    public float getVolume(){
    	return mVolume;
    }

    synchronized public void setURI(final URI uri, final String metaData) {

    	Intent intent = new Intent();
    	intent.setClassName(PACKAGE_NAME, ACTIVITY_NAME);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	UpnpSingleton.getInstance().getApplicationContext().startActivity(intent);

    	// DIDL fragment parsing and handling of currentURIMetaData
    	DIDLParser parser = new DIDLParser();
        try {
			DIDLContent didl =parser.parse(metaData);
			List<Item> items = didl.getItems();
			if (items != null && items.size() > 0){
				Item itemOne = items.get(0);
				Res resource = itemOne.getResources().get(0);
				upnpItemId = itemOne.getTitle();
				String protocolInfo = resource.getProtocolInfo().toString();
				if (protocolInfo.startsWith(videoTypePrefix))
					upnpItemType = VIDEOTYPE;
				else if(protocolInfo.startsWith(audioTypePrefix))
					upnpItemType = AUDIOTYPE;
				else if (protocolInfo.startsWith(imageTypePrefix))
					upnpItemType = IMAGETYPE;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG,"CurrentURIMetaData parse error");
		}
        
        if (getActivity() == null){
			Log.d(TAG, "getActivity() is null");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setURI(uri,metaData);
			return;
		} 
        
        // recycle bitmap
        getActivity().runOnUiThread(new Runnable(){

			@Override
			public void run() {
		        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
				if (bitmapDrawable != null) {
					Bitmap bitmap = bitmapDrawable.getBitmap();
					if (bitmap != null && !bitmap.isRecycled())	bitmap.recycle();
				}
			}});
        
        switch (upnpItemType) {
		case VIDEOTYPE:
			
			// Video show
	    	getActivity().runOnUiThread(new Runnable(){

				@Override
				public void run() {
					videoView.stopPlayback();
					videoView.setVisibility(View.VISIBLE);
		        	imageView.setVisibility(View.INVISIBLE);
		        	videoView.bringToFront();
		        	Log.d(TAG,"uri = " + uri.toString());
			        videoView.setVideoPath(uri.toString());
			        videoView.setMediaController(mediaController);
			        videoView.requestFocus();
				}});
	    	
			break;

		case AUDIOTYPE:
			
			break;
			
		case IMAGETYPE:
			// Image show
			getActivity().runOnUiThread(new Runnable(){
        		@Override
				public void run() {
        			videoView.stopPlayback();
        			videoView.setVisibility(View.INVISIBLE);
                	imageView.setVisibility(View.VISIBLE);
                	imageView.bringToFront();
                	File bitmapFile = new File(cacheDir, upnpItemId);
                	Log.d(TAG,"upnpItemId = " + upnpItemId );
                	Log.d(TAG,"bitmapFile = " + bitmapFile.toString() );
                	setImage(imageView, uri.toString(), bitmapFile);
        		}
        	});
			break;
		}
        
    	currentMediaInfo =
                new MediaInfo(
                		uri.toString(),
                		metaData,
                        new UnsignedIntegerFourBytes(1),
                        ModelUtil.toTimeString(trackDurationTime / 1000),
                        StorageMedium.NETWORK
                );
        currentPositionInfo = new PositionInfo(1, metaData, uri.toString());

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );
    	
    	transportStateChanged(TransportState.TRANSITIONING);
    }
    
    synchronized public void setVolume(double volume) {
        storedVolume = getVolume();
        setVolume((float)volume,(float)volume);
        
        ChannelMute switchedMute =
                (storedVolume == 0 && volume > 0) || (storedVolume > 0 && volume == 0)
                        ? new ChannelMute(Channel.Master, storedVolume > 0 && volume == 0)
                        : null;

        getRenderingControlLastChange().setEventedValue(
                getInstanceId(),
                new RenderingControlVariable.Volume(
                        new ChannelVolume(Channel.Master, (int) (volume * 100))
                ),
                switchedMute != null
                        ? new RenderingControlVariable.Mute(switchedMute)
                        : null
        );
    }

    synchronized public void setMute(boolean desiredMute) {
        if (desiredMute && getVolume() > 0) {
            setVolume(0);
        } else if (!desiredMute && getVolume() == 0) {
            setVolume(storedVolume);
        }
    }

    // Because we don't have an automated state machine, we need to calculate the possible transitions here

    synchronized public TransportAction[] getCurrentTransportActions() {
        TransportState state = currentTransportInfo.getCurrentTransportState();
        TransportAction[] actions;

        switch (state) {
            case STOPPED:
                actions = new TransportAction[]{
                        TransportAction.Play
                };
                break;
            case PLAYING:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek
                };
                break;
            case PAUSED_PLAYBACK:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek,
                        TransportAction.Play
                };
                break;
            default:
                actions = null;
        }
        return actions;
    }

    synchronized protected void transportStateChanged(TransportState newState) {
        TransportState currentTransportState = currentTransportInfo.getCurrentTransportState();
        Log.d(TAG,"Current state is: " + currentTransportState + ", changing to new state: " + newState);
        currentTransportInfo = new TransportInfo(newState);

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.TransportState(newState),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions())
        );
    }

    public boolean isPlaying(){
    	return videoView.isPlaying();
    }
    
    public void seekTo(final int msec){
    	
    	getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run() {
				videoView.seekTo(msec);
		    	videoView.start();
			}
		});
    	
    	transportStateChanged(TransportState.PLAYING);
    }
    
    
	public void pause() {
		Log.d(TAG,"pause is called");
		
		getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run() {
				videoView.pause();
			}
		});
		
		transportStateChanged(TransportState.PAUSED_PLAYBACK);
	}

	public void play() {
		Intent intent = new Intent();
    	intent.setClassName(PACKAGE_NAME, ACTIVITY_NAME);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	UpnpSingleton.getInstance().getApplicationContext().startActivity(intent);
    	
		if (upnpItemType == VIDEOTYPE) {
	        
	        getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run() {
				    videoView.bringToFront();
			        videoView.requestFocus();
			        videoView.start();
			        
				    mediaController.show(5000);
				}
			});
		} 
		
		transportStateChanged(TransportState.PLAYING);
	}

	public void stop() {
		// TODO Auto-generated method stub
		if (upnpItemType == VIDEOTYPE) {
			Log.d(TAG,"stopPlayback is called");
			
			getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run() {
					if (videoView.isPlaying()){ 
						videoView.stopPlayback();
						getActivity().moveTaskToBack(true);
					}
				}
			});
		}
		
		transportStateChanged(TransportState.STOPPED);
	}
	
	public void endOfMedia(){
		Log.d(TAG,"DefMediaPlayer endOfMedia");
		getActivity().moveTaskToBack(true);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG,"videoView onCompletion");
//		mp.stop();
		
		getActivity().moveTaskToBack(true);
		
		transportStateChanged(TransportState.STOPPED);
	}

	private void setImage(ImageView view, String url, File bitmapFile) {
    	class IntializeViewImageTask extends AsyncTask<Object, Void, Bitmap> {

            private ImageView view;
            private String url;
            private File bitmapFile;
            
            /**
             * Loads a bitmap from the specified url.
             * 
             * @param url The location of the bitmap asset
             * @return The bitmap, or null if it could not be loaded
             * @throws IOException
             * @throws MalformedURLException
             */
            public Bitmap getBitmap(final String string, Object fileObj) throws MalformedURLException, IOException {
            	
                File file = (File)fileObj;        
                // Get the source image's dimensions
                int desiredWidth = 1000;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                if (file != null && file.isFile()) {
                	BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                } else {
                	InputStream is = (InputStream) new URL(string).getContent();
                    BitmapFactory.decodeStream(is, null, options);
                    is.close();
                }
                int srcWidth = options.outWidth;
                int srcHeight = options.outHeight;

                // Only scale if the source is big enough. This code is just trying
                // to fit a image into a certain width.
                if (desiredWidth > srcWidth)
                    desiredWidth = srcWidth;

                // Calculate the correct inSampleSize/scale value. This helps reduce
                // memory use. It should be a power of 2
                int inSampleSize = 1;
                while (srcWidth / 2 > desiredWidth) {
                    srcWidth /= 2;
                    srcHeight /= 2;
                    inSampleSize *= 2;
                }
                // Decode with inSampleSize
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inSampleSize = inSampleSize;
                options.inScaled = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inPurgeable = true;
                Bitmap sampledSrcBitmap;
                if (file != null && file.isFile()) {
                	sampledSrcBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                } else {
                    InputStream is = (InputStream) new URL(string).getContent();
                    sampledSrcBitmap = BitmapFactory.decodeStream(is, null, options);
                    is.close();
                }
                return sampledSrcBitmap;
            }

           @Override
            protected Bitmap doInBackground(Object... params) {
                view = (ImageView) params[0];
                url = String.valueOf(params[1]);
                bitmapFile = (File) params[2];
                
                try {
					return getBitmap(url,bitmapFile);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                
            }

            @Override
            protected void onPostExecute(final Bitmap result) {
            	super.onPostExecute(result);
            	final ImageView view = (ImageView) getActivity().findViewById(R.id.myimageview);
            	
            	ObjectAnimator visToInvis = ObjectAnimator.ofFloat(view, "rotationY", 0f, 90f);
                visToInvis.setDuration(1000);
                visToInvis.setInterpolator(decelerator);
                
                final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(imageView, "rotationY", -90f, 0f);
                invisToVis.setDuration(1000);
                invisToVis.setInterpolator(decelerator);
                
                visToInvis.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator anim) {
                    	view.setImageBitmap(result);
                        view.setVisibility(View.VISIBLE);
                        view.bringToFront();
                        view.requestFocus();
                        invisToVis.start();
                    }
                });
                visToInvis.start();
                
//                view.setImageBitmap(result);
//                view.setVisibility(View.VISIBLE);
//                view.bringToFront();
//                view.requestFocus();
            	
            }
        }

        new IntializeViewImageTask().execute(view, url, bitmapFile);
    }

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG,"videoView onError");
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG,"videoView onprepared");
		Log.d(TAG,"trackDurationTime = " + mp.getDuration() );
		
		trackDurationTime = videoView.getDuration();//this returned the duration
		
		Log.d(TAG,"trackDurationTime from videoview= " + trackDurationTime );
		
		synchronized (DefMediaPlayer.this) {
            String newValue = ModelUtil.toTimeString(trackDurationTime / 1000);
            currentMediaInfo =
                    new MediaInfo(
                            currentMediaInfo.getCurrentURI(),
                            "",
                            new UnsignedIntegerFourBytes(1),
                            newValue,
                            StorageMedium.NETWORK
                    );

            getAvTransportLastChange().setEventedValue(
                    getInstanceId(),
                    new AVTransportVariable.CurrentTrackDuration(newValue),
                    new AVTransportVariable.CurrentMediaDuration(newValue)
            );
        }
	}
	
	private Interpolator accelerator = new AccelerateInterpolator();
    private Interpolator decelerator = new DecelerateInterpolator();
	
}
