package leeon.mobile.BBSBrowser.actions;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import leeon.mobile.BBSBrowser.ContentException;
import leeon.mobile.BBSBrowser.IDocAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;
import leeon.mobile.BBSBrowser.utils.XmlOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;

/**
 * 和文章读取相关的方法
 */
public class BBSDocAction implements IDocAction {
	
	/**
	 * 查找top 10的方法
	 */
	public List<DocObject> topTenDocList() throws NetworkException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_TOPTEN);
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
						
			Document doc = XmlOperator.readDocument(entity.getContent());
			return BBSBodyParseHelper.parseTopTenList(doc); 
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
		
	/**
	 * 计算start
	 */
	static int calculateStartFromFetched(BoardObject board, int fetchedTotalCount, int stickyCount) {
		int start = 0;
		if (fetchedTotalCount != 0) {
			start = board.getTotal()-fetchedTotalCount-HttpConfig.BBS_PAGE_SIZE+1+stickyCount;
		}
		return start;
	}
	
	/**
	 * 处理第一页的重复数据
	 */
	static List<DocObject> dealFirstPage(List<DocObject> list, int endFromIndex) {
		for (int i = list.size()-1 ; endFromIndex > 0 && i >= 0; i--, endFromIndex--)
			list.remove(i);
		return list;
	}
		
	/**
	 * 版面文章列表
	 * @param isTitle 是否主题模式
	 */
	public List<DocObject> boardDoc(BoardObject board, boolean isTitle) throws NetworkException {
		return boardDoc(board, isTitle, 0, 0);
	}
	
	/**
	 * 同上
	 */
	public List<DocObject> boardDoc(BoardObject board, boolean isTitle, int fetchedTotalCount, int stickyCount) throws NetworkException {
		int start = 0;
		String url;
		if (fetchedTotalCount == 0) {
			url = boardDocUrl(board, isTitle, 0);//total==0表示第一次提取，最后一页
		} else {
			start = calculateStartFromFetched(board, fetchedTotalCount, stickyCount);//total不为0，那么就是非最后一页，计算start
			url = boardDocUrl(board, isTitle, start<1?1:start);//小于1说明到第一页了
		}
		
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			Document doc = XmlOperator.readDocument(entity.getContent());
			if (fetchedTotalCount == 0)
				return BBSBodyParseHelper.parseDocList(doc, board);//第一次提起，最后一页，全部显示 
			else
				return dealFirstPage(BBSBodyParseHelper.parseDocList(doc, board), start<1?-start+1:0);
			//modify leeon 需要计算如果是第一页有多少重复数据，从第几个开始提取;
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 组装版面文章列表的url
	 */
	static String boardDocUrl(BoardObject board, boolean isTitle, int start) {
		String url = HttpConfig.bbsURL() + (isTitle?HttpConfig.BBS_DOC_TITLE:HttpConfig.BBS_DOC) + 
				board.getName() + 
				(start==0?"":"&" + HttpConfig.BBS_DOC_START_PARAM_NAME+"="+start);
		return url;
	}
	
	/**
	 * 查找版面文章的方法
	 * @param board 被查版面
	 * @param t1 包含标题 1
	 * @param t2 包含标题 2
	 * @param t3 不包含标题
	 * @param author 作者
	 * @param limit 时间限制
	 * @param mark 是否标记
	 * @param nore 不包含回文
	 * @return
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public static List<DocObject> findDoc(BoardObject board, String t1, String t2, String t3, String author, int limit, boolean mark, boolean nore) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		
		String url = HttpConfig.bbsURL() + HttpConfig.BBS_DOC_FIND + board.getId();
		if (limit <= 0) limit = 1;
		else if (limit > 30) limit = 30;
		url += "&" + HttpConfig.BBS_DOC_FIND_LIMIT_PARAM_NAME + "=" + limit;
		if (mark) url += HttpConfig.BBS_DOC_FIND_MARK_PARAM_NAME;
		if (nore) url += HttpConfig.BBS_DOC_FIND_NORE_PARAM_NAME;
		try {
			if (author != null) url += "&" + HttpConfig.BBS_DOC_FIND_AUTHOR_PARAM_NAME + "=" + URLEncoder.encode(author, BBSBodyParseHelper.BBS_CHARSET);
			if (t1 != null) url += "&" + HttpConfig.BBS_DOC_FIND_TITLE_PARAM_NAME1 + "=" + URLEncoder.encode(t1, BBSBodyParseHelper.BBS_CHARSET);
			if (t2 != null) url += "&" + HttpConfig.BBS_DOC_FIND_TITLE_PARAM_NAME2 + "=" + URLEncoder.encode(t2, BBSBodyParseHelper.BBS_CHARSET);
			if (t3 != null) url += "&" + HttpConfig.BBS_DOC_FIND_TITLE_PARAM_NAME3 + "=" + URLEncoder.encode(t3, BBSBodyParseHelper.BBS_CHARSET);			
			
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
						
			if (HTTPUtil.isXmlContentType(response)) {
				Document doc = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parseDocList(doc, board); 
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
	 * 获取单个文章的内容
	 * 或者是一个主题的内容
	 * 该方法只能获取一个主题模式前二十的帖子
	 * @param doc 获取内容帖子，或者主题模式的首帖
	 * @param isTitle 是否主题模式
	 * @throws NetworkException
	 * @throws ContentException 
	 */
	public List<DocObject> docContent(DocObject doc, boolean isTitle) throws NetworkException, ContentException {
		return docContent(doc, isTitle, null, null);
	}
	
	/**
	 * 获取单个文章的内容
	 * 或者是一个主题的内容
	 * @param doc 获取内容帖子，或者主题模式的首帖
	 * @param isTitle 是否主题模式
	 * @param tag 用于获取文档的上篇，下篇，上楼，下楼，上主题，下主题的标志
	 * @throws NetworkException
	 * @throws ContentException 
	 */
	@Override
	public List<DocObject> docContent(DocObject doc, boolean isTitle, String tag) throws NetworkException, ContentException {
		List<DocObject> ret = docContent(doc, isTitle, null, tag);
		
		if (isTitle && "a=a".equals(tag) && ret != null && ret.size() == 0)
			throw new ContentException("不要再往下了，没有了");
		else
			return ret;
	}
	
	
	/**
	 * 获取单个文章的内容
	 * 或者是一个主题的内容
	 * @param doc 获取内容帖子，或者主题模式的首帖,获取当主题模式的二十贴之后的贴子时，传入最后一个贴子作为入参
	 * @param isTitle 是否主题模式
	 * @param gdoc 非主题模式下无效,主题模式下，再获取二十贴之后的贴子时，传入主贴对象
	 * @throws NetworkException
	 * @throws ContentException 
	 */
	public List<DocObject> docContent(DocObject doc, boolean isTitle, DocObject gdoc) throws NetworkException, ContentException {
		return docContent(doc, isTitle, gdoc, null);
	}
	
	//内部方法
	private List<DocObject> docContent(DocObject doc, boolean isTitle, DocObject gdoc, String tag) throws NetworkException, ContentException {
		HttpClient client = HttpConfig.newInstance();
		String url = docContentUrl(doc, isTitle, gdoc, tag);
		
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = client.execute(get);
			if (HTTPUtil.isXmlContentType(response)) {
				HttpEntity entity = response.getEntity();
			
				Document document = XmlOperator.readDocument(entity.getContent());
				return BBSBodyParseHelper.parseContentList(document, doc);
			} else {
				String msg = BBSBodyParseHelper.parseFailMsg(response.getEntity());
				if ("a=a".equals(tag) || "a=n".equals(tag)) msg = msg.replace("往上", "往下");
				throw new ContentException(msg);
			}
		} catch (ContentException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

	/**
	 * 组装文章内容的url
	 */
	static String docContentUrl(DocObject doc, boolean isTitle, DocObject gdoc, String tag) {
		String url = HttpConfig.bbsURL() + (isTitle?(doc.getBoard().getId()!=null?HttpConfig.BBS_CON_TITLE:HttpConfig.BBS_CON_TITLE_BNAME):HttpConfig.BBS_CON);
		url += (doc.getBoard().getId()!=null?doc.getBoard().getId():doc.getBoard().getName());
		url	+= "&" + HttpConfig.BBS_CON_FILE_PARAM_NAME + "=" + doc.getId();
		if (gdoc != null) url += "&" + HttpConfig.BBS_CON_TITLE_PARAM_NAME + "=" + gdoc.getId() + "&" + HttpConfig.BBS_CON_NP_PARAM_NAME;
		if (tag != null) url += "&" + tag;
		else if (!isTitle && doc.isSticky()) url += "&" + HttpConfig.BBS_CON_S_PARAM_NAME;
		return url;
	}
	
	public static void main(String[] args) throws Exception {
		List<DocObject> list = new BBSDocAction().docContent(new DocObject("3085186122", null, null, null, null, false, new BoardObject("523", null, null)), false);
		String content = list.get(0).getContent();
		int s = content.indexOf("--\n");
		content = content.substring(0, s);
		
		int e = s = 0;
		StringBuffer sb = new StringBuffer();
		while ((e = content.indexOf("\n", s)) != -1) {
			String c = content.substring(s, e);
			s = e + 1;
			
			if (c.length() != 0) {
				if (c.startsWith("https://bbs.fudan") || c.startsWith("http://bbs.fudan")) {
					if (sb.length() != 0) {
						System.out.print("醒目"+sb.length()+"：");
						System.out.println(sb);
						sb.setLength(0);
					}
					System.out.print("图片"+c.length()+"：");
					System.out.println(c);
				} else {
					sb.append((c + "\n").trim());
				}
			} else {
				if (sb.length() != 0) {
					System.out.print("醒目"+sb.length()+"：");
					System.out.println(sb);
					sb.setLength(0);
				}
			}
		}
		
		
		
		System.out.println("\n-----------------\n");
		String[] ct = content.split("\\n\\n");
		for (String c : ct) {
			System.out.print("醒目"+c.length()+"：");
			System.out.println(c);
		}
	}
}
