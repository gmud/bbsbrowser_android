package leeon.mobile.BBSBrowser;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;



public class BoardTabActivity extends TabActivity implements TabHost.TabContentFactory {
	
	TopTenDocView topTen = new TopTenDocView(this);
	FavBoardView favBoard = new FavBoardView(this);
	MailView mail = new MailView(this);
	NewMailThread mailThread = new NewMailThread(this);
	
	private String guestUserId = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final TabHost tabHost = getTabHost();
		tabHost.setBackgroundResource(R.color.app_background);
		
		guestUserId = this.getIntent().getStringExtra("guestUserId");
		
		//LayoutInflater.from(this).inflate(R.layout.tabs1, tabHost.getTabContentView(), true);
		
		tabHost.addTab(tabHost.newTabSpec("top")
				.setIndicator("十大", this.getResources().getDrawable(R.drawable.launch))
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec("fav")
				.setIndicator("收藏", this.getResources().getDrawable(R.drawable.services))
				.setContent(this)); 
		tabHost.addTab(tabHost.newTabSpec("rec")
				.setIndicator("推荐", this.getResources().getDrawable(R.drawable.ksmiletris))
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec("all")
				.setIndicator("全部", this.getResources().getDrawable(R.drawable.block))
				.setContent(this));	
		tabHost.addTab(tabHost.newTabSpec("mail")
				.setIndicator("信箱", this.getResources().getDrawable(R.drawable.mail_new))
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec("soc")
				.setIndicator("社交", this.getResources().getDrawable(R.drawable.proxy))
				.setContent(this));
		
//		this.getTabWidget().getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.WRAP_CONTENT, 30, 1));
//		this.getTabWidget().getChildAt(1).setLayoutParams(new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.WRAP_CONTENT, 30, 1));
//		this.getTabWidget().getChildAt(2).setLayoutParams(new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.WRAP_CONTENT, 30, 1));
//		this.getTabWidget().getChildAt(3).setLayoutParams(new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.WRAP_CONTENT, 30, 1));
//		this.getTabWidget().getChildAt(4).setLayoutParams(new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.WRAP_CONTENT, 30, 1));
		
		
		UIUtil.runActionInThread(BoardTabActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newBoardAction().allBlock();
			}
			@Override
			public void actionFinish() {
		        if (UserUtil.CURRENT_USER_ID != null) {
		        	tabHost.setCurrentTabByTag("fav");
		        } else if (guestUserId != null && guestUserId.length() != 0) {
		        	tabHost.setCurrentTabByTag("fav");
		        }
		        	
			}
		});
		mailThread.start();
    }
	
	@Override 
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
				&& keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//为兼容2.0以下，由app调用onbackpressed
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override     
	public void onBackPressed() {
		if (UserUtil.CURRENT_USER_ID != null) {
			if (SettingActivity.logoutConfirm(this)) {
				new AlertDialog.Builder(this).setTitle("退出登录")
					.setMessage(UserUtil.CURRENT_USER_ID + ", 你确定真的要离开吗?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							logout();
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
				}).create().show();
			} else {
				logout();
			}
		} else {
			finish();
		}
	}
	
	private void logout() {
		UIUtil.runActionInThread(this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				ActionFactory.newInstance().newLogAction().logout();
				if (UserUtil.CURRENT_USER_ID != null)
					CookieCacheUtil.logoutRemoveCookie(UserUtil.CURRENT_USER_ID, BoardTabActivity.this);
			}
			@Override
			public void actionFinish() {
				out();
			}
			@Override
			public void actionError() {
				out();
			}
			
			private void out() {
				if (SettingActivity.logoutToExit(BoardTabActivity.this))
					BoardTabActivity.this.setResult(RESULT_OK);
				finish();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mailThread.stopCheck();
		
	}
	
	public View createTabContent(String tag) {
		if ("rec".equals(tag)) {
			return (new RecommendBoardView(this)).createRecommendBoardView();
		} else if ("fav".equals(tag)) {
			return favBoard.createFavouriteBoardView(guestUserId);
		} else if ("top".equals(tag)) {
			return topTen.createTopTenDocView();
		} else if ("mail".equals(tag)) {
			return mail.createMailListView();
		} else if ("soc".equals(tag)) {
			return (new SocView(this)).createSocView();
		} else {
			return (new BlockView(this)).createAllBoardView();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
			menu.add(0, 1, 1, "刷新十大").setIcon(android.R.drawable.ic_menu_rotate);
			menu.add(0, 2, 2, "新建邮件").setIcon(android.R.drawable.ic_menu_send);
			menu.add(0, 3, 3, "浏览精华").setIcon(android.R.drawable.ic_menu_view);
			menu.add(0, 4, 4, "应用设置").setIcon(android.R.drawable.ic_menu_manage);
        return result;
	}
	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(1).setEnabled(false).setVisible(true);
		menu.findItem(2).setEnabled(false).setVisible(false);
		menu.findItem(3).setEnabled(false).setVisible(false);
		
		if ("mail".equals(this.getTabHost().getCurrentTabTag()) && UserUtil.CURRENT_USER_ID != null) { 
			menu.findItem(1).setTitle("刷新邮件");
			menu.findItem(1).setEnabled(true);
			menu.findItem(2).setEnabled(true).setVisible(true);
		} else if ("fav".equals(this.getTabHost().getCurrentTabTag()) && UserUtil.CURRENT_USER_ID != null) {
			menu.findItem(1).setTitle("刷新收藏");
			menu.findItem(1).setEnabled(true);
		} else if ("top".equals(this.getTabHost().getCurrentTabTag())) {
			menu.findItem(1).setTitle("刷新十大");
			menu.findItem(1).setEnabled(true);
		} else if ("soc".equals(this.getTabHost().getCurrentTabTag())) {
			menu.findItem(1).setVisible(false);
		} else {
			menu.findItem(1).setVisible(false);
			menu.findItem(3).setEnabled(true).setVisible(true);
		}
		return result;
	} 	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				if ("mail".equals(this.getTabHost().getCurrentTabTag()) && UserUtil.CURRENT_USER_ID != null) { 
					mail.refresh();
				} else if ("fav".equals(this.getTabHost().getCurrentTabTag()) && UserUtil.CURRENT_USER_ID != null) {
					favBoard.refresh();
				} else if ("top".equals(this.getTabHost().getCurrentTabTag())) {
					topTen.refresh();
				}
				break;
			case 2: 
				mail.newMail();
				break;
			case 3:
				startActivity(new Intent(this, PathListActivity.class));
				break;
			case 4:
				startActivity(new Intent(this, SettingActivity.class));
				break;
			default: break;
		}
		return super.onOptionsItemSelected(item);
	}

	static final int REQ_MAIL_DETAIL = 0;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_MAIL_DETAIL) {
			mail.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
