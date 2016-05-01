package leeon.mobile.BBSBrowser;

import java.util.List;

import org.apache.http.cookie.Cookie;

import android.content.Context;

public class CookieCacheUtil {
	
	private static final String COOKIE_CACHE_PREFIX = "cookiecache.";
	private static final String COOKIE_USER_ID = "utmpuserid";
	
	public static void loginWriteCookie(String id, String content, Context context) {
		UIUtil.createCacheFile(COOKIE_CACHE_PREFIX + id.toLowerCase(), content, context);
	}
	
	public static void logoutRemoveCookie(String id, Context context) {
		UIUtil.deleteCacheFile(COOKIE_CACHE_PREFIX + id.toLowerCase(), context);
	}
	
	public static String hasNoRemoveCookie(String id, Context context) {
		byte[] ret = UIUtil.readCacheFile(COOKIE_CACHE_PREFIX + id.toLowerCase(), context);
		if (ret == null) return null;
		else return new String(ret);
	}
	
	public static String getCookieUserId(List<Cookie> list) {
		for (Cookie cookie : list)
			if (COOKIE_USER_ID.equals(cookie.getName())) return cookie.getValue();
		return null;
	}

}
