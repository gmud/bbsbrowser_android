package leeon.mobile.BBSBrowser;

import java.io.InputStream;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GIFView extends View implements Runnable {
	
	private boolean isStop = true;
	private int delta;

	private GIFOpenHelper gHelper;
	private Bitmap bmp;

	// construct - refer for java
	public GIFView(Context context) {
		this(context, null);
	}

	// construct - refer for xml
	public GIFView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//添加属性
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GifView);
		int n = ta.getIndexCount();

		for (int i = 0; i < n; i++) {
			int attr = ta.getIndex(i);

			switch (attr) {
			case R.styleable.GifView_src:
				int id = ta.getResourceId(R.styleable.GifView_src, 0);
				setSrc(id);
				break;

			case R.styleable.GifView_delay:
				int idelta = ta.getInteger(R.styleable.GifView_delay, 1);
				setDelta(idelta);
				break;

			case R.styleable.GifView_stop:
				boolean sp = ta.getBoolean(R.styleable.GifView_stop, false);
				if (!sp) {
					setStop();
				}
				break;
			}
		}
		ta.recycle();
	}

	/**
	 * 停止播放 
	 * @param stop
	 */
	public void setStop() {
		isStop = false;
	}

	/**
	 * 开始播放
	 */
	public void setStart() {
		isStop = true;
		Thread updateTimer = new Thread(this);
		updateTimer.start();
	}

	/**
	 * 设置源
	 * @param id
	 */
	public void setSrc(int id) {
		setSrc(this.getResources().openRawResource(id));
	}
	
	public void setSrc(InputStream is) {
		gHelper = new GIFOpenHelper();
		gHelper.read(is);
		bmp = gHelper.getImage();//得到第一张图片
	}

	public void setDelta(int is) {
		delta = is;
	}

	// to meaure its Width & Height
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) {
		return gHelper.getWidth();
	}

	private int measureHeight(int measureSpec) {
		return gHelper.getHeigh();
	}

	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bmp, 0, 0, new Paint());
		bmp = gHelper.nextBitmap();

	}

	public void run() {
		while (isStop) {
			try {
				this.postInvalidate();
				Thread.sleep(gHelper.nextDelay() / delta);
			} catch (Exception ex) {

			}
		}
	}

}
