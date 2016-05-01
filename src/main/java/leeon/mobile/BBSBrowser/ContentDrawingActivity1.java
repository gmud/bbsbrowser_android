package leeon.mobile.BBSBrowser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ContentDrawingActivity1 extends Activity {
	
	private Button doneButton;
	private Button clearButton;
	
	protected GestureOverlayView gestureOverlayView;
	
	private Uri uri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_gesture);
		
		uri = (Uri)getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
		
		doneButton = (Button) this.findViewById(R.id.done);
		clearButton = (Button) this.findViewById(R.id.clear);
		gestureOverlayView = (GestureOverlayView)this.findViewById(R.id.gesturesOverlay);
		
		doneButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				done();
			}
		});
		
		clearButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				gestureOverlayView.clear(false);
			}
		});
	}
	
	@Override 
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
	}
	
	
	private void done() {
		Gesture gesture = gestureOverlayView.getGesture();

		if (uri != null && gesture != null) {
			int h = getResources().getDisplayMetrics().heightPixels;
			int w = getResources().getDisplayMetrics().widthPixels;
			Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(gesture.toBitmap(w, h, 10, Color.BLACK), 0, 0, null);
			
			try {
				bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(uri.getPath()));
			} catch (FileNotFoundException e) {
				Log.e("content draw", "file not found", e);
			}
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}
}
