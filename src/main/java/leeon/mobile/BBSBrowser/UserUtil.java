package leeon.mobile.BBSBrowser;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BoardObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;

public class UserUtil {
	
	//主题模式静态变量，默认使用
	public static boolean TITLE_MODE = true;
	
	//HTML看图模式静态变量，默认看图
	public static boolean HTML_MODE = true;
	
	//本地用户数据
	public static final String APP_USER_SETTING = "settings";
	
	public static final String APP_USER_SETTING_LASTLOGIN = "lastLogin";
	
	public static final String APP_USER_SETTING_WB_USER = "wb.user";
	
	public static final String APP_USER_SETTING_WB_PASSWORD = "wb.password";
	
	public static final String APP_USER_SETTING_KAIXIN_USER = "kaixin.user";
	
	public static final String APP_USER_SETTING_KAIXIN_PASSWORD = "kaixin.password";
	
	public static final String APP_USER_SETTING_USERS = "userList";
	
	public static final String APP_USER_SETTING_USER_PASSWORD = "userPassword.";
	
	public static final String APP_USER_SETTING_USER_TITLE_MODE = "titleMode.";
	
	public static final String APP_USER_SETTING_USER_HTML_MODE = "htmlMode.";
	
	public static final String APP_USER_SETTING_USER_FAV_LIST = "favList.";
	
	public static final String APP_USER_SETTING_OTHER_FAV_LIST = "otherFavList.";
	
	
	public static GestureLibrary glib;
	
	//记录登录的id信息，guest的话是null
	public static String CURRENT_USER_ID = null;
	
	//返回上次登录的用户
	public static String getLastLoginId(Context context) {
		init(context);
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		return setting.getString(APP_USER_SETTING_LASTLOGIN, null);
	}
	
	//返回某个用户的密码
	public static String getLoginPassword(Context context, String userId) {
		if (userId == null || userId.length() == 0) return null;
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		return setting.getString(APP_USER_SETTING_USER_PASSWORD + userId, null);
	}
	
	//返回已经登录过的用户清单
	public static String[] getUserList(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		String l = setting.getString(APP_USER_SETTING_USERS, null);
		return l == null?null:l.split(","); 
	}
	
	//获取上次记录的登录wb的用户名
	public static String getWeiBoUser(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		return setting.getString(APP_USER_SETTING_WB_USER, null);
	}
	
	//获取上次记录的登录wb的密码
	public static String getWeiBoPassword(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		return setting.getString(APP_USER_SETTING_WB_PASSWORD, null);
	}
	
	//获取上次记录的登录kaixin的用户名
	public static String getKaixinUser(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		return setting.getString(APP_USER_SETTING_KAIXIN_USER, null);
	}
	
	//获取上次记录的登录kaixin的密码
	public static String getKaixinPassword(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		return setting.getString(APP_USER_SETTING_KAIXIN_PASSWORD, null);
	}
	
	//是否主题模式
	public static void isTitleMode(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		TITLE_MODE = setting.getBoolean(APP_USER_SETTING_USER_TITLE_MODE + CURRENT_USER_ID, true);
	}
	
	//是否看图模式
	public static void isHtmlMode(Context context) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		HTML_MODE = setting.getBoolean(APP_USER_SETTING_USER_HTML_MODE + CURRENT_USER_ID, true);
	}
	
	//favList,存取顺序id,name,ch;
	public static List<BoardObject> getFavList(Context context, String user) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		String fav = setting.getString(APP_USER_SETTING_USER_FAV_LIST + user, null);
		return parseFavList(fav);
	}
	
	//other favList,存取顺序id,name,ch;
	public static List<BoardObject> getOtherFavList(Context context, int actionParam) {
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		String fav = setting.getString(APP_USER_SETTING_OTHER_FAV_LIST + actionParam, null);
		return parseFavList(fav);
	}
	
	private static List<BoardObject> parseFavList(String fav) {
		List<BoardObject> ret = new ArrayList<BoardObject>();
		if (fav == null || fav.length() == 0) return ret;
		for (String f : fav.split(";")) {
			String[] s = f.split(",");
			if (s.length == 3) ret.add(new BoardObject(s[0], s[1], s[2]));
		}
		return ret;
	}
	
	//保存微博的用户和密码
//	public static void saveWeiBoUserInfo(Context context, String user, String password) {
//		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
//		setting.edit()
//			.putString(APP_USER_SETTING_WB_USER, user)
//			.putString(APP_USER_SETTING_WB_PASSWORD, password)
//			.commit();
//	}

	
	//保存最新登录用户
	public static void saveLoginId(Context context) {
		if (CURRENT_USER_ID == null) return;
		
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);
		setting.edit()
			.putString(APP_USER_SETTING_LASTLOGIN, CURRENT_USER_ID)
			.commit();
		
		//看有没有在用户清单中
		String[] l = getUserList(context);
		if (l != null) {
			for (String u : getUserList(context)) {
				if (u.equals(CURRENT_USER_ID)) return;
			}
		}
		
		//没有就添加
		String s = setting.getString(APP_USER_SETTING_USERS, null);
		if (s == null) {
			setting.edit()
				.putString(APP_USER_SETTING_USERS, CURRENT_USER_ID)
				.commit();
		} else {
			setting.edit()
				.putString(APP_USER_SETTING_USERS, s + "," + CURRENT_USER_ID)
				.commit();
		}
	}
	
	//保存密码
	public static void saveLoginPassword(Context context, String password) {
		if (CURRENT_USER_ID == null) return;
		
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);		
		if (password != null && password.length() != 0)
			setting.edit()
				.putString(APP_USER_SETTING_USER_PASSWORD + CURRENT_USER_ID, password)
				.commit();
		else
			setting.edit()
			.remove(APP_USER_SETTING_USER_PASSWORD + CURRENT_USER_ID)
			.commit();
	}
	
	//保存主题模式
	public static void saveTitleMode(Context context) {
		if (CURRENT_USER_ID == null) return;
		
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);		
		setting.edit()
			.putBoolean(APP_USER_SETTING_USER_TITLE_MODE + CURRENT_USER_ID, TITLE_MODE)
			.commit();
	}
	
	//保存html看图模式
	public static void saveHtmlMode(Context context) {
		if (CURRENT_USER_ID == null) return;
		
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);		
		setting.edit()
			.putBoolean(APP_USER_SETTING_USER_HTML_MODE + CURRENT_USER_ID, HTML_MODE)
			.commit();
	}
	
	//保存favList
	public static void saveFavList(Context context, List<BoardObject> list) {
		if (CURRENT_USER_ID == null) return;
		
		String ret = parseFavList(context, list);
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);		
		setting.edit()
			.putString(APP_USER_SETTING_USER_FAV_LIST + CURRENT_USER_ID, ret)
			.commit();
	}
	
	//保存other favList
	public static void saveOtherFavList(Context context, List<BoardObject> list, int actionParam) {
		if (actionParam == ActionFactory.DEFAULT_PARAM) return;

		String ret = parseFavList(context, list);
		SharedPreferences setting = context.getSharedPreferences(APP_USER_SETTING, 0);		
		setting.edit()
			.putString(APP_USER_SETTING_OTHER_FAV_LIST + actionParam, ret)
			.commit();
	}
	
	private static String parseFavList(Context context, List<BoardObject> list) {
		if (list == null) return "";
		String ret = "";
		for (BoardObject b : list)
			ret += b.getId() + "," + b.getName() + "," + b.getCh() + ";";
		if (ret.length() != 0) ret = ret.substring(0, ret.length()-1);
		return ret;
	}
	
	
	//获取快速回复的词组
	public static String[] getPostShortWords(Context context) {
		String re = SettingActivity.getShortWords(context);
		if (re != null)
			return re.split(";");
		else
			return new String[] {"给力!", "顶!", "赞!"};
	}
	
	//初始化一些应用的配置，比如手势库
	private static void init (Context context) {		
		if (glib == null) {
			glib = GestureLibraries.fromRawResource(context, R.raw.gestures);
			glib.load();
		}
	}
}
