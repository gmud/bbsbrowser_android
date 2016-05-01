package leeon.mobile.BBSBrowser.actions;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import leeon.mobile.BBSBrowser.models.MailObject;
import leeon.mobile.BBSBrowser.models.PathObject;
import leeon.mobile.BBSBrowser.utils.HTMLUtil;
import leeon.mobile.BBSBrowser.utils.HTTPUtil;
import leeon.mobile.BBSBrowser.utils.XmlOperator;

import org.apache.http.HttpEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 解析http返回结果的xml或者html的帮助类
 */
public class BBSBodyParseHelper {
	
	/*
	 * 默认的编码
	 */
	public static String BBS_CHARSET = "GB2312";
	
	
	/**
	 * 用于转义错误信息的map
	 */
	public static final Map<String, String> errorMsg = new HashMap<String, String>();
	static {
		if (Charset.isSupported("GB18030")) BBS_CHARSET = "GB18030";
		errorMsg.put("找不到指定的用户", "还没注册过吧?");
		errorMsg.put("用户名和密码不匹配", "忘记密码了？");
		errorMsg.put("您不能登录更多帐号了", "登录太多了,只能两个!");
		errorMsg.put("请先登录", "快登录去，否则不给用!");
		errorMsg.put("此文不可回复，或您没有发文权限", "别灌水了，快被封了!");
		errorMsg.put("内部错误", "BBS告诉我内部错误，我也只能告诉你内部错误，找站长去!");
		errorMsg.put("参数错误", "你输错东西了，没有这个版面，没有这个用户，什么都有可能!");
		errorMsg.put("找不到指定的文件", "不要再往上了，没有了");
		errorMsg.put("413 Request Entity Too Large", "上传图片给太大了!");
	}

	/**
	 * 解析错误的信息页面
	 */
	public static String parseFailMsg(String html) {
		//<html><head><title>发生错误</title></head><body><div>找不到指定的用户</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>发生错误</title></head><body><div>用户名和密码不匹配</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>发生错误</title></head><body><div>您不能登录更多帐号了</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>发生错误</title></head><body><div>请先<a href='login'>登录</a></div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>发生错误</title></head><body><div>此文不可回复，或您没有发文权限</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>发生错误</title></head><body><div>内部错误</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>发生错误</title></head><body><div>找不到指定的文件</div><a href=javascript:history.go(-1)>快速返回</a></body></html>
		//<html><head><title>413 Request Entity Too Large</title></head><body bgcolor="white"><center><h1>413 Request Entity Too Large</h1></center><hr><center>nginx</center></body></html>
		String re = HTMLUtil.findStr(html, "<div>", "</div>");
		if (re == null || re.length() == 0 ) re = HTMLUtil.findStr(html, "<title>", "</title>");
		if (!errorMsg.containsKey(re))
			return re;
		else
			return errorMsg.get(re);
	}
	
	/**
	 * 解析错误的信息页面
	 */	
	public static String parseFailMsg(HttpEntity entity) {
		return parseFailMsg(HTTPUtil.getHttpBody(entity, BBS_CHARSET));
	}

	/**
	 * 解析我的收藏
	 * @param doc
	 */
	public static List<String> parseMyBoardList(Document doc) {
		//<session m='t'><p>lt  </p><u>leeon</u><f><b>Auto</b><b>Heart</b><b>Android</b></f></session>
		NodeList list = doc.getElementsByTagName("session");
		Element element = (Element)list.item(0);
		return XmlOperator.getTextValuesByTagName(element, "b");
	}
	
	/**
	 * 解析我的收藏
	 * @param doc
	 */
	public static List<BoardObject> parseFavBoardList(Document doc) {
		//<brd bid='71' brd='Cartoon'>卡通</brd><brd bid='73' brd='Food'>美食 </brd><brd bid='67' brd='Joke'>嘻嘻哈哈 </brd>
		
		NodeList list = doc.getElementsByTagName("brd");
		List<BoardObject> ret = new ArrayList<BoardObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementBoard = (Element)list.item(i);
			
			String id = elementBoard.getAttribute("bid");
			String name = elementBoard.getAttribute("brd");
			String ch = XmlOperator.getTextValue(elementBoard);
			
			BoardObject board = new BoardObject(id, name, ch);
			ret.add(board);
		}
		return ret;
	}
	
	/**
	 * 解析讨论区
	 */
	public static List<BlockObject> parseBlockList(Document doc) {
		//<sec id='0' desc='BBS 系统 [站内]'></sec>
		NodeList list = doc.getElementsByTagName("sec");
		List<BlockObject> ret = new ArrayList<BlockObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementBlock = (Element)list.item(i);
			
			String id = elementBlock.getAttribute("id");
			String name = elementBlock.getAttribute("desc");
			BlockObject block = new BlockObject(id, name); 
			parseRecommendBoardList(elementBlock, block);
			
			ret.add(block);
		}		
		return ret;
	}
	
	/**
	 * 解析各讨论区的推荐版面
	 */
	private static void parseRecommendBoardList(Element elementBlock, BlockObject block) {
		//<brd name='SysOp' desc='站长工作室' />
		NodeList list = elementBlock.getElementsByTagName("brd");
		List<BoardObject> ret = new ArrayList<BoardObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementBoard = (Element)list.item(i);
			
			String name = elementBoard.getAttribute("name");
			String ch = elementBoard.getAttribute("desc");
			BoardObject board = new BoardObject(name, ch);
			
			ret.add(board);
		}
		block.setRecommendBoardList(ret);
	}
	
	/**
	 * 解析个讨论区的所有版面
	 */
	public static void parseBlockBoardList(Document doc, BlockObject block) {
		parseBoardList(doc, block, null);
	}
	
	/**
	 * 解析父版面的子版面 
	 */
	public static void parseChildBoardList(Document doc, BoardObject parentBoard) {
		parseBoardList(doc, null, parentBoard);
	}
	
	/**
	 * 解析版面对象 
	 */
	private static List<BoardObject> parseBoardList(Document doc, BlockObject block, BoardObject parentBoard) {
		//<brd dir='0' title='FDU_C.D.E.' cate='[院系]' desc='网络教育学院 ' bm='springage' read='1' count='3183' />
		//<brd dir='0' title='FDU_C.S.' cate='[院系]' desc='计算机科学技术学院' bm='anbecial AirCarrier' read='1' count='4357' />
		//<brd title='CS_Graduate' desc='计算机研究生' bm='Tux' total='4190' start='4171' bid='393' page='20' link='' />
		NodeList list = doc.getElementsByTagName("brd");
		List<BoardObject> ret = new ArrayList<BoardObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementBoard = (Element)list.item(i);
			
			String id = elementBoard.getAttribute("bid");
			String name = elementBoard.getAttribute("title");
			String ch = elementBoard.getAttribute("desc");
			String type = elementBoard.getAttribute("cate");
			String master = elementBoard.getAttribute("bm");
			String docNumber = elementBoard.getAttribute("count");
			String total = elementBoard.getAttribute("total");
			boolean dir = "1".equals(elementBoard.getAttribute("dir"));
			BoardObject board = new BoardObject(id, name, type, ch, master, docNumber, dir, (total!=null&&total.length()!=0?Integer.parseInt(total):0));
			
			ret.add(board);
		}
		
		if (block != null)
			block.setAllBoardList(ret);
		else if (parentBoard != null)
			parentBoard.setChildBoardList(ret);
		return ret; 
	}
	
	/**
	 * 解析十大 
	 */
	public static List<DocObject> parseTopTenList(Document doc) {
		//<top board='Employees' owner='dovefly' count='282' gid='4329167'>求问复旦的MBA要考试嘛~~~&amp;&amp;&amp;&amp;&amp;对学历有要求吗</top>
		//<top board='DongBei' owner='niko' count='268' gid='222617'>问候下大家</top>
		//<top board='Employees' owner='IcedFire' count='238' gid='4331911'>大家的羽绒服都什么颜色的？</top>
		NodeList list = doc.getElementsByTagName("top");

		List<DocObject> ret = new ArrayList<DocObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementDoc = (Element)list.item(i);
			
			String id = elementDoc.getAttribute("gid");
			String title = XmlOperator.getTextValue(elementDoc);
			String author = elementDoc.getAttribute("owner");
			String boardName = elementDoc.getAttribute("board");
			String docNumber = elementDoc.getAttribute("count");
			
			DocObject d = new DocObject(id, author, title, docNumber, boardName);
			d.setGid(d.getId());
			ret.add(d);
		}
		
		return ret;
	}
	
	/**
	 * 解析版面文档列表 
	 */
	public static List<DocObject> parseDocList(Document doc, BoardObject board) {
		//<po m='+' owner='yaong' time= '2010-11-29T14:41:36' id='70285'>Re: 找考博的战友</po>
		//<po m='+' owner='Lunder' time= '2010-11-25T11:19:32' id='70272'>“巴黎高科博士项目”复旦大学宣讲会</po>
		//<po sticky='1' m='b' owner='CSGSU' time= '2010-11-09T17:49:35' id='70226'>[转载]复旦大学博士生学术论坛之计算机科学技术篇盛大开幕</po>
		//<po m='+' owner='leeon' time= '2011-01-26T19:54:55' id='3085074438'>Re: 发一个我用的bbs浏览器的源码</po>

		List<BoardObject> boardList = parseBoardList(doc, null, null);
		if (boardList.size() > 0) {
			board.setId(boardList.get(0).getId());
			board.setName(boardList.get(0).getName());
			board.setCh(boardList.get(0).getCh());
			board.setMaster(boardList.get(0).getMaster());
			board.setDocNumber(boardList.get(0).getDocNumber());
			board.setTotal(boardList.get(0).getTotal());
		}
		
		NodeList list = doc.getElementsByTagName("po");
		List<DocObject> ret = new ArrayList<DocObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementDoc = (Element)list.item(i);
			
			String id = elementDoc.getAttribute("id");
			String author = elementDoc.getAttribute("owner");
			String date = elementDoc.getAttribute("time");
			String title = XmlOperator.getTextValue(elementDoc);
			String status = elementDoc.getAttribute("m");
			boolean sticky = "1".equals(elementDoc.getAttribute("sticky"));
			DocObject d = new DocObject(id, status, author, 
					date!=null?date.replace('T', ' '):null, title, sticky, board);
			
			ret.add(d);
		}	
		return ret;
	}
	
	/**
	 * 解析每一个文档内容或者主题模式下的主题的内容 
	 */
	public static List<DocObject> parseContentList(Document doc, DocObject docObject) {
//		<bbstcon bid='110' gid='4391037' page='20'>
//		<po fid='1166670' sticky='1'>
//		<po fid='1174997'>
//		<po fid='1174999' reid='1174935' gid='1174795'>
//		<po fid='70286' owner='computercumt'>发信人: computercumt (lqlself), 信区: CS_Graduate
//		标  题: 复旦计算机研究生复试笔试多吗？
//		发信站: 日月光华 (2010年11月30日14:32:48 星期二)
//
//		 
//		我在复旦研究生网站上看到计算机复试中有很多门课都需要笔试，是真的吗？
//		望有经验的大侠能提供点信息啊，万分感激。
//		--
//
//		>1b[m>1b[1;32m※ 来源:·日月光华 bbs.fudan.edu.cn·HTTP [FROM: 121.233.57.*]>1b[m
//		</po>
//		</bbstcon>
		
		//获取bbstcon或者bbscon的节点，获取bid
		boolean tcon = isTConAndFindBid(doc, docObject);
		
		NodeList list = doc.getElementsByTagName("po");
		List<DocObject> ret = new ArrayList<DocObject>();
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementDoc = (Element)list.item(i);
			
			String id = elementDoc.getAttribute("fid");
			String gid = elementDoc.getAttribute("gid");
			String rid = elementDoc.getAttribute("reid");
			boolean sticky = "1".equals(elementDoc.getAttribute("sticky"));
			
			String content = XmlOperator.getTextValue(elementDoc);
			String author = HTMLUtil.findStr(content, "发信人: ", ", 信区:");
			String title = HTMLUtil.findStr(content, "标  题: ", "\n");
			String date = HTMLUtil.findStr(content, "发信站: 日月光华 (", " 星期");
			if (date == null) date = HTMLUtil.findStr(content, "发信站: 日月光华自动发信系统 (", " 星期");
			content = content.substring(content.indexOf('\n', content.indexOf("发信站"))+1);
			content = content.replace("·日月光华 bbs.fudan.edu.cn·", "");
			if (HttpConfig.SH_NO_EDU) content = content.replace("https://bbs.fudan.edu.cn/upload/", "https://bbs.fudan.sh.cn/upload/");
			
			DocObject d = new DocObject(id, author, date, title, content, docObject.getBoard());
			d.setSticky(sticky);
			if (gid != null && gid.length() != 0) d.setGid(gid);
			if (rid != null && rid.length() != 0) d.setRid(rid);
			if (!tcon && (gid == null || gid.length() == 0)) d.setGid(d.getId());
			ret.add(d);
			//if (i == 0) docObject.setContent(d.getContent()); 
		}
		return ret;
	}
	
	/**
	 * 解析每一个文档内容或者主题模式下的主题的内容 
	 * 用于GAE的解析器
	 * content为new=1的值，content1为old的值
	 */
	public static List<DocObject> parseContentListNewForGAE(Document docOld, Document docNew, DocObject docObject) {
//		<bbstcon bid='110' gid='4391037' page='20'>
//		<po fid='1166670' sticky='1'>
//		<po fid='1174997'>
//		<po fid='1174999' reid='1174935' gid='1174795'>
//		<po fid='70286' owner='computercumt'>发信人: computercumt (lqlself), 信区: CS_Graduate
//		标  题: 复旦计算机研究生复试笔试多吗？
//		发信站: 日月光华 (2010年11月30日14:32:48 星期二)
//
//		 
//		我在复旦研究生网站上看到计算机复试中有很多门课都需要笔试，是真的吗？
//		望有经验的大侠能提供点信息啊，万分感激。
//		--
//
//		>1b[m>1b[1;32m※ 来源:·日月光华 bbs.fudan.edu.cn·HTTP [FROM: 121.233.57.*]>1b[m
//		</po>
//		</bbstcon>
//		<po fid='1212955' reid='1212854' gid='1212854'>
//			<owner>lovun</owner>
//			<nick>聆思尔爱0422</nick>
//			<board>PIC</board>
//			<title>Re:&#160;步步惊心-刘雨欣疯照</title>
//			<date>2011年12月06日09:58:56 星期二</date>
//			<pa m='t'><p>还不如直接脱掉来得劲爆</p><p><br/></p><p><a i='i' href='http://bbs.fudan.edu.cn/upload/Animal/1317883430-9240.jpg'/></p></pa>
//			<pa m='q'><p>【&#160;在&#160;QII&#160;的大作中提到:&#160;】</p><p>:&#160;&#160;</p></pa>
//			<pa m='s'>
//				<p>--</p>
//				<p>&#160;&#160;</p>
//				<p><c h='0' f='37' b='40'></c><c h='1' f='35' b='40'>※&#160;来源:·日月光华&#160;bbs.fudan.edu.cn·HTTP&#160;[FROM:&#160;61.186.153.*]</c><c h='0' f='37' b='40'></c></p>
//			</pa>
//		</po>
		
		boolean tcon = isTConAndFindBid(docNew, docObject);
		NodeList listNew = docNew.getElementsByTagName("po");
		NodeList listOld = docOld.getElementsByTagName("po");
		int length = Math.min(listNew.getLength(), listOld.getLength());
		
		List<DocObject> ret = new ArrayList<DocObject>();
		for (int i = 0; i < length; i ++) {
			Element elementDoc = (Element)listNew.item(i);
			
			String id = elementDoc.getAttribute("fid");
			String gid = elementDoc.getAttribute("gid");
			String rid = elementDoc.getAttribute("reid");
			boolean sticky = "1".equals(elementDoc.getAttribute("sticky"));
			
			StringBuffer content = new StringBuffer();
			XmlOperator.innerXml(elementDoc.getChildNodes(), content);
			
			String content1 = XmlOperator.getTextValue((Element)listOld.item(i));
			
			String author = XmlOperator.getTextValueByTagName(elementDoc, "owner");
			String title = XmlOperator.getTextValueByTagName(elementDoc, "title");
			String date = XmlOperator.getTextValueByTagName(elementDoc, "date");
			
			DocObject d = new DocObject(id, author, date, 
					XmlOperator.replaceChar(title).replace(' ', ' '), 
					content.toString().replace(" ", "&#160;"), 
					docObject.getBoard());
			d.setSticky(sticky);
			d.setContent1(XmlOperator.replaceChar(content1));
			
			if (gid != null && gid.length() != 0) d.setGid(gid);
			if (rid != null && rid.length() != 0) d.setRid(rid);
			if (!tcon && (gid == null || gid.length() == 0)) d.setGid(d.getId());
			ret.add(d);
			//if (i == 0) docObject.setContent(d.getContent()); 
		}
		return ret;
	}
	
	private static Node getRootFromDocument(Document doc) {
		Node root = doc.getFirstChild();
		while (root != null && root.getNodeType() != Node.ELEMENT_NODE)
			root = root.getNextSibling();
		return root;
	}
	
	private static boolean isTConAndFindBid(Document doc, DocObject docObject) {
		//获取bbstcon或者bbscon的节点，获取bid
		boolean tcon = true;
		Node root = getRootFromDocument(doc);
		if (root != null && root.getNodeType() == Node.ELEMENT_NODE && 
			("bbstcon".equals(((Element)root).getTagName()) || "bbscon".equals(((Element)root).getTagName()))) {
				String bid = ((Element)root).getAttribute("bid");
				if (bid != null && docObject.getBoard().getId() == null) docObject.getBoard().setId(bid);
				tcon = "bbstcon".equals(((Element)root).getTagName());
		}
		return tcon;
	}
	
	/**
	 * 解析邮件列表 
	 * 包括所有邮件和新邮件
	 */
	public static List<MailObject> parseMailList(Document doc, boolean isNew) {
		//<bbsmail start='13' total='211' page='20'>
		//<mail m='m' from='qzy (鬼塚英吉~~一个人的日子也很精彩)' date='2000-06-04T11:47:36' name='M.960090456.A'>[公告] 封 leeon 用户 FDU_Materials 板的发表文章权限</mail>
		//<mail from='leeon' date='2010-12-24T16:09:55' name='M.1293178195.A' n='211'>没主题</mail>
		int start = 0;
		if (!isNew) {//不是新邮件列表，解析邮件总数和本页开始数，用于翻页
			Element elementRoot = (Element)(doc.getElementsByTagName("bbsmail").item(0));
			start = Integer.parseInt(elementRoot.getAttribute("start"));
			BBSMailAction.totalMailCount = Integer.parseInt(elementRoot.getAttribute("total"));
		}
		
		NodeList list = doc.getElementsByTagName("mail");
		List<MailObject> ret = new ArrayList<MailObject>();
		int l = list.getLength();
		for (int i = 0; i < l; i ++) {
			Element elementDoc = (Element)list.item(i);
			
			String id = elementDoc.getAttribute("name");
			String title = XmlOperator.getTextValue(elementDoc);
			String sender = elementDoc.getAttribute("from");
			String status = elementDoc.getAttribute("m");
			String date = elementDoc.getAttribute("date");
			String number = elementDoc.getAttribute("n");
			
			MailObject mail = new MailObject(
					number==null||number.length()==0?String.valueOf(start+l-i-1):number, 
					status==null?"+":status, sender, 
					date!=null?date.replace('T', ' '):null, title, id);
			ret.add(mail);
		}
		return ret;
	}
	
	/**
	 * 解析邮件的详细内容 
	 */
	public static void parseMailContent(Document doc, MailObject mail) {
//		<bbsmailcon  prev='M.1288229471.A' next='M.1289297307.A'><t>没主题</t><mail f='M.1288323309.A' n='210'>寄信人: peddy (。)
//		标  题: 没主题
//		发信站: 日月光华 (2010年10月29日11:35:50 星期五)
//		来  源: 219.233.226.*
//
//		http://item.taobao.com/item.htm?id=7533745908
//
//
//		你看看这个，我打算买这个，颜色款式怎么样？是我同学的jj做代购的，可能可以再便
//		宜点。。。
//		>1b[32;40m
//		>1b[m>1b[1;32m※ 来源:·日月光华 bbs.fudan.edu.cn·[FROM: 219.233.226.*]>1b[m
//		</mail>
		Element elementRoot = (Element)(doc.getElementsByTagName("bbsmailcon").item(0));
		String content = XmlOperator.getTextValueByTagName(elementRoot, "mail");
		content = content.replace("·日月光华 bbs.fudan.edu.cn·", "");
		mail.setContent(content);
	}
	
	/**
	 * 解析回邮件的内容
	 */
	public static void parseMailReContent(Document doc, MailObject mail) {
//		<bbspstmail  ref='pstmail' recv='peddy'><t>[日租]馨宁短租房 近复旦东区和南区北区 有宽带 50元起(转寄)</t><m>【 在 peddy 的来信中提到: 】
//		: 
//		: 无中介费
//		: 
//		: 梁小姐  手机：135 6485 6282      周先生 电话：139 1787 1000 
//		: 
//		: 短信：13685777684（浙江短信号码，请勿拨打，可以加飞信，飞信自动确认的），
//		: .................（以下省略）</m>
		Element elementRoot = (Element)(doc.getElementsByTagName("bbspstmail").item(0));
		String content = XmlOperator.getTextValueByTagName(elementRoot, "m");
		mail.setReContent(content);
	}
	
	/**
	 * 解析回复文档时系统提供的RE文信息 
	 */
	public static DocObject parsePostContent(Document doc, BoardObject board) {
//		<bbspst brd='Android' bid='506' edit='0' att='0' anony='0'>
//		<t>Re: [转载]谷歌回应Android短信缺陷 称即将发布修复补丁</t>
//		<po f='3085074145'>【 在 Android 的大作中提到: 】
//		: 用API的呀，还会去读取你短信混合在一起，看得就难受。。。
//		: 我想要的是和电脑上那种类似的客户端，不读取sim卡的＠＠
//		: 【 在 gyj 的大作中提到: 】
//		: : minifetion啊
//		: : 
//		</po></bbspst>
		Element elementRoot = (Element)(doc.getElementsByTagName("bbspst").item(0));
		board.setAttach("1".equals(elementRoot.getAttribute("att")));//附件标志
		String title = XmlOperator.getTextValueByTagName(elementRoot, "t");
		String content = XmlOperator.getTextValueByTagName(elementRoot, "po");
		if (title == null)
			return null;
		else
			return new DocObject(title.startsWith("Re: ")?title:"Re: "+title, content, board);
	}
	
	/**
	 * 解析附件上传成功后返回的URL 
	 */
	public static String parseAttachURL(Document doc) {
//		<?xml version="1.0" encoding="gb18030"?>
//		<?xml-stylesheet type="text/xsl" href="../xsl/bbsupload.xsl?v1"?>
//		<bbsupload><size>561278</size><user>leeon</user><url>http://bbs.fudan.edu.cn/upload/PIC/1294905528-0456.jpg</url></bbsupload>
		Element elementRoot = (Element)(doc.getElementsByTagName("bbsupload").item(0));
		return XmlOperator.getTextValueByTagName(elementRoot, "url");
	}
	
	/**
	 * 解析精华区路径
	 */
	public static void parsePathList(Document doc, PathObject parent) {
//		<bbs0an path='/groups/comp.faq/Algorithm' v='74063'  brd='Algorithm'>
//		<ent path='/.index' t='f' id='qianli' time='2006-07-01T02:43:41'>【本板精华区索引】</ent>
		Element elementRoot = (Element)(doc.getElementsByTagName("bbs0an").item(0));
		String parentPath = elementRoot.getAttribute("path");
		parent.setPath(parentPath);
		parent.setFetched(true);
		
		NodeList list = doc.getElementsByTagName("ent");
		for (int i = 0; i < list.getLength(); i ++) {
			Element elementPath = (Element)list.item(i);
			
			String path = elementPath.getAttribute("path");
			String type = elementPath.getAttribute("t");
			String author = elementPath.getAttribute("id");
			String name = XmlOperator.getTextValue(elementPath);
			String time = elementPath.getAttribute("time");
			
			time = time!=null?time.replace('T', ' '):null;
			path = parent.getPath() + path;
			
			if (parent.hasChild(path)) {
				parent.getChild(path).setName(name);
				parent.getChild(path).setType(type);
				parent.getChild(path).setAuthor(author);
				parent.getChild(path).setTime(time);
				parent.reIndexChild(path);
			} else {
				PathObject p = new PathObject(path, author, name, time, type);
				parent.addChild(p);
			}
		}
	}
	
	/**
	 * 解析精华区内容
	 */
	public static String parsePathContent(Document doc, PathObject file) {
//	<po>
//
//	    此处文章为日月光华五周年庆典时举办的站内文章收集活动的结果,
//	均为各版代表性文章.欢迎阅读.
//
//
//	公告:
//	在www的“光华概览”下新增加“特别栏目 -- 光华五周年站庆网上文集”，
//	内有这次Digest版征集的各版精华文章，欢迎大家参观。
//
//	http://bbs.fudan.edu.cn/20010419/default.htm
//
//	在此感谢各位版主的大力协助，并特别感谢aven,chester,clamp,datura,
//	gemmy,jiangnan,kimura,kitora等网友参加制作。
//	</po>
		Element elementRoot = (Element)(doc.getElementsByTagName("bbsanc").item(0));
		String content = XmlOperator.getTextValueByTagName(elementRoot, "po");
		file.setContent(content);
		file.setFetched(true);
		return content;
	}
}
