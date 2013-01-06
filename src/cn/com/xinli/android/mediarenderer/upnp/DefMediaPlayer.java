package cn.com.xinli.android.mediarenderer.upnp;

import java.net.URI;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import cn.com.xinli.android.mediarenderer.R;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class DefMediaPlayer extends Fragment implements OnCompletionListener{
	
	final private static String TAG = "DefMediaPlayer";

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
    
    private long trackDurationTime;
    
    final Activity mActivity = this.getActivity();

    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_videoview, container,false);
    	videoView = (VideoView) mRootView.findViewById(R.id.myvideoview);
    	
    	videoView.setKeepScreenOn(true);
    	videoView.setOnPreparedListener(new OnPreparedListener(){
    	
    			@Override
    			public void onPrepared(MediaPlayer mediaPlayer) {
    				mMediaPlayer = mediaPlayer;
    				
    				trackDurationTime = videoView.getDuration();//this returned the duration 
    				
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
    			}});
    	
    	videoView.setOnCompletionListener(this);
    	
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
    	Log.d(TAG,"getPositionInfo = " + videoView.getCurrentPosition());
    	
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

    synchronized public void setURI(final URI uri) {
    	Log.d(TAG,"setURI is called");
//    	if (videoView.isPlaying())
//    		videoView.stopPlayback();
//    	videoView.setVideoURI(Uri.parse(uri.toString()));
//        stop();
//        super.setURI(uri);
//        currentMediaInfo = new MediaInfo(uri.toString(), "");
//    	Log.d(TAG,"this.getDuration() = " + videoView.getDuration());
    	
    	getActivity().runOnUiThread(new Runnable(){

			@Override
			public void run() {
				videoView = (VideoView) mRootView.findViewById(R.id.myvideoview);
		        Uri video = Uri.parse(uri.toString());
		        videoView.setVideoURI(video);
				Log.d(TAG, "set uri in thread");
			}});
    	
    	currentMediaInfo =
                new MediaInfo(
                		uri.toString(),
                        "",
                        new UnsignedIntegerFourBytes(1),
                        ModelUtil.toTimeString(trackDurationTime / 1000),
                        StorageMedium.NETWORK
                );
        currentPositionInfo = new PositionInfo(1, "", uri.toString());

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );

        transportStateChanged(TransportState.STOPPED);
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
    
    public void seekTo(int msec){
    	videoView.seekTo(msec);
    	videoView.start();
    	transportStateChanged(TransportState.PLAYING);
    }
    
    
	public void pause() {
		Log.d(TAG,"pause is called");
		videoView.pause();
		
		transportStateChanged(TransportState.PAUSED_PLAYBACK);
	}

	public void play() {
		videoView.requestFocus();
        videoView.start();
        
		transportStateChanged(TransportState.PLAYING);
	}

	public void stop() {
		// TODO Auto-generated method stub
		Log.d(TAG,"stopPlayback is called");
		videoView.stopPlayback();
		
		transportStateChanged(TransportState.STOPPED);
	}
	
	public void endOfMedia(){
		Log.d(TAG,"DefMediaPlayer endOfMedia");
		getActivity().finish();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG,"videoView onCompletion");
//		getActivity().finish();

		videoView.stopPlayback();
		transportStateChanged(TransportState.STOPPED);
	}

    
    /*
    protected class GstMediaListener implements MediaListener {

        public void pause(StopEvent evt) {
            transportStateChanged(TransportState.PAUSED_PLAYBACK);
        }

        public void start(StartEvent evt) {
            transportStateChanged(TransportState.PLAYING);
        }

        public void stop(StopEvent evt) {
            transportStateChanged(TransportState.STOPPED);
        }

        public void endOfMedia(EndOfMediaEvent evt) {
            log.fine("End Of Media event received, stopping media player backend");
            GstMediaPlayer.this.stop();
        }

        public void positionChanged(PositionChangedEvent evt) {
            log.fine("Position Changed event received: " + evt.getPosition());
            synchronized (GstMediaPlayer.this) {
                currentPositionInfo =
                        new PositionInfo(
                                1,
                                currentMediaInfo.getMediaDuration(),
                                currentMediaInfo.getCurrentURI(),
                                ModelUtil.toTimeString(evt.getPosition().toSeconds()),
                                ModelUtil.toTimeString(evt.getPosition().toSeconds())
                        );
            }
        }

        public void durationChanged(final DurationChangedEvent evt) {
            log.fine("Duration Changed event received: " + evt.getDuration());
            synchronized (GstMediaPlayer.this) {
                String newValue = ModelUtil.toTimeString(evt.getDuration().toSeconds());
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
    }*/
	
}
