package leeon.mobile.BBSBrowser.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.ContentException;
import leeon.mobile.BBSBrowser.ILogAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;
import leeon.mobile.BBSBrowser.utils.XmlOperator;
import leeon.mobile.BBSBrowser.utils.HTTPUtil.DefaultHttpClientEx;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;

/**
 * 登录和退出相关操作方法类
 * 该类和收藏有关的方法要求登录
 */
public class BBSLogAction implements ILogAction {

	
	
	/**
	 * 系统登录方法，登录成功返回true，
	 * 否则抛出ContentException
	 */
	public boolean login(String userId, String password) throws ContentException, NetworkException {
		return login(userId, password, null);
	}
	
	/**
	 * 登录，带入cookie
	 */
	public boolean login(String userId, String password, List<Cookie> cookieList) throws ContentException, NetworkException {
		HttpClient client = HttpConfig.newInstance();
		HttpPost post = new HttpPost(HttpConfig.bbsURL() + HttpConfig.BBS_LOGIN);
		HttpResponse response = null;
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_LOGIN_USERID_PARAM_NAME, userId));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_LOGIN_PASSWORD_PARAM_NAME, password));
		// HTTP/1.1 302 Found
		// [Server: nginx, Date: Thu, 02 Dec 2010 07:23:35 GMT, Content-Type:
		// text/html; charset=gb18030, Connection: keep-alive, Keep-Alive:
		// timeout=20, Set-cookie: utmpnum=3334, Set-cookie: utmpkey=93830186,
		// Set-cookie: utmpuserid=leeon, Location: sec, Content-Length: 0]
		
		// HTTP/1.1 200 OK 
		// [Server: nginx, Date: Mon, 06 Dec 2010 07:28:57 GMT, Content-Type: 
		// text/html; charset=gb18030, Connection: keep-alive, Keep-Alive: 
		// timeout=20, Content-Length: 139]
		
		// [Server: nginx, Date: Sat, 11 Dec 2010 12:34:33 GMT, Content-Type: 
		// text/xml; charset=gb18030, Connection: keep-alive, Keep-Alive: 
		// timeout=20, Content-Length: 7066]
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			response = client.execute(post);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
		
		if (cookieList != null) {
			DefaultHttpClientEx c = (DefaultHttpClientEx) client;
			if (c.cookieStore != null)
				cookieList.addAll(c.cookieStore.getCookies());
		}

		//add wait 600ms to avoid fetching fav for first login
		try
		{
			Thread.sleep(600);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		//普通登录成功
		if (HTTPUtil.isHttp302(response)) {
			try {
				HTTPUtil.consume(response.getEntity());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		//Android登录的情况
		} else if (HTTPUtil.isHttp200(response)) {				
			//if  content type is xml
			if (HTTPUtil.isXmlContentType(response)) {
				try {
					HTTPUtil.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			//not xml
			} else {
				//获取msg
				String msg = BBSBodyParseHelper.parseFailMsg(response.getEntity());
				//抛出
				throw new ContentException(msg);
			}
		} else {
			throw new NetworkException();
		}
	}

	
	/**
	 * 退出登录方法
	 * 一定要调,不调死翘翘
	 */
	public void logout() throws NetworkException {
		logout(null);
	}
	
	/**
	 * 退出并消灭session
	 */
	public void logout(String cookieString) throws NetworkException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_LOGOUT);

		if (cookieString != null && cookieString.length() != 0)
			get.setHeader("Cookie", cookieString);
		// HTTP/1.1 200 OK 
		// [Server: nginx, Date: Mon, 06 Dec 2010 07:28:57 GMT, Content-Type: 
		// text/html; charset=gb18030, Connection: keep-alive, Keep-Alive: 
		// timeout=20, Content-Length: 6623]		
		try {
			HttpResponse response = client.execute(get);
			if (response != null && response.getEntity() != null) {
				HTTPUtil.consume(response.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 获取收藏版面
	 */
	public List<BoardObject> favBoard() throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_FAV);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if (HTTPUtil.isHttp200(response) && HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parseFavBoardList(doc);
			} else {
				String msg = BBSBodyParseHelper.parseFailMsg(entity);
				throw new ContentException(msg);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

	/**
	 * 添加收藏版面
	 */	
	public boolean addFavBoard(BoardObject board) throws NetworkException, ContentException {
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_FAV_ADD + board.getId();
		
		HttpClient client = HttpConfig.newInstance();		
		HttpGet get = new HttpGet(url);
		
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isHttp200(response) && HTTPUtil.isXmlContentType(response)) {
				HTTPUtil.consume(response.getEntity());
				return true;
			} else {
				String msg = BBSBodyParseHelper.parseFailMsg(entity);
				throw new ContentException(msg);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 设定收藏的版面
	 * 用于删除收藏版面
	 */
	public boolean setFavBoard(List<BoardObject> list) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		
		HttpPost post = new HttpPost(HttpConfig.bbsURL() + HttpConfig.BBS_FAV_SETTING);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (BoardObject board : list)
			nvps.add(new BasicNameValuePair(board.getId(), "on"));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, BBSBodyParseHelper.BBS_CHARSET));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isHttp200(response) && HTTPUtil.isXmlContentType(response)) {
				HTTPUtil.consume(response.getEntity());
				return true;
			} else {
				String msg = BBSBodyParseHelper.parseFailMsg(entity);
				throw new ContentException(msg);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

}