package leeon.mobile.BBSBrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class GIFViewActivity extends Activity {
	
	private File file;
	private GIFView view;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gif_view);
		view = (GIFView) findViewById(R.id.gifView1);	

//		Button btn = (Button) findViewById(R.id.Button01);
//		Button btn2 = (Button) findViewById(R.id.Button02);
//		btn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				view.setStop();
//			}
//		});
//		btn2.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				view.setStart();
//			}
//		});
		
		file = (File)getIntent().getSerializableExtra("file");
		if (file != null)
			try {
				view.setSrc(new FileInputStream(file));
				view.setStart();
			} catch (FileNotFoundException e) {
				Log.i("gif", "not found gif", e);
				finish();
			}
		else
			finish();
	}
	
	
	
	@Override
	protected void onDestroy() {
		if (view != null)
			view.setStop();
		super.onDestroy();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "另存图片").setIcon(android.R.drawable.ic_menu_save);
        return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (file != null) UIUtil.chooseAndCopyFile(this, file, file.getName()+".gif");
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
