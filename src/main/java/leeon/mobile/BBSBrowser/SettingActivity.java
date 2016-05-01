package leeon.mobile.BBSBrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingActivity extends PreferenceActivity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
	
	public static boolean autoLogin(Context context) {	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("log_auto_login", false);
	}
	
	public static boolean logoutToExit(Context context) {	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("log_logout_to_exit", false);
	}
	
	public static boolean logoutConfirm(Context context) {	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("log_logout_confirm", true);
	}
	
	public static boolean checkMail(Context context) {	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("mail_check_new", true);
	}
	
	public static int checkMailInterval(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString("mail_check_interval", "5"))*60*1000;
	}
	
	public static String getShortWords(Context context) {	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString("post_short_words", null);
	}
	
	public static boolean renderColorContent(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("content_render_color", true);
	}
	
	public static boolean enableGesture(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("enable_gesture", false);
	}
	
	public static boolean notitleReimage(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("content_notitle_reimage", true);
	}
	
	public static boolean qmdImage(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("content_qmdimage", false);
	}
	
	public static boolean fromSh(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("log_from_sh", true);
	}
	
//	public static boolean autoLoginWeibo(Context context) {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//		return prefs.getBoolean("weibo_auto_login", false);
//	}
//
//	public static boolean autoLoginKaixin(Context context) {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//		return prefs.getBoolean("kaixin_auto_login", false);
//	}
	
	public static int cacheTime(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString("cache_time", "5"));
	}
	
	public static int zipImage(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString("image_zip", "1"));
	}
	
	public static boolean displayHd(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("display_hd", false);
	}
}
