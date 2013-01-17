package cn.com.xinli.android.mediarenderer.activity;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import cn.com.xinli.android.mediarenderer.R;
import cn.com.xinli.android.mediarenderer.UpnpSingleton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PlayerActivity extends FragmentActivity {
	
	private static final String TAG = "PlayerActivity";
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	private static final int RED = 0xffFF8080;
    private static final int BLUE = 0xff8080FF;
    private static final int CYAN = 0xff80ffff;
    private static final int GREEN = 0xff80ff80;
    private ValueAnimator colorAnim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_player);
		
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.MediaRendererRoot, UpnpSingleton.getInstance().getMediaPlayers().get(new UnsignedIntegerFourBytes(0)))
			.commit();
		
		final View rootView = findViewById(R.id.root_container);

		// Animate background color
        colorAnim = ObjectAnimator.ofInt(rootView, "backgroundColor", RED, BLUE);
        colorAnim.setDuration(3000);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(ValueAnimator.INFINITE);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        
	}

	@Override
	public void onBackPressed() {
		
		/*// clear videoView and imageView firstly
		UpnpSingleton.getInstance().getMediaPlayers().get(new UnsignedIntegerFourBytes(0)).stop();
		
		// recycle bitmap
		ImageView imageView = (ImageView) findViewById(R.id.myimageview);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
		if (bitmapDrawable != null) {
			Bitmap bitmap = bitmapDrawable.getBitmap();
			if (bitmap != null && !bitmap.isRecycled())	bitmap.recycle();
		}
				
		moveTaskToBack(true);*/
		
		finish();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		colorAnim.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		colorAnim.cancel();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy");
		super.onDestroy();
//		getApplicationContext().unbindService(UpnpSingleton.getInstance().getServiceConnection());
		
	}

}
