package cn.com.xinli.android.mediarenderer.upnp;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.lastchange.LastChange;

import cn.com.xinli.android.mediarenderer.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class DefMediaPlayers extends ConcurrentHashMap<UnsignedIntegerFourBytes, DefMediaPlayer> {

    final private static Logger log = Logger.getLogger(DefMediaPlayers.class.getName());
    final private static String TAG = "DefMediaPlayers";

    final protected LastChange avTransportLastChange;
    final protected LastChange renderingControlLastChange;

    public DefMediaPlayers(Context context,
    						int numberOfPlayers,
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
                            } 
                        }
                        
                    };
                    
            player.init(new UnsignedIntegerFourBytes(i),
            		avTransportLastChange,
                    renderingControlLastChange);
            
            put(player.getInstanceId(), player);
            
            ((DefMediaRenderer)context).getSupportFragmentManager().beginTransaction().add(R.id.MediaRendererRoot, player, "videoview").commit();
        }
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
