package leeon.mobile.BBSBrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

public class ImageDetailView extends FrameLayout {
	
	
	public ImageDetailView(Context context) {
		this(context, null);
	}

	public ImageDetailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	//��ƽ����ز���
	private float mLastMotionX;
	private float mLastMotionY;
	private float mLastSpacing;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int tag = 0;
	
	private ImageView imageView;
	private Bitmap bmp;
	
	//��������ز���
	private PointF zoomPoint = new PointF();
	private int oImageWidth = 0;
	private int oImageHeight = 0;
	private float oScale = 1f;
	private float minScale = 1f;
	private float maxScale = 4f;
	private float lScale = 1f;
	private float lastDeltaX = 0f;
	private float lastDeltaY = 0f;
	
	
	private void init() {
		ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();		
		mScroller = new Scroller(getContext());
		
		imageView = new ImageView(getContext());
		imageView.setScaleType(ImageView.ScaleType.MATRIX);
		addView(imageView);
		
		
		
	}	

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		this.initZoom();
	}
	
	public void setImage(Bitmap bitmap) {
		bmp = bitmap;
		oImageWidth = bmp.getWidth();
		oImageHeight = bmp.getHeight();
		imageView.setImageBitmap(bmp);
		imageView.setLayoutParams(new FrameLayout.LayoutParams(oImageWidth, oImageHeight));
	}
		
	public void initZoom() {
		float oWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float oHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		
		float iWidth = oImageWidth;
		float iHeight = oImageHeight;
		
		float scaleW = oWidth/iWidth;
		float scaleH = oHeight/iHeight;
		
		if (scaleW >= 1 && scaleH >= 1) {
			oScale = 1f;
		} else {
			oScale = Math.min(scaleW, scaleH);
			scale();
		}
		minScale = oScale;
		center((int)(iWidth*oScale), (int)(iHeight*oScale));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();

		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				
				mLastMotionX = event.getX();
				mLastMotionY = event.getY();			
				if (mVelocityTracker == null) {
					mVelocityTracker = VelocityTracker.obtain();
				}
				tag = 1;
				break;
			}
			case MotionEvent.ACTION_UP: {
				mLastMotionX = event.getX();
				mLastMotionY = event.getY();
				if (tag == 1) {
					final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
					int initialVelocitx = (int) velocityTracker.getXVelocity();
					int initialVelocity = (int) velocityTracker.getYVelocity();
	
					if (Math.abs(initialVelocitx) > mMinimumVelocity
							|| Math.abs(initialVelocity) > mMinimumVelocity) {
						fling(-initialVelocitx, -initialVelocity);
					} else {
						center();
					}
	                
					if (mVelocityTracker != null) {
						mVelocityTracker.recycle();
						mVelocityTracker = null;
	                }
				}			
				tag = 0;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (tag == 1) {
					float x = event.getX();
					int deltaX = (int) (mLastMotionX - x);
					deltaX = clampWidth(getScrollX()+deltaX, imageView.getWidth())-getScrollX();
					mLastMotionX -= deltaX;
	
					float y = event.getY();
					int deltaY = (int) (mLastMotionY - y);
					deltaY = clampHeight(getScrollY()+deltaY, imageView.getHeight())-getScrollY();
					mLastMotionY -= deltaY;
	
					scrollBy(deltaX, deltaY);
					
					if (mVelocityTracker != null)
						mVelocityTracker.addMovement(event);
				} else if (tag == 2) {
					float scale = spacing(0, 1, event);
					scale = (2000+scale)/(2000+mLastSpacing);
					oScale = oScale*scale;
					zoomTo();
				}
	
				break;
			}
	
			case MotionEvent.ACTION_CANCEL: {
				if (tag == 1) {
					if (mVelocityTracker != null) {
						mVelocityTracker.recycle();
						mVelocityTracker = null;
		            }
				}
				tag = 0;
				break;
			}
			case MotionEvent.ACTION_POINTER_DOWN: {
				if (tag == 1) {
					mLastSpacing = spacing(0, 1, event);
					zoomPoint();
				} else if (tag == 2) {
					center();
				}
				tag++;
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: {
				int pointerId = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				int pointerIndex = event.findPointerIndex(pointerId);
				int newPointerIndex = (pointerIndex == 0 ? 1 : 0);
				mLastMotionX = event.getX(newPointerIndex);
				mLastMotionY = event.getY(newPointerIndex);
				
				if (mVelocityTracker != null)
					mVelocityTracker.clear();
				
				if (tag == 3) {
					mLastSpacing = spacing(newPointerIndex, (pointerIndex == 1 ? 2 : newPointerIndex + 1), event);
					zoomPoint();
				} else if (tag == 2) {
					center();
				}
				tag--;
				// System.out.println("action_pointer_up;"+pointerId+";"+pointerIndex);
				break;
			}
		}
		return true;
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where mScrollX/Y is different from what the app
            //         thinks it is.
            //
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			
			x = clampWidth(x, imageView.getWidth());
			y = clampHeight(y, imageView.getHeight());
			scrollTo(x, y);
            
			//awakenScrollBars();
            // Keep on drawing until the animation has finished.
            postInvalidate();
        }
	}
	

	private void fling(int velocityX, int velocityY) {
		int oWidth = getWidth() - getPaddingRight() - getPaddingLeft();
		int iWidth = imageView.getWidth();
		
		int oHeight = getHeight() - getPaddingBottom() - getPaddingTop();
		int iHeight = imageView.getHeight();
		
		mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 
			iWidth<oWidth?((iWidth-oWidth)/2):0, iWidth<oWidth?((iWidth-oWidth)/2):(iWidth-oWidth), 
			iHeight<oHeight?((iHeight-oHeight)/2):0, iHeight<oHeight?((iHeight-oHeight)/2):(iHeight-oHeight));
		invalidate();
	}
	
	private float spacing(int index0, int index1, MotionEvent event) { 
		float x = event.getX(index0) - event.getX(index1); 
		float y = event.getY(index0) - event.getY(index1); 
		return FloatMath.sqrt(x * x + y * y); 
	}
	
	private void center() {
		center(imageView.getWidth(), imageView.getHeight());
	}
	
	private void center(final int iWidth, final int iHeight) {
		int sx = clampWidth(getScrollX(), iWidth);
		int sy = clampHeight(getScrollY(), iHeight);
		scrollTo(sx, sy);
	}
	
	private void zoomPoint() { 
		float x = getWidth()/2 + getScrollX();
		float y = getHeight()/2 + getScrollY(); 
		zoomPoint.set(x, y);
		
		lScale = oScale;
		lastDeltaX = 0f;
		lastDeltaY = 0f;
	} 
	
	private void zoomScoll() {
		float x = zoomPoint.x*oScale/lScale;
		float y = zoomPoint.y*oScale/lScale;
		
		float deltaX = x-zoomPoint.x;
		float deltaY = y-zoomPoint.y;
		scrollBy((int)(deltaX-lastDeltaX), (int)(deltaY-lastDeltaY));
		
		lastDeltaX = deltaX;
		lastDeltaY = deltaY;
	}
	
	private void zoomTo() {
		if (oScale > maxScale) {
			oScale = maxScale;
            return;
        } else if (oScale < minScale) {
        	oScale = minScale;
        	return;
        }
		scale();
		zoomScoll();
    }
	
	private void scale() {
		Matrix m = new Matrix();
		m.postScale(oScale, oScale);
		imageView.setImageMatrix(m);
		imageView.setLayoutParams(new FrameLayout.LayoutParams((int)(oImageWidth*oScale), (int)(oImageHeight*oScale)));		
	}
	
	private int clampWidth(int n, int child) {
		int oWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		return clamp(n, oWidth, child);
	}
	
	private int clampHeight(int n, int child) {
		int oHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		return clamp(n, oHeight, child);
	}
	
    private int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- mScrollX --|
             */
        	if (my >= child)
        		return (child-my)/2;
        	else
        		return 0;
        }
        if ((my+n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- mScrollX --|
             */
            return child-my;
        }
        return n;
    }


}
