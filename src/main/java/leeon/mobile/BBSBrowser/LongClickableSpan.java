package leeon.mobile.BBSBrowser;

import android.text.style.ClickableSpan;
import android.view.View;

public abstract class LongClickableSpan extends ClickableSpan {
	
	abstract public void onLongClick(View view);

}
