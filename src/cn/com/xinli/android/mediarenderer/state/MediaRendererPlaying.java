package cn.com.xinli.android.mediarenderer.state;

import java.net.URI;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Playing;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

public class MediaRendererPlaying extends Playing<AVTransport> {

	public MediaRendererPlaying(AVTransport transport) {
		super(transport);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public void onEntry() {
        super.onEntry();
        // Start playing now!
    }

	@Override
	public Class<? extends AbstractState> next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends AbstractState> pause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends AbstractState> play(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends AbstractState> previous() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends AbstractState> seek(SeekMode arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends AbstractState> setTransportURI(URI arg0, String arg1) {
		// Your choice of action here, and what the next state is going to be!
        return MediaRendererStopped.class;
	}

	@Override
	public Class<? extends AbstractState> stop() {
		// Stop playing!
        return MediaRendererStopped.class;
	}

}
