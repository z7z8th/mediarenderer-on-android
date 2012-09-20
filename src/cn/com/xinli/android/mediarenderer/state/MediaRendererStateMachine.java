package cn.com.xinli.android.mediarenderer.state;

import org.fourthline.cling.support.avtransport.impl.AVTransportStateMachine;
import org.seamless.statemachine.*;

@States({
        MediaRendererNoMediaPresent.class,
        MediaRendererStopped.class,
        MediaRendererPlaying.class
})
public interface MediaRendererStateMachine extends AVTransportStateMachine {}
