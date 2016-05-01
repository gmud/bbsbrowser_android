package leeon.mobile.BBSBrowser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ContentDrawingActivity extends Activity {
	
	private Button doneButton;
	private Button clearButton;
	
	private Button strokeColorButton;
	private Spinner strokeWidthSpinner;
	
	protected FingerPaintView fingerView;
	
	private Uri uri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_finger);
		setTitle("手指涂鸦板");
		
		uri = (Uri)getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);
		
		doneButton = (Button) this.findViewById(R.id.done);
		clearButton = (Button) this.findViewById(R.id.clear);
		strokeColorButton = (Button) this.findViewById(R.id.strokeColorButton);
		strokeWidthSpinner = (Spinner)  this.findViewById(R.id.strokeWidthSpinner);
		fingerView = (FingerPaintView)this.findViewById(R.id.fingerView);
		
		doneButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				done();
			}
		});
		
		clearButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				fingerView.clear();
			}
		});
		
		strokeColorButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new ColorPickerDialog(ContentDrawingActivity.this, 
					new ColorPickerDialog.OnColorChangedListener() {
						public void colorChanged(int color) {
							fingerView.paint.setColor(color);
							strokeColorButton.setBackgroundColor(color);
						}
					}, fingerView.paint.getColor()).show();
			}
		});
		strokeColorButton.setBackgroundColor(Color.BLACK);
		
		final StrokeWidthAdapter adapter = new StrokeWidthAdapter();		
		strokeWidthSpinner.setAdapter(adapter);
		strokeWidthSpinner.setBackgroundColor(Color.TRANSPARENT);
		strokeWidthSpinner.setPrompt("选择画笔宽度");
		strokeWidthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> view, View v, int position, long id) {
				fingerView.paint.setStrokeWidth(adapter.getItem(position));
			}

			public void onNothingSelected(AdapterView<?> view) {
			}
		});
		strokeWidthSpinner.setSelection(2);
	}
	
	private void done() {
		if (uri != null) {
			try {
				fingerView.bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(uri.getPath()));
			} catch (FileNotFoundException e) {
				Log.e("content draw", "file not found", e);
			}
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}
	
	private class StrokeWidthAdapter extends BaseAdapter {

		private float[] w = new float[] {1, 5, 10, 15, 20, 25, 30};
		Drawable[] d = new Drawable[w.length];
		private Paint p = new Paint();

		public StrokeWidthAdapter() {
			p.setAntiAlias(true);
			p.setDither(true);
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeJoin(Paint.Join.ROUND);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setColor(Color.BLACK);
			
			for (int i = 0; i < w.length; i ++) {
				Bitmap b = Bitmap.createBitmap(100, 40, Bitmap.Config.ARGB_8888);
				d[i] = new BitmapDrawable(b);
				
				Canvas c = new Canvas(b);
				p.setStrokeWidth(w[i]);
				c.drawLine(0, 10, 100, 10, p);
			}
		}

		public int getCount() {
			return w.length;
		}

		public Float getItem(int position) {
			return new Float(w[position]);
		}
		
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv = LayoutInflater.from(ContentDrawingActivity.this).inflate(R.layout.board_list, parent, false);
			} else {
				tv = convertView;
			}
			final TextView l = (TextView)tv.findViewById(R.id.boardListItem);
			l.setBackgroundDrawable(d[position]);
			l.setText("");
			return tv;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View tv;
			if (convertView == null) {
				tv = LayoutInflater.from(ContentDrawingActivity.this).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			} else {
				tv = convertView;
			}
			TextView l = (TextView)tv.findViewById(android.R.id.text1);
			l.setCompoundDrawablesWithIntrinsicBounds(d[position], null, null, null);
			l.setText(w[position] + "像素");
			return tv;
		}
		
		
	}
}
