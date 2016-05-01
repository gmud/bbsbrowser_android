package leeon.mobile.BBSBrowser;

import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;

public class BaseGesture implements OnGestureListener {
	
	private static final int FLING_MIN_DISTANCE = 100;
    private static final int FLING_MIN_VELOCITY = 200;
    
	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			onRightToLeftFling(e1, e2);
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			onLeftToRightFling(e1, e2);
		}
		return false;
	}

	
	public void onLongPress(MotionEvent e) {
	}

	
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	
	public void onShowPress(MotionEvent e) {
	}

	
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	protected void onRightToLeftFling(MotionEvent e1, MotionEvent e2) {}
	
	protected void onLeftToRightFling(MotionEvent e1, MotionEvent e2) {}
}
