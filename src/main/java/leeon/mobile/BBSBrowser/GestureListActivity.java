package leeon.mobile.BBSBrowser;

import java.util.ArrayList;

import android.app.ListActivity;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.view.MotionEvent;

public class GestureListActivity extends ListActivity {
	
	protected GestureOverlayView gestureOverlayView;
	protected int gestureStartPosition = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.gesture_list);	
		
		gestureOverlayView = (GestureOverlayView)this.findViewById(R.id.gestureOverlayView);
		gestureOverlayView.addOnGesturePerformedListener(new OnGesturePerformedListener() {

			public void onGesturePerformed(GestureOverlayView v, Gesture g) {
				ArrayList<Prediction> l = UserUtil.glib.recognize(g);
				if (l.size() > 0 && l.get(0).score > 1.0) {
					String action = l.get(0).name;
					onGestureActionRecognized(action, gestureStartPosition);
				}
				onGestureActionRecognized(l, gestureStartPosition);
			}
			
		});
		
		gestureOverlayView.addOnGestureListener(new OnGestureListener() {

			public void onGesture(GestureOverlayView gview, MotionEvent event) {}
			public void onGestureCancelled(GestureOverlayView gview, MotionEvent event) {}
			public void onGestureEnded(GestureOverlayView gview, MotionEvent event) {}

			public void onGestureStarted(GestureOverlayView gview, MotionEvent event) {
				gestureStartPosition = 
					GestureListActivity.this
					.getListView()
					.pointToPosition((int)event.getX(), (int)event.getY());
			}
			
		});
		if (!SettingActivity.enableGesture(this)) gestureOverlayView.setEnabled(false);
	}
	
	protected void onGestureActionRecognized(String actionName, int position) {		
	}
	
	protected void onGestureActionRecognized(ArrayList<Prediction> pList, int postion) {
	}
}
