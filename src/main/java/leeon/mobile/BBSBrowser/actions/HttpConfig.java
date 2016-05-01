package leeon.mobile.BBSBrowser.actions;

import java.util.Calendar;
import java.util.TimeZone;

import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.sjtu.SjtuHttpConfig;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;
import leeon.mobile.BBSBrowser.yanxi.YanxiHttpConfig;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.params.HttpParams;

/**
 * 定义bbs基本url和参数的config类
 * 另外还包括一些用于http协议的基本方法
 */
public class HttpConfig {
	
	/**
	 * 接入的域名，sh还是edu
	 */
	public static boolean SH_NO_EDU = true;

	/**
	 * 可能的接入点
	 */
	public static final String BBS_HOST1 = "bbs.fudan.edu.cn";
	
	public static final String BBS_HOST2 = "bbs.fudan.sh.cn";
	
	public static final String BBS_HOST3 = "bbsapp.191922.co.cc";
	
	public static final String BBS_URL1 = "https://" + BBS_HOST1;
	
	public static final String BBS_URL2 = "https://" + BBS_HOST2;
	
	public static final String BBS_URL3 = "https://" + BBS_HOST3;
			
	public static final String BBS_BOARD = "sec";
	
	public static final String BBS_FAV = "fav";
	
	public static final String BBS_FAV_ADD = "brdadd?bid=";
	
	public static final String BBS_FAV_SETTING = "mybrd?type=1";
	
	public static final String BBS_BLOCK = "boa?s=";
	
	public static final String BBS_BLOCK_DIR = "boa?board=";
	
	public static final String BBS_DOC = "doc?board=";
	
	public static final String BBS_DOC_TITLE = "tdoc?board=";
	
	public static final String BBS_DOC_START_PARAM_NAME = "start";
	
	public static final String BBS_CON = "con?bid=";
	
	public static final String BBS_CON_TITLE = "tcon?bid=";
	
	public static final String BBS_CON_TITLE_BNAME = "tcon?board=";
	
	public static final String BBS_CON_FILE_PARAM_NAME = "f";
	
	public static final String BBS_CON_TITLE_PARAM_NAME = "g";
	
	public static final String BBS_CON_NP_PARAM_NAME = "a=n";
	
	public static final String BBS_CON_PP_PARAM_NAME = "a=p";
	
	public static final String BBS_CON_AP_PARAM_NAME = "a=a";
	
	public static final String BBS_CON_S_PARAM_NAME = "s=1";
	
	public static final String BBS_LOGIN = "login";
	
	public static final String BBS_LOGIN_USERID_PARAM_NAME = "id";
	
	public static final String BBS_LOGIN_PASSWORD_PARAM_NAME = "pw";
	
	public static final String BBS_LOGOUT = "logout";
	
	public static final String BBS_TOPTEN = "top10";
	
	public static final String BBS_MAIL_NEW = "newmail";
	
	public static final String BBS_MAIL = "mail";
	
	public static final String BBS_MAIL_START_PARAM_NAME = "start";
	
	public static final String BBS_MAIL_CON = "mailcon?f=";
	
	public static final String BBS_MAIL_RECON = "pstmail?n=";
	
	public static final String BBS_MAIL_DEL = "delmail?f=";
	
	public static final String BBS_MAIL_N_PARAM_NAME = "n";
	
	public static final String BBS_MAIL_SEND = "sndmail";
	
	public static final String BBS_MAIL_SEND_REF_PARAM_NAME = "ref";
	
	public static final String BBS_MAIL_SEND_RECV_PARAM_NAME = "recv";
	
	public static final String BBS_MAIL_SEND_TITLE_PARAM_NAME = "title";
	
	public static final String BBS_MAIL_SEND_CONTENT_PARAM_NAME = "text";
	
	public static final String BBS_MAIL_SEND_BACKUP_PARAM_NAME = "backup";
	
	public static final String BBS_DOC_SEND = "snd?bid=";
	
	public static final String BBS_DOC_POST_BOARD_PARAM_NAME = "brd";
	
	public static final String BBS_DOC_POST_EDIT_PARAM_NAME = "e";
	
	public static final String BBS_DOC_POST_ANONY_PARAM_NAME = "anony";
	
	public static final String BBS_DOC_POST_ATT_PARAM_NAME = "attach";
	
	public static final String BBS_DOC_POST_SIG_PARAM_NAME = "sig";
	
	public static final String BBS_DOC_POST_TITLE_PARAM_NAME = "title";
	
	public static final String BBS_DOC_POST_CONTENT_PARAM_NAME = "text";

	public static final String BBS_DOC_EDIT = "edit?bid=";
	
	public static final String BBS_DOC_POST = "pst?bid=";
	
	public static final String BBS_DOC_DEL = "del?bid=";
	
	public static final String BBS_DOC_FWD = "fwd?bid=";
	
	public static final String BBS_DOC_FWD_USER_PARAM_NAME = "u";
	
	public static final String BBS_DOC_CCC = "ccc?bid=";
	
	public static final String BBS_DOC_CCC_BOARD_PARAM_NAME = "t";
	
	public static final String BBS_DOC_UPLOAD = "upload?b=";
	
	public static final String BBS_DOC_UPLOAD_FILE_PARAM_NAME = "up";
	
	public static final String BBS_DOC_FIND = "bfind?bid=";
	
	public static final String BBS_DOC_FIND_TITLE_PARAM_NAME1 = "t1";
	
	public static final String BBS_DOC_FIND_TITLE_PARAM_NAME2 = "t2";
	
	public static final String BBS_DOC_FIND_TITLE_PARAM_NAME3 = "t3";
	
	public static final String BBS_DOC_FIND_AUTHOR_PARAM_NAME = "user";
	
	public static final String BBS_DOC_FIND_LIMIT_PARAM_NAME = "limit";
	
	public static final String BBS_DOC_FIND_MARK_PARAM_NAME = "&mark=on";
	
	public static final String BBS_DOC_FIND_NORE_PARAM_NAME = "&nore=on";
	
	public static final String BBS_0AN = "0an?path=";
	
	public static final String BBS_0AN_BOARD = "0an?bid=";
	
	public static final String BBS_ANC = "anc?path=";
	
	public static final int BBS_PAGE_SIZE = 20;
	
	/**
	 * 默认的附件上传版面，
	 * 如果版面不支持附件，使用该版面上传附件
	 * 包括邮件的附件也可以使用该附件
	 */
	public static final BoardObject DEFAULT_UPLOAD_BOARD = new BoardObject("11", "PIC", "贴图乐园");
	
	
	/**
	 * 访问bbs xml或者html所用的default 的http client实现
	 * 这些路由的连接数量控制在2个
	 * 在其他的实现里可以clone出一个新的param也可以利用该param
	 */
	public static final HttpParams BBS_PARAMS = HTTPUtil.cloneDefault();
	static {
		ConnPerRouteBean connPerRoute = new ConnPerRouteBean(2);
		connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(BBS_HOST1, 80)), 5);//到fdu bbs的路由的连接最大5个
		connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(BBS_HOST2, 80)), 5);//到fdu bbs的路由的连接最大5个
		connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(BBS_HOST3, 80)), 5);//到fdu bbs的路由的连接最大5个
		connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(SjtuHttpConfig.BBS_HOST, 80)), 5);//sjtu bbs
		connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(SjtuHttpConfig.BBS_HOST, 443)), 5);//sjtu bbs
		connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(YanxiHttpConfig.BBS_HOST, 80)), 5);//yanxi bbs
		ConnManagerParams.setMaxConnectionsPerRoute(BBS_PARAMS, connPerRoute);
	}
	public static HttpClient newInstance() {
		return HTTPUtil.newInstance(BBS_PARAMS);
	}
	
	public static String bbsURL() {
		return "https://" + bbsHost() + "/bbs/";
	}
	
	public static String bbsHost() {
		return useServer()?BBS_HOST3:(SH_NO_EDU?BBS_HOST2:BBS_HOST1);
	}
	
	/**
	 * 判断是否在android环境还是在GAE环境
	 * 在GAE返回false，在android再根据时间判断是否返回true
	 */
	public static boolean useServer() {
		try {
			Class.forName("com.google.appengine.api.utils.SystemProperty");
			return false;
		} catch (ClassNotFoundException e) {
			int h = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
					.get(Calendar.HOUR_OF_DAY);
			return (h >= 0 && h < 9);
			//return true;
		}
	}
}
