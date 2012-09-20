package cn.com.xinli.android.mediarenderer.service;

import cn.com.xinli.android.mediarenderer.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MediaRendererBCR extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LightedGreenRoom.setup(context);
    	startService(context,intent);
	}
	
	private void startService(Context context, Intent intent)
    {
    	Intent serviceIntent = new Intent(context,MediaRendererService.class);
    	serviceIntent.putExtra("original_intent", intent);
    	context.startService(serviceIntent);

        // Tell the user about what we did.
        Toast.makeText(context, R.string.service_start,
                Toast.LENGTH_LONG).show();
        
    }

}
