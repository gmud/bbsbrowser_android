package leeon.mobile.BBSBrowser.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.ContentException;
import leeon.mobile.BBSBrowser.IMailAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.MailObject;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;
import leeon.mobile.BBSBrowser.utils.XmlOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

/**
 * 和邮件处理相关的方法
 * 该类所有方法要求登录
 */
public class BBSMailAction implements IMailAction {
	
	/**
	 * 邮件总数，用于翻页
	 */
	static int totalMailCount = 0;
	
	/**
	 * 获取邮件总数
	 */
	public int totalMailCount() {
		return totalMailCount;
	}
	
	/**
	 * 获取邮件列表
	 */
	public List<MailObject> mailList(int start) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_MAIL + 
					(start==0?"":"?" + HttpConfig.BBS_MAIL_START_PARAM_NAME+"="+start);
		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parseMailList(doc, false);
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
	 * 同上
	 */
	public List<MailObject> mailList() throws NetworkException, ContentException {
		return mailList(0);
	}
	
	/**
	 * 检查是否有新邮件
	 */
	public List<MailObject> newMailList() throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_MAIL_NEW);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parseMailList(doc, true);
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
	 * 获取某一个邮件的内容
	 */
	public void conMail(MailObject mail) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_MAIL_CON + mail.getId() +
					"&" + HttpConfig.BBS_MAIL_N_PARAM_NAME + "=" + mail.getNumber();
		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				BBSBodyParseHelper.parseMailContent(doc, mail);
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
	 * 获取某回信邮件的内容
	 */
	public void conReMail(MailObject mail) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_MAIL_RECON + mail.getNumber();
		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				BBSBodyParseHelper.parseMailReContent(doc, mail);
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
	 * 发出邮件
	 * @param backup 是否备份给自己的标志
	 */
	public boolean sendMail(MailObject mail, boolean backup) throws NetworkException, ContentException {
//		HTTP/1.1 200 OK [Server: nginx, Date: Fri, 24 Dec 2010 09:20:26 GMT, Content-Type: text/html; charset=gb18030, Connection: keep-alive, Keep-Alive: timeout=20, Content-Length: 241]
//		<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"><html><head><meta http-equiv='Refresh' content='1; url=pstmail' />
//		</head>
//		<body>发表成功，1秒钟后自动转到<a href='pstmail'>原页面</a>
//		</body>
//		</html>
//		HTTP/1.1 400 Bad Request [Server: nginx, Date: Fri, 24 Dec 2010 09:21:47 GMT, Content-Type: text/html; charset=gb18030, Connection: keep-alive, Keep-Alive: timeout=20, Content-Length: 127]
//		<html><head><title>发生错误</title></head><body><div>参数错误</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		HttpClient client = HttpConfig.newInstance();
		
		HttpPost post = new HttpPost(HttpConfig.bbsURL() + HttpConfig.BBS_MAIL_SEND);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_MAIL_SEND_REF_PARAM_NAME, "pstmail"));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_MAIL_SEND_RECV_PARAM_NAME, mail.getSender()));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_MAIL_SEND_TITLE_PARAM_NAME, mail.getTitle()));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_MAIL_SEND_CONTENT_PARAM_NAME, mail.getContent()));
		if (backup) nvps.add(new BasicNameValuePair(HttpConfig.BBS_MAIL_SEND_BACKUP_PARAM_NAME, "backup"));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, BBSBodyParseHelper.BBS_CHARSET));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isHttp200(response)) {
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
	 * 删除邮件
	 */
	public boolean delMail(MailObject mail) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_MAIL_DEL + mail.getId());

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isXmlContentType(response)) {
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
