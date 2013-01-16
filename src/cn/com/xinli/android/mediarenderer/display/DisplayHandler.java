package cn.com.xinli.android.mediarenderer.display;

import cn.com.xinli.android.mediarenderer.upnp.DefMediaPlayer;

/**
 * Decouples backend from displaying media, e.g. windowed or fullscreen.
 *
 * @author Christian Bauer
 */
public interface DisplayHandler {
	
	public void onNoMedia(DefMediaPlayer player);
	
    public void onPlay(DefMediaPlayer player);

    public void onStop(DefMediaPlayer player);

}
