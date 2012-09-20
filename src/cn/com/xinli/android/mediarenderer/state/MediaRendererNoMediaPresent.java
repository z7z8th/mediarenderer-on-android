package cn.com.xinli.android.mediarenderer.state;

import java.net.URI;

import org.fourthline.cling.support.avtransport.impl.state.AbstractState;
import org.fourthline.cling.support.avtransport.impl.state.NoMediaPresent;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.AVTransport;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;

import cn.com.xinli.android.mediarenderer.upnp.DefMediaPlayer;

public class MediaRendererNoMediaPresent extends NoMediaPresent<AVTransport> {

	public MediaRendererNoMediaPresent(AVTransport transport) {
        super(transport);
    }

    @Override
    public Class<? extends AbstractState> setTransportURI(URI uri, String metaData) {

        getTransport().setMediaInfo(
                new MediaInfo(uri.toString(), metaData)
        );

        // If you can, you should find and set the duration of the track here!
        getTransport().setPositionInfo(
                new PositionInfo(1, metaData, uri.toString())
        );

        // It's up to you what "last changes" you want to announce to event listeners
        getTransport().getLastChange().setEventedValue(
                getTransport().getInstanceId(),
                new AVTransportVariable.AVTransportURI(uri),
                new AVTransportVariable.CurrentTrackURI(uri)
        );

        return MediaRendererStopped.class;
    }
}
