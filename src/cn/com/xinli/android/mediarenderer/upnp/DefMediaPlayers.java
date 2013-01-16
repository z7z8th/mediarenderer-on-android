package cn.com.xinli.android.mediarenderer.upnp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;


public class DefMediaPlayers extends ConcurrentHashMap<UnsignedIntegerFourBytes, DefMediaPlayer> {

	final private static Logger log = Logger.getLogger(DefMediaPlayers.class.getName());
    final private static String TAG = "DefMediaPlayers";

    final protected LastChange avTransportLastChange;
    final protected LastChange renderingControlLastChange;

    public DefMediaPlayers(int numberOfPlayers,
                           LastChange avTransportLastChange,
                           LastChange renderingControlLastChange) {
    	super(numberOfPlayers);
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;
        
        for (int i = 0; i < numberOfPlayers; i++) {
        	DefMediaPlayer player =
                    new DefMediaPlayer() {
                        @Override
                        protected void transportStateChanged(TransportState newState) {
                            super.transportStateChanged(newState);
                            if (newState.equals(TransportState.PLAYING)) {
                                onPlayerPlay(this);
                            } else if (newState.equals(TransportState.STOPPED)) {
                                onPlayerStop(this);
                            } else if (newState.equals(TransportState.PAUSED_PLAYBACK)) {
                            	onPlayerPaused(this);
                            } else if (newState.equals(TransportState.NO_MEDIA_PRESENT)) {
                            	onPlayerNoMedia(this);
                            } 
                        }
                        
                    };
                    
            player.init(new UnsignedIntegerFourBytes(i),
            		avTransportLastChange,
                    renderingControlLastChange);
            
            put(player.getInstanceId(), player);
            
//            if (context instanceof DefMediaRenderer)
//            	((DefMediaRenderer)context).getSupportFragmentManager().beginTransaction().add(R.id.MediaRendererRoot, player, "videoview").commit();
        }
    }
    
    protected void onPlayerNoMedia(DefMediaPlayer player) {
        log.fine("Player is no media: " + player.getInstanceId());
    }

    protected void onPlayerPlay(DefMediaPlayer player) {
        log.fine("Player is playing: " + player.getInstanceId());
    }

    protected void onPlayerStop(DefMediaPlayer player) {
        log.fine("Player is stopping: " + player.getInstanceId());
    }    
    
    protected void onPlayerPaused(DefMediaPlayer player) {
        log.fine("Player is pausing: " + player.getInstanceId());
    }  
    
    

}
