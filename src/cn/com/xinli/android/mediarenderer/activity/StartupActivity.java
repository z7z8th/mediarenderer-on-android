package cn.com.xinli.android.mediarenderer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * do nothing but with launcher category
 * @author chen
 *
 */
public class StartupActivity extends Activity {

	private static final String START_BACKEND_SERVICE= "start_backend_service";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		sendBroadcast(new Intent(START_BACKEND_SERVICE));
		
		finish();
	}

}
