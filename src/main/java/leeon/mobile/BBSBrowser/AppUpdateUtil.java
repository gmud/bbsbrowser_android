package leeon.mobile.BBSBrowser;

import java.io.File;

import leeon.mobile.BBSBrowser.utils.HTTPUtil;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class AppUpdateUtil {

	//自动更新下载的app name
	public static final String APP_NAME = "bbsbrowser.apk";
	
	//自动更新下载的app version 文件
	public static final String APP_VERSION_FILE = "version.txt";
	
	//SVN路径
	public static final String APP_SVN_URL = "https://raw.githubusercontent.com/gmud/bbsbrowser_android/master/build/outputs/";

	//自动更新的url路径
	public static final String APP_UPDATE_URL = APP_SVN_URL + "apk/";
	
	//帮助路劲
	public static final String APP_HELP_URL = APP_UPDATE_URL + "help/%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.mht";
	
	//自动更新的url路径
	public static final String APP_UPDATE_APP_FILE_URL = APP_UPDATE_URL + APP_NAME;
	
	//自动更新的url路径
	public static final String APP_UPDATE_VERSION_FILE_URL = APP_UPDATE_URL + APP_VERSION_FILE;
		
	//自动更新本地保存目录
	public static String APP_LOCAL_URL =  Environment.getExternalStorageDirectory() + "/Download" + APP_NAME;

	//最新的app version 及 发布信息
	//自动更新下载的app version 用于比较
	public static String CURRENT_APP_VERSION = "";
	public static String LAST_APP_VERSION = CURRENT_APP_VERSION;	
	public static String LAST_APP_MSG = null;

	
	private boolean exception = false;	
	private Handler handler = new Handler();
	
	//创建一个实例
	public static AppUpdateUtil newInstance(Context context) {
		//APP_LOCAL_URL = context.getApplicationInfo().dataDir + APP_LOCAL_URL; 
		return new AppUpdateUtil();
	}
	
	//是否是最新版本检查
	public static boolean isLastVersion(Context context) {
		initVersion(context);
		return AppUpdateUtil.CURRENT_APP_VERSION.equals(AppUpdateUtil.LAST_APP_VERSION);
	}
	
	//检查服务器上的版本
	//版本说明书txt必须符合规范
	public static void checkVersion(Context context) {
		initVersion(context);
		new Thread() {
			public void run() {
				try {
					//删除升级完不用的文件
					File f = new File(APP_LOCAL_URL);
					if (f.exists()) f.delete();
					
					String msg = HTTPUtil.viewTextFile(APP_UPDATE_VERSION_FILE_URL);
					if (msg != null) {						
						LAST_APP_VERSION = msg.substring(8, 13);
						LAST_APP_MSG = msg.substring(msg.indexOf('{')+1, msg.indexOf('}'));
					}
				} catch (NetworkException e) {
					Log.e("updateApp", "check app version error", e);
				}
			}
		}.start();
	}
	
	//init version from manifest
	private static void initVersion(Context context) {
		if (!"".equals(AppUpdateUtil.CURRENT_APP_VERSION)) return; 
		try {
			AppUpdateUtil.CURRENT_APP_VERSION = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			AppUpdateUtil.CURRENT_APP_VERSION = "";
		} finally {
			AppUpdateUtil.LAST_APP_VERSION = AppUpdateUtil.CURRENT_APP_VERSION;
		}
	}
	
	//下载罪行的apk包
	public void downloadFile(final Context context) {
		final ProgressDialog pBar = new ProgressDialog(context);					
		pBar.show();
		
		new Thread() {
			public void run() {
				exception = false;
				try {
					HTTPUtil.downloadFile(APP_UPDATE_APP_FILE_URL, new File(APP_LOCAL_URL));					
				} catch (NetworkException e) {
					Log.e("updateApp", "update app error", e);
					exception = true;
				}
				//下载完成后启动安装
				handler.post(new Runnable() {
					public void run() {
						pBar.cancel();
						pBar.dismiss();
						
						if (exception)
							UIUtil.showErrorToast(context);
						else {							
							install(context);
						}
					}
				});
			}
		}.start();

	}
	
//	private static final int REQUEST_CODE_INSTALL_APP = 0;
//		
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		switch (requestCode) {
//			case REQUEST_CODE_INSTALL_APP:
//				File f = new File(UIUtil.APP_LOCAL_URL);
//				if (f.exists()) f.delete();
//				break;
//			default:break;
//		}		
//	}
	
	//安装apk的方法
	private void install(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(APP_LOCAL_URL)), "application/vnd.android.package-archive");
		//this.startActivityForResult(intent, REQUEST_CODE_INSTALL_APP);
		context.startActivity(intent);
	}
}
