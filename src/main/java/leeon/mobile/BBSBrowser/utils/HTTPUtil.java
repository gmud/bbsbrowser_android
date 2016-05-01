package leeon.mobile.BBSBrowser.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import leeon.mobile.BBSBrowser.NetworkException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * android下使用http client类的一些工具方法
 * @author leeon
 */
public class HTTPUtil {
	
	//以下为用来下载，基本的client默认方法，在每一个bbs或者http网站实现中，
	//可以修改基本的配置，使用自己的配置类
	//特别是对于路由这块配置信息，可以进行修改调整
	public static HttpParams cloneDefault() {		
		HttpParams p = new BasicHttpParams(); 
		// Create and initialize HTTP parameters
		HttpConnectionParams.setConnectionTimeout(p, 30*1000);//建立http连接，30秒超时
		HttpConnectionParams.setSoTimeout(p, 2*60*1000);//建立http连接后，数据读取间隔，2分钟超时，下载时间超过2分钟停止
		ConnManagerParams.setMaxTotalConnections(p, 100);//连接管理器最大100个连接
		ConnManagerParams.setTimeout(p, 5*60*1000);//在向连接管理器要空闲连接时，线程阻塞5分钟超时
		HttpProtocolParams.setVersion(p, HttpVersion.HTTP_1_1);
				
		//Increase max connections for url:80 to 50
		//connPerRoute.setMaxForRoute(new HttpRoute(new HttpHost(BBS_HOST, 80)), 5);//到bbs的路由的连接20最大20个
		ConnManagerParams.setMaxConnectionsPerRoute(p, new ConnPerRouteBean(5));//每个路由默认分配的最大连接数是5个
		
		return p;
	}
		
	//http client 的缓存池
	private static Map<HttpParams, HttpClient> client = new HashMap<HttpParams, HttpClient>();
	
	/**
	 * 创建http client实例
	 */
	public static HttpClient newInstance(HttpParams params) {
		if (!client.containsKey(params)) {
	        // Create and initialize scheme registry 
	        SchemeRegistry schemeRegistry = new SchemeRegistry();
	        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	        
	        // Create an HttpClient with the ThreadSafeClientConnManager.
	        // This connection manager must be used if more than one thread will
	        // be using the HttpClient.
	        // HttpHost proxy = new HttpHost("localhost", 8888);
	        // params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
	        DefaultHttpClient c = new HTTPUtil.DefaultHttpClientEx(cm, params);
	        
	        //put into client
	        client.put(params, c);
		}
		return client.get(params);
	}
	
	/**
	 * 关闭client
	 */
	public static void shutdownClient(HttpParams params) {
		if (client.containsKey(params)) {
			client.get(params).getConnectionManager().shutdown();
			client.remove(params);
		}
	}
	
	public static void shutdownAll() {
		Iterator<HttpParams> i = client.keySet().iterator();
		while (i.hasNext()) {
			HttpParams params = i.next();
			client.get(params).getConnectionManager().shutdown();
			i.remove();
		}
	}
	
	//加入cookie的默认实现
	public static class DefaultHttpClientEx extends DefaultHttpClient {
		
		public CookieStore cookieStore;
		public DefaultHttpClientEx(ClientConnectionManager cm, HttpParams params) {
			super(cm, params);
		}
		
		@Override
	    protected CookieStore createCookieStore() {
			cookieStore = new BasicCookieStore();
	        return cookieStore;
	    }
	}
	
	
	//以下为用来下载，检查更新等方法使用的client默认配置
	
	private static final HttpParams FILE_PARAMS = cloneDefault();
	private static HttpClient newInstance() {
		return newInstance(FILE_PARAMS);
	}
	
	/**
	 * 文件下载的公共方法
	 * @param url 下载文件的网络地址
	 * @param localFile 下载的本地路径
	 * @throws NetworkException 网络异常
	 */
	public static void downloadFile(String url, File localFile) throws NetworkException {
		downloadFile(url, localFile, newInstance());
	}
	
	public static void downloadFile(String url, File localFile, HttpClient client) throws NetworkException {
		if (client == null) client = newInstance();
		HttpGet get = new HttpGet(url);
		
		HttpResponse response = null;
		FileOutputStream fileOutputStream = null;
		
		try {
			response = client.execute(get);
			if (response != null) {
				InputStream is = response.getEntity().getContent();
				fileOutputStream = new FileOutputStream(localFile);				

				byte[] buf = new byte[1024];
				int ch = -1;
				while ((ch = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, ch);
				}
				fileOutputStream.flush();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
					if (response != null) consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 下载在线文本文件的方法
	 * @param url 网络地址
	 * @return 字节流数组
	 * @throws NetworkException
	 */
	public static byte[] downloadFileToByteArray(String url) throws NetworkException {
		return downloadFileToByteArray(url, newInstance());
	}
	
	public static byte[] downloadFileToByteArray(String url, HttpClient client) throws NetworkException {
		if (client == null) client = newInstance();
		HttpGet get = new HttpGet(url);
		
		HttpResponse response = null;
		ByteArrayOutputStream baos = null;
		
		try {
			response = client.execute(get);
			if (response != null) {
				InputStream is = response.getEntity().getContent();
				baos = new ByteArrayOutputStream(1024);
				byte[] buf = new byte[1024];
				int ch = -1;
				while ((ch = is.read(buf)) != -1) {
					baos.write(buf, 0, ch);
				}
				baos.flush();
			}
			return baos.toByteArray();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		} finally {
			if (baos != null) {
				try {
					baos.close();
					if (response != null) consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 查看在线文本文件的方法
	 * @param url 文本文件网络地址
	 * @param charsetName 编码
	 * @return 返回文本文件内容的字符串
	 * @throws NetworkException
	 */
	public static String viewTextFile(String url, String charsetName) throws NetworkException {
		byte[] t = downloadFileToByteArray(url);
		try {
			return new String(t, charsetName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	
	/**
	 * 同上，默认UTF8的编码查看
	 */
	public static String viewTextFile(String url) throws NetworkException {
		return viewTextFile(url, HTTP.UTF_8);
	}
	
	/**
	 * 消耗entity,android上没有EntityUtil中的consume方法
	 * @param entity
	 * @throws IOException
	 */
	public static void consume(HttpEntity entity) throws IOException {
		if (entity == null)
			return;

		if (entity.isStreaming()) {
			InputStream instream = entity.getContent();
			if (instream != null) {
				instream.close();
			}
		}
	}
	
	/**
	 * 公共方法
	 * 把http entity转为字符串,默认字符集utf8 
	 */
	public static String getHttpBody(HttpEntity entity) {
		return getHttpBody(entity, HTTP.UTF_8);
	}
	
	
	public static String getHttpBody(HttpEntity entity, String charset) {
		String body = "";
		if (entity != null) {
			try {
				body = new String(EntityUtils.toByteArray(entity), charset);
				consume(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return body;
	}
	
	/**
	 * 支持gizp格式的将entity转为bytep[]
	 * 以下系列方法类似
	 */
	public static byte[] toByteArray(HttpEntity entity) {
		if (entity == null) return null;
		
		Header h = entity.getContentEncoding();
        if (h != null && "gzip".equals(h.getValue())) {
        	InputStream is = null;
        	ByteArrayOutputStream baos = null;
        	try {
				is = new GZIPInputStream(entity.getContent());
				baos = new ByteArrayOutputStream(1024);
				byte[] buf = new byte[1024];
				int ch = -1;
				while ((ch = is.read(buf)) != -1) {
					baos.write(buf, 0, ch);
				}
				baos.flush();
				return baos.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				if (baos != null)
					try {
						baos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
        } else {
        	try {
				return EntityUtils.toByteArray(entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return null;
	}
	
	//用charset编码 转为string
	public static String toString(HttpEntity entity, String charset) {
		if (entity == null) return null;
		if (charset == null) charset = HTTP.UTF_8;
		try {
			return new String(toByteArray(entity), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String toString(HttpEntity entity) {
		return toString(entity, null);
	}

	/**
	 * 判断http协议返回的内容是否是xml格式
	 */
	public static boolean isXmlContentType(HttpResponse response) {
		return response.getHeaders("Content-Type") != null 
		&& response.getHeaders("Content-Type").length == 1
		&& response.getHeaders("Content-Type")[0].getValue().indexOf("text/xml") != -1;
	}

	/**
	 * 判断http协议返回的内容是否是html格式
	 */	
	public static boolean isHtmlContentType(HttpResponse response) {
		return response.getHeaders("Content-Type") != null 
		&& response.getHeaders("Content-Type").length == 1
		&& response.getHeaders("Content-Type")[0].getValue().indexOf("text/html") != -1;
	}
	
	/**
	 * 判断http协议返回的状态码是否是200，正常结果
	 */
	public static boolean isHttp200(HttpResponse response) {
		return response.getStatusLine().getStatusCode() == 200 
		&& "OK".equals(response.getStatusLine().getReasonPhrase());
	}
	
	/**
	 * 判断http协议返回的状态码是否是302，需要跳转
	 */
	public static boolean isHttp302(HttpResponse response) {
		return response.getStatusLine().getStatusCode() == 302 
		&& "Found".equals(response.getStatusLine().getReasonPhrase());
	}

	/**
	 * 判断http协议返回的状态码是否是400，失败
	 */
	public static boolean isHttp400(HttpResponse response) {
		return response.getStatusLine().getStatusCode() == 400 
		&& "Bad Request".equals(response.getStatusLine().getReasonPhrase());
	}
	
	/**
	 * 将cookie转化为字符串的方法
	 */
	public static String cookieListToString(List<Cookie> cookieList) {
		StringBuffer ret = new StringBuffer();
		if (cookieList != null) {
			for (Cookie c : cookieList) {
				ret.append(c.getName() + "=" + c.getValue() + ";");
			}
		}
		return ret.toString();
	}
	
	/**
	 * 纯jdk的读取url的方法
	 */
	public static InputStream readUrl(String url) throws IOException {
		URL u = new URL(url);
		//return u.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888))).getInputStream();
		return u.openStream();
	}

	public static void main(String[] args) {
	}

}
