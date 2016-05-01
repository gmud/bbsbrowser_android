package leeon.mobile.BBSBrowser;

import java.util.ArrayList;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Color;
import android.view.MotionEvent;
import android.widget.ListView;

public class BaseGestureView {
	
	private Context context;
	
	protected GestureOverlayView gestureOverlayView;
	protected int gestureStartPosition = -1;
    
	public BaseGestureView(Context context) {
		this.context = context;
	}
	
	private void createBaseGestureOverlayView() {
		gestureOverlayView = new GestureOverlayView(this.context);
		gestureOverlayView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE);
		gestureOverlayView.setEventsInterceptionEnabled(true);
		gestureOverlayView.setOrientation(GestureOverlayView.ORIENTATION_VERTICAL);
		gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
		
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
	}
	
	protected void addListView(final ListView view) {
		createBaseGestureOverlayView();
		gestureOverlayView.addOnGestureListener(new OnGestureListener() {

			public void onGesture(GestureOverlayView gview, MotionEvent event) {}
			public void onGestureCancelled(GestureOverlayView gview, MotionEvent event) {}
			public void onGestureEnded(GestureOverlayView gview, MotionEvent event) {}

			public void onGestureStarted(GestureOverlayView gview, MotionEvent event) {
				gestureStartPosition = view.pointToPosition((int)event.getX(), (int)event.getY());
			}
			
		});
		gestureOverlayView.addView(view);
		if (!SettingActivity.enableGesture(context)) gestureOverlayView.setEnabled(false);
	}
	
	protected void onGestureActionRecognized(String actionName, int position) {		
	}
	
	protected void onGestureActionRecognized(ArrayList<Prediction> pList, int postion) {
	}
	
}
