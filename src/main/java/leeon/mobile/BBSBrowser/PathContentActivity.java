package leeon.mobile.BBSBrowser;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

public class PathContentActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String content = getIntent().getStringExtra("content");
		content = content.replaceAll(UIUtil.IMG_ANSI_PATTERN, "");
		
		
		ScrollView v = new ScrollView(this);
		v.setBackgroundColor(getResources().getColor(R.color.login_background));
		setContentView(v);
		
		TextView t = new TextView(this);
		t.setText(content);
		t.setTextColor(getResources().getColor(R.color.login_text));
		t.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingActivity.displayHd(this)?26:16);
		v.addView(t);
	}
}
