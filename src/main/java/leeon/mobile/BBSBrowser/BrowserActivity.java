package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.actions.HttpConfig;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class BrowserActivity extends Activity {

	private Button loginButton;
	private Button guestButton;
	private Button clearButton;

	private EditText userEdit;
	private EditText passwordEdit;
	private CheckBox saveUser;	
	
	private boolean first = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(SettingActivity.displayHd(this)?R.layout.main_hd:R.layout.main);
		
		saveUser = (CheckBox) this.findViewById(R.id.saveUserCheckBox);		
		
		userEdit = (EditText) this.findViewById(R.id.user);
		passwordEdit = (EditText) this.findViewById(R.id.password);

		loginButton = (Button) this.findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				goLogin();
			}
		});

		guestButton = (Button) this.findViewById(R.id.guestButton);
		guestButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				UserUtil.CURRENT_USER_ID = null;
				Intent intent = new Intent(BrowserActivity.this, BoardTabActivity.class);
				BrowserActivity.this.startActivity(intent);
			}
		});
		
		clearButton = (Button) this.findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				userEdit.setText("");
				passwordEdit.setText("");
//				Intent intent = new Intent(BrowserActivity.this, KaixinMainActivity.class);
//				BrowserActivity.this.startActivity(intent);
			}
		});
		HttpConfig.SH_NO_EDU = SettingActivity.fromSh(this);
		AppUpdateUtil.checkVersion(this);
		clearCache();
	}	
	
	@Override 
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
	}
		
	
	@Override
	protected void onResume() {
		super.onResume();
		UserUtil.saveHtmlMode(this);
		UserUtil.saveTitleMode(this);
		
		UserUtil.CURRENT_USER_ID = null;
		UserUtil.isHtmlMode(this);
		UserUtil.isTitleMode(this);
		
		initUserEdit(UserUtil.getLastLoginId(this));
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		HTTPUtil.shutdownAll();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, SELECT_USER, SELECT_USER, "切换账号").setIcon(android.R.drawable.ic_menu_myplaces);
		menu.add(0, UPDATE_APP, UPDATE_APP, "检查更新").setIcon(android.R.drawable.ic_menu_set_as);
		menu.add(0, ABOUT_APP, ABOUT_APP, "关于").setIcon(android.R.drawable.ic_menu_info_details);
        return result;
	}
	
	@Override  
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(UPDATE_APP).setEnabled(!AppUpdateUtil.isLastVersion(this));
		menu.findItem(UPDATE_APP).setTitle("检查更新" + (AppUpdateUtil.isLastVersion(this)?"(无新版本)":"(有新版本)"));
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		showDialog(item.getItemId());
		return super.onOptionsItemSelected(item);
	}
	
	private static final int SELECT_USER = 0;
	private static final int UPDATE_APP = 1;
	private static final int ABOUT_APP = 2;
	
	@Override
    protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SELECT_USER:
			final String[] l = UserUtil.getUserList(this);
			return new AlertDialog.Builder(this)
				.setTitle("选择账号")
				.setItems(l, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						initUserEdit(l[which]);
					}
			}).create();
		case UPDATE_APP:
			if (!AppUpdateUtil.isLastVersion(this)) {
				return new AlertDialog.Builder(this).setTitle("系统更新")
					.setMessage("发现新版本，请更新" + AppUpdateUtil.LAST_APP_MSG)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {					
						public void onClick(DialogInterface dialog, int which) {
							AppUpdateUtil.newInstance(BrowserActivity.this).downloadFile(BrowserActivity.this);
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
				}).create();
			} else {
				return new AlertDialog.Builder(this).setTitle("系统更新")
					.setMessage("当前已经是最新版本")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
				}).create();
			}
		case ABOUT_APP:
			return new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon)
				.setTitle("关于本软件\n(Version:"+AppUpdateUtil.CURRENT_APP_VERSION+")")
				.setMessage(R.string.app_about)
				.setPositiveButton("下载APK", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppUpdateUtil.APP_UPDATE_APP_FILE_URL)));
					}
				})
				.setNeutralButton("使用帮助", new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppUpdateUtil.APP_HELP_URL)));
					}
				})
				.setNegativeButton("啥也不做", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.create();
		default:
			return null;
		}
	}
	
	private static final int LOGIN_CODE = 1;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				finish();
			}
		}
	}
	
	private void goLogin() {
		if (!HttpConfig.useServer()) {
			login();
		} else {
			//走guest的入口，但传过去user id，提取本地的收藏
			UserUtil.CURRENT_USER_ID = null;
			Intent intent = new Intent(BrowserActivity.this, BoardTabActivity.class);
			intent.putExtra("guestUserId", userEdit.getText().toString());
			BrowserActivity.this.startActivity(intent);
		}
	}
	
	private void login() {
		UIUtil.runActionInThread(BrowserActivity.this, new UIUtil.ActionInThread<Object>() {
			@Override
			public void action() throws NetworkException, ContentException {
				String id = userEdit.getText().toString();
				//查找是否有曾经未正常退出的cookie缓存,有就先踢除
				String cookieString = CookieCacheUtil.hasNoRemoveCookie(id, BrowserActivity.this);
				if (cookieString != null) {
					ActionFactory.newInstance().newLogAction().logout(cookieString);
					CookieCacheUtil.logoutRemoveCookie(id, BrowserActivity.this);
				}
				
				//读取cookie,并记录cookie缓存
				List<Cookie> cookie = new ArrayList<Cookie>();
				ActionFactory.newInstance().newLogAction().login(id, passwordEdit.getText().toString(), cookie);
				if (!cookie.isEmpty()) {
					cookieString = HTTPUtil.cookieListToString(cookie);
					CookieCacheUtil.loginWriteCookie(id, cookieString, BrowserActivity.this);
					
					//读取cookie中的id，为了区分大小写
					String u = CookieCacheUtil.getCookieUserId(cookie);
					if (u != null) id = u;
				}
				UserUtil.CURRENT_USER_ID = id;
			}
			@Override
			public void actionFinish() {
				UserUtil.saveLoginId(BrowserActivity.this);
				UserUtil.isHtmlMode(BrowserActivity.this);
				UserUtil.isTitleMode(BrowserActivity.this);
				if (saveUser.isChecked()) UserUtil.saveLoginPassword(BrowserActivity.this, passwordEdit.getText().toString());
				else UserUtil.saveLoginPassword(BrowserActivity.this, null);
				
				Intent intent = new Intent(BrowserActivity.this, BoardTabActivity.class);
				BrowserActivity.this.startActivityForResult(intent, LOGIN_CODE);
			}
		});
	}
	
	private void initUserEdit(String userId) {
		String password = UserUtil.getLoginPassword(this, userId);
		userEdit.setText(userId);
		passwordEdit.setText(password);
		if (SettingActivity.autoLogin(this) && first) {
			if (userId != null && userId.length() != 0 && password != null && password.length() != 0) {
				goLogin();
			}
			first = false;
		}
	}
	
	private void clearCache() {
		final int d = SettingActivity.cacheTime(this);
		new Thread() {
			public void run() {
				UIUtil.clearCache(BrowserActivity.this, d);
			}
		}.start();
	}
}