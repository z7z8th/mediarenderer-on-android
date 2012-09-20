package cn.com.xinli.android.mediarenderer.upnp;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.seamless.util.MimeType;

import java.util.List;
import java.util.logging.Logger;

public class DefConnectionManagerService extends ConnectionManagerService {

    final private static Logger log = Logger.getLogger(DefConnectionManagerService.class.getName());

    public DefConnectionManagerService() {
        /*List<PluginFeature> types = Registry.getDefault().getPluginFeatureListByPlugin("typefindfunctions");
        for (PluginFeature type : types) {
            try {
                MimeType mt = MimeType.valueOf(type.getName());
                log.fine("Supported MIME type: " + mt);
                sinkProtocolInfo.add(new ProtocolInfo(mt));
            } catch (IllegalArgumentException ex) {
                log.finer("Ignoring invalid MIME type: " + type.getName());
            }
        }
        log.info("Supported MIME types: " + sinkProtocolInfo.size());*/
        	
    	try {
            sinkProtocolInfo.add(new ProtocolInfo(
                    Protocol.HTTP_GET,
                    ProtocolInfo.WILDCARD,
                    "audio/mpeg",
                    "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"
            ));
            
            sinkProtocolInfo.add(new ProtocolInfo(
                    Protocol.HTTP_GET,
                    ProtocolInfo.WILDCARD,
                    "video/mpeg",
                    "DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"
            ));
        } catch (IllegalArgumentException ex) {
            log.finer("Ignoring invalid MIME type: " + "video/mp4");
        }
    }

}
