package leeon.mobile.BBSBrowser.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.ContentException;
import leeon.mobile.BBSBrowser.IPostAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

/**
 * 和发文相关操作的方法
 * 该类所有方法要求登录
 */ 
public class BBSPostAction implements IPostAction {
	
	/**
	 * 读取re文时的一些被re的文章内容
	 * 读取版面的一些基本信息的方法
	 * doc参数为被re的原文
	 */
	public DocObject inPostDoc(BoardObject board, DocObject doc) throws NetworkException, ContentException {
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_DOC_POST + board.getId();
		if (doc != null) url += "&" + HttpConfig.BBS_CON_FILE_PARAM_NAME + "=" + doc.getId();
		
		HttpClient client = HttpConfig.newInstance();		
		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isXmlContentType(response)) {
				Document document = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parsePostContent(document, board);
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
	 * 发新文章时用于读取版面的一些基本信息的方法
	 */	
	public DocObject inPostDoc(BoardObject board) throws NetworkException, ContentException {
		return inPostDoc(board, null);
	}
	
	/**
	 * 发帖的方法
	 * newdoc是要发的新贴，修改老帖时该值为null
	 * olddoc是被re的老帖，或者被修改的老帖，发新帖该值为null
	 * anony 是否匿名，匿名版有效
	 * edit  是否是修改老帖
	 * sig   使用的签名档
	 */
	public boolean sendPostDoc(DocObject newdoc, DocObject olddoc, boolean anony, boolean edit, int sig) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		
		HttpPost post = new HttpPost(HttpConfig.bbsURL() + HttpConfig.BBS_DOC_SEND + (newdoc != null?newdoc.getBoard().getId():olddoc.getBoard().getId()));
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (olddoc != null) nvps.add(new BasicNameValuePair(HttpConfig.BBS_CON_FILE_PARAM_NAME, olddoc.getId()));
		if (newdoc != null) nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_BOARD_PARAM_NAME, newdoc.getBoard().getName()));
		else if (olddoc != null) nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_BOARD_PARAM_NAME, olddoc.getBoard().getName()));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_EDIT_PARAM_NAME, edit?"1":"0"));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_ANONY_PARAM_NAME, anony?"1":"0"));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_SIG_PARAM_NAME, String.valueOf(sig)));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_TITLE_PARAM_NAME, newdoc != null?newdoc.getTitle():olddoc.getTitle()));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_POST_CONTENT_PARAM_NAME, newdoc != null?newdoc.getContent():olddoc.getContent()));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, BBSBodyParseHelper.BBS_CHARSET));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isHttp200(response)) {
				String body = HTTPUtil.getHttpBody(entity, BBSBodyParseHelper.BBS_CHARSET);
				if (body.indexOf("成功") != -1)
					return true;
				else {
					String msg = BBSBodyParseHelper.parseFailMsg(body);
					throw new ContentException(msg);
				}					
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
	 * 上传附件操作
	 * @param file 被上传文件
	 * @param board 
	 * @return 返回成功上传后的图片url
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public String sendAttFile(File file, BoardObject board, String mimeType) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		
		HttpPost post = new HttpPost(HttpConfig.bbsURL() + HttpConfig.BBS_DOC_UPLOAD + board.getName());
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT);
		reqEntity.addPart(HttpConfig.BBS_DOC_UPLOAD_FILE_PARAM_NAME, new BBSPostAction().new ExFileBody(file, mimeType==null?"image/jpeg":mimeType));
		post.setEntity(reqEntity);

		try {
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isHttp200(response) && HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parseAttachURL(doc);
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
	 * 删帖操作，可以删除自己发的贴子，版主是否能删文没有测试过
	 * @param doc 被删帖子
	 * @return 成功返回true，失败抛错
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public boolean delPostDoc(DocObject doc) throws NetworkException, ContentException {
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_DOC_DEL + doc.getBoard().getId();
		if (doc != null) url += "&" + HttpConfig.BBS_CON_FILE_PARAM_NAME + "=" + doc.getId();
		
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
	 * 转信操作
	 * @param doc 被转文档
	 * @param user 转给的用户
	 * @return 成功返回true，失败抛错
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public boolean fwdPostDoc(DocObject doc, String user) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		
		HttpPost post = new HttpPost(HttpConfig.bbsURL() + HttpConfig.BBS_DOC_FWD + doc.getBoard().getId());
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_CON_FILE_PARAM_NAME, doc.getId()));
		nvps.add(new BasicNameValuePair(HttpConfig.BBS_DOC_FWD_USER_PARAM_NAME, user));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, BBSBodyParseHelper.BBS_CHARSET));
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			
			if (HTTPUtil.isHttp200(response)) {
				String body = HTTPUtil.getHttpBody(entity, BBSBodyParseHelper.BBS_CHARSET);
				if (body.indexOf("文章转寄成功") != -1)
					return true;
				else {
					String msg = BBSBodyParseHelper.parseFailMsg(body);
					throw new ContentException(msg);
				}					
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
	 * 转载操作
	 * @param doc 被转贴子
	 * @param to 转到的版面
	 * @return 成功返回true，失败抛错
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public boolean cccPostDoc(DocObject doc, BoardObject to) throws NetworkException, ContentException {		
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_DOC_CCC + doc.getBoard().getId();
		url += "&" + HttpConfig.BBS_CON_FILE_PARAM_NAME + "=" + doc.getId();
		url += "&" + HttpConfig.BBS_DOC_CCC_BOARD_PARAM_NAME + "=" + to.getName();

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
	 * 上传附件被重载的类
	 * 重点将transferEncoding置为null
	 * 否则上传后的pic会在前后多出两个/n/r的字节
	 * ie显示不出来
	 * 这是bbs上传操作的bug
	 */
	public class ExFileBody extends FileBody {

		private String filename = null;
		public ExFileBody(File file, String mimeType) {
			super(file, mimeType);
			if (file != null) {
				String n = file.getName().toLowerCase();
				if (!(n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".gif"))) {
					filename = n + getImageFileType(mimeType);
				}
			}
			
		}
		
		public String getFilename() {
			if (filename == null)
				return super.getFilename();
			else
				return filename;
		}
		
		public String getTransferEncoding() {
			return null;
		}
		
		private String getImageFileType(String mimeType) {
	    	if ("image/jpeg".equals(mimeType))
	    		return ".jpg";
	    	else if ("image/gif".equals(mimeType))
	    		return ".gif";
	    	else if ("image/png".equals(mimeType))
	    		return ".png";
	    	else if ("application/x-bmp".equals(mimeType))
	    		return ".bmp";
	    	else
	    		return "";
	    }
	}
}
