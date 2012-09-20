package cn.com.xinli.android.mediarenderer.state;

import java.net.URI;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.Stopped;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.SeekMode;

public class MediaRendererStopped extends Stopped<AVTransport> {

	public MediaRendererStopped(AVTransport transport) {
		super(transport);
	}
	
	public void onEntry() {
        super.onEntry();
        // Optional: Stop playing, release resources, etc.
    }

    public void onExit() {
        // Optional: Cleanup etc.
    }
    
	
	@Override
	public Class<? extends AbstractState> next() {
		return MediaRendererStopped.class;
	}

	@Override
	public Class<? extends AbstractState> play(String speed) {
		// It's easier to let this classes' onEntry() method do the work
        return MediaRendererPlaying.class;
	}

	@Override
	public Class<? extends AbstractState> previous() {
		return MediaRendererStopped.class;
	}

	@Override
	public Class<? extends AbstractState> seek(SeekMode unit, String target) {
		// Implement seeking with the stream in stopped state!
        return MediaRendererStopped.class;
	}

	@Override
	public Class<? extends AbstractState> setTransportURI(URI arg0, String arg1) {
		// This operation can be triggered in any state, you should think
        // about how you'd want your player to react. If we are in Stopped
        // state nothing much will happen, except that you have to set
        // the media and position info, just like in MyRendererNoMediaPresent.
        // However, if this would be the MyRendererPlaying state, would you
        // prefer stopping first?
        return MediaRendererStopped.class;
	}

	@Override
	public Class<? extends AbstractState> stop() {
		/// Same here, if you are stopped already and someone calls STOP, well...
        return MediaRendererStopped.class;
	}

}
