package leeon.mobile.BBSBrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FingerPaintView extends View {

	Bitmap bitmap;
	Canvas canvas;
	Path path;
	Paint bitmapPaint;
	Paint paint;
	
	public void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setColor(Color.BLACK);
		
		path = new Path();
        bitmapPaint = new Paint(Paint.DITHER_FLAG);
	}

	public FingerPaintView(Context context) {
		super(context);
		init();
	}
	
	public FingerPaintView(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}
	
    @Override
	protected void onDraw(Canvas canvas) {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			this.canvas = new Canvas(bitmap);
			this.canvas.drawColor(Color.WHITE);
		}
		
    	canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
		canvas.drawPath(path, paint);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}
		return true;
	}
    
    public void clear() {
    	path.reset();
    	bitmap = null;
    	invalidate();
    }
    
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;
	private void touch_start(float x, float y) {
		path.reset();
		path.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		path.lineTo(mX, mY);
		// commit the path to our offscreen
		canvas.drawPath(path, paint);
		// kill this so we don't double draw
		path.reset();
	}
}
