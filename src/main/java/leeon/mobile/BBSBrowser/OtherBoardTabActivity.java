package leeon.mobile.BBSBrowser;

import android.app.TabActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;



public class OtherBoardTabActivity extends TabActivity implements TabHost.TabContentFactory {
	
	FavBoardView favBoard = new FavBoardView(this);
	
	private int actionParam;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final TabHost tabHost = getTabHost();
		tabHost.setBackgroundResource(R.color.app_background);
		
		actionParam = this.getIntent().getIntExtra("action", ActionFactory.DEFAULT_PARAM);
		this.setTitle("欢迎光临" + ActionFactory.PARAMS_DESC[actionParam]);
		
		//LayoutInflater.from(this).inflate(R.layout.tabs1, tabHost.getTabContentView(), true);
		
		tabHost.addTab(tabHost.newTabSpec("fav")
				.setIndicator("收藏", this.getResources().getDrawable(R.drawable.services))
				.setContent(this)); 
		tabHost.addTab(tabHost.newTabSpec("rec")
				.setIndicator("推荐", this.getResources().getDrawable(R.drawable.ksmiletris))
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec("all")
				.setIndicator("全部", this.getResources().getDrawable(R.drawable.block))
				.setContent(this));	
		
		UIUtil.runActionInThread(OtherBoardTabActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newBoardAction(actionParam).allBlock();
			}
			@Override
			public void actionFinish() {
				tabHost.setCurrentTabByTag("fav");
			}
		});
    }
	
	@Override 
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
	}
	
	
	public View createTabContent(String tag) {
		if ("rec".equals(tag)) {
			return (new RecommendBoardView(this, actionParam)).createRecommendBoardView();
		} else if ("fav".equals(tag)) {
			return favBoard.createFavouriteBoardView(actionParam);
		} else {
			return (new BlockView(this, actionParam)).createAllBoardView();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
			menu.add(0, 1, 1, "刷新收藏").setIcon(android.R.drawable.ic_menu_rotate);
        return result;
	}
	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(1).setEnabled(false).setVisible(true);		
		if ("fav".equals(this.getTabHost().getCurrentTabTag())) {
			menu.findItem(1).setTitle("刷新收藏");
			menu.findItem(1).setEnabled(true);
		} else {
			menu.findItem(1).setVisible(false);
		}
		return result;
	} 	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				if ("fav".equals(this.getTabHost().getCurrentTabTag())) {
					favBoard.refresh();
				}
				break;
			default: break;
		}
		return super.onOptionsItemSelected(item);
	}
}
