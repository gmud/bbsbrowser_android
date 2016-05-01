package leeon.mobile.BBSBrowser.yanxi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import leeon.mobile.BBSBrowser.utils.HTMLUtil;

public class BBSYanxiBodyParseHelper {
	/*
	 * 默认的编码
	 */
	public static String BBS_CHARSET = "GB2312";
	
	static String dateToString(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		return sdf.format(new Date(Long.parseLong(date)*1000));
	}
	
	/**
	<script type="text/javascript"><!--
	var c = new docWriter('Sex',405,30876,0,0,1545,30895,'/groups/GROUP_4/Sex',0,1);
	c.o(132221,132220,'chesspot','  ',1323782385,'Re: 跳蛋好像流行起来了？ ',16,0,0);
	c.o(132222,132220,'Anonymous','; ',1323782556,'Re: 跳蛋好像流行起来了？ ',8,0,0);
	c.o(132223,132198,'Anonymous','  ',1323830670,'Re: 版上的姐妹们有多少愿意女上位的？ ',122,0,0);
	c.o(132224,132220,'Anonymous','  ',1323862282,'Re: 跳蛋好像流行起来了？ ',50,0,0);
	c.o(132225,132207,'Anonymous','  ',1323862506,'Re: 被发现了，怎么办！ ',6,0,0);
	c.o(132226,131789,'usemjj','  ',1323875780,'Re: 这里男士多，听听男士的意见 ',282,0,0);
	c.o(132227,131789,'tonyhuang','  ',1323879608,'Re: 这里男士多，听听男士的意见 ',4,0,0);
	c.o(132228,131789,'cybeon','  ',1323911913,'Re: 这里男士多，听听男士的意见 ',4,0,0);
	c.o(132229,132220,'Anonymous','  ',1323932617,'Re: 跳蛋好像流行起来了？ ',32,0,0);
	c.o(132230,132230,'wsn','  ',1323964796,'发现自己最近发福以后。理我的姑娘少了好多 ',28,0,0);
	c.o(132231,132162,'Anonymous','  ',1323967507,'Re: 这是啥情况？ ',143,0,0);
	c.o(129138,129098,'Anonymous','d ',1316359651,'Re: 潜水感谢 ',58,0,0);
	c.o(129666,129666,'flysky','d ',1317389028,'yc清单 ',381,0,0);
	c.o(119719,119719,'flysky','d ',1304343582,'债务明细 ',343,0,0);
	c.o(119248,119248,'flysky','d ',1303734536,'版宠及其权利和义务 ',158,0,0);
	c.o(910,910,'SmartQ','d ',1231419435,'请大家珍惜版面 本版讨论内容提示 ',42,0,0);
	c.o(12,12,'SmartQ','d ',1231386726,'燕曦BBS Sex/人之初版版规V1.0 ',6680,0,0);
	c.o(1317,1317,'SmartQ','d ',1231427663,'Sex 版常用缩略语 ',2488,0,0);
	c.o(87,87,'SmartQ','d ',1231388491,'如何设置匿名发帖 ',158,0,0);
	c.o(261,261,'SmartQ','d ',1231395182,'[再次提醒各位注意关于被骚扰] ',62,0,0);
	c.t();c.f('',0,0);
	//-->
	</script>
	 * 解析版面文章列表
	 */
	public static List<DocObject> parseBoardDoc(BoardObject board, String source, boolean isTitle) {
		List<DocObject> ret = new ArrayList<DocObject>();
		if (source == null) return ret;
		
		String tableSource = HTMLUtil.findStr(source, "<script type=\"text/javascript\"><!--\n", "//-->\n</script>");
		if (tableSource == null) return ret;
				
		String[] rows = HTMLUtil.findStrs(tableSource, "c.o(", ");\n");
		for (int i = 0; i < rows.length; i ++) {
			String[] cells = HTMLUtil.splitArguments(rows[i]);
			if (cells.length != 9) continue;
			
			String author = cells[2];
			String status = "+";
			String date = dateToString(cells[4]);
			String title = cells[5].trim();
			boolean sticky = (cells[3].indexOf('d')!=-1);
			String id = cells[0];
			String gid = cells[1];
			
			DocObject d = new DocObject(id, status, author, date, title, sticky, board);
			d.setGid(gid);
			ret.add(d);
		}
		//总页数,没有提取过，再进行提取
		if (board.getTotal() == 0) {
			String boardInfo = HTMLUtil.findStr(tableSource, "var c = new docWriter(", ");\n");
			String[] ag = HTMLUtil.splitArguments(boardInfo);
			if (ag.length == 10) {
				board.setId(ag[1]);
				board.setTotal(Integer.parseInt(ag[6]));
			}
		}
		
		return ret;
	}
	
	/**
	<script type="text/javascript"><!--
	prints('发信人: Anonymous (我爱燕曦!), 信区: Sex\n标  题: Re: 版上的姐妹们有多少愿意女上位的？\n发信站: 燕曦BBS (2011年12月14日10:47:37 星期三), 站内\n\n我也说不清，也可能因为男上动作都习惯了，很快直奔主题就射了。女上的动作规律尚\n未找到，感到有些神秘，于是就有了期待中的坚持？\n\n\n【 在 Anonymous (我爱燕曦!) 的大作中提到: 】\n: 女上能延时？没听说过\n: 【 在 Anonymous (我爱燕曦!) 的大作中提到: 】\n: : 我短小而软，又擅短跑。新交的MM每次床战的下半场，都是女上，看她很陶醉的样子，\n: : 又延时，又增强我信心，又爽，我觉得该娶她进门才对得起自己的身心\n\n\n--\n\n\r[m\r[1;33m※ 来源:·燕曦BBS bbs.yanxi.org·[FROM: 匿名天使的家]\r[m\n');o.h(0);o.t();
	//-->
	</script>
	 * 解析文章详细
	 */
	public static DocObject parseDocContent(DocObject d, String source) {
		if (source == null) return d;
		
		String tableSource = HTMLUtil.findStr(source, "<script type=\"text/javascript\"><!--\nprints('", "');o.h(0);o.t();\n//-->\n</script>");
		if (tableSource == null) return d;
		
		return buildContent(tableSource, d);
	}
	
	
	/**
	<script type="text/javascript"><!--
	var o = new tconWriter('Sex',405,132162,132162,2,1,0,132064,132176);
	o.h();
	o.o([[132162,'Anonymous'],[132163,'Anonymous'],[132164,'Anonymous'],[132165,'Anonymous'],[132166,'Anonymous'],[132167,'Anonymous'],[132168,'Anonymous'],[132169,'zongsan'],[132170,'Anonymous'],[132172,'Anonymous'],[132173,'Anonymous'],[132175,'Anonymous'],[132178,'Anonymous'],[132180,'Anonymous'],[132181,'Anonymous'],[132182,'Anonymous'],[132194,'flysky'],[132196,'Anonymous'],[132197,'Anonymous'],[132205,'Anonymous']]);o.h();
	//-->
	</script>
	 * 解析title 文章 列表
	 */
	public static boolean parseTitleContent(DocObject gdoc, String source, List<DocObject> list) {
		if (source == null) return false;
		
		String tableSource = HTMLUtil.findStr(source, "<script type=\"text/javascript\"><!--", "o.h();\n//-->\n</script>");
		if (tableSource == null) return false;
		
		//文章列表
		String docInfo = HTMLUtil.findStr(tableSource, "o.o([", "]);");
		String[] rows = HTMLUtil.findStrs(docInfo, "[", ",");
		for (String row : rows) {
			DocObject d = new DocObject(row, null, null, null, null, gdoc.getBoard());
			d.setGid(gdoc.getId());
			list.add(d);
		}
		
		String boardInfo = HTMLUtil.findStr(tableSource, "var o = new tconWriter(", ");\n");
		String[] ag = HTMLUtil.splitArguments(boardInfo);
		if (ag.length != 9) return false;
		if (ag[4].equals(ag[5])) return false;
		return true;
	}	
	
	private static DocObject buildContent(String content, DocObject doc) {
		String content1 = content;

		//处理\
		content = content.replace("\\n", "\n").replace("\\'", "'").replace("\\/", "/").replace("\\r", ">1b");
		//处理作者，日期，标题
		content = HTMLUtil.removeHtmlTag(content);
		String author = HTMLUtil.findStr(content, "发信人: ", ", 信区:");
		String title = HTMLUtil.findStr(content, "标  题: ", "\n");
		String date = HTMLUtil.findStr(content, "发信站: 燕曦BBS (", " 星期");
		if (date == null) date = HTMLUtil.findStr(content, "发信站: 燕曦BBS自动发信系统 (", " 星期");
		
		//处理内容		
		content = content.substring(content.indexOf('\n', content.indexOf("发信站"))+1);
		content = content.replace("·燕曦BBS bbs.yanxi.org·", "");
		content = content.replace("·燕曦BBS http://bbs.yanxi.org·", "");
		
		//处理图片
		//先替换掉onload中的>号
//		content = content.replace("onload=\"if(this.width > screen.width - 200){this.width = screen.width - 200}\"", "");
//		content = HTMLUtil.replacePattern(content, "<IMG[^>]*>", new PatternListener(){
//			@Override
//			public String onPatternMatch(String source) {
//				String src = HTMLUtil.findStrRegex(source, "src=[\"']", "[\"']");
//				if (src == null) return "";
//				else return YanxiHttpConfig.BBS_URL + src;
//			}
//		});
		
		DocObject ret = new DocObject(doc.getId(), author, date, title, content, doc.getBoard());
		ret.setContent1(content1);
		ret.setGid(doc.getGid());
		
		return ret;
	}

	/**
	 * <dt>
	 * [<a href='bbsboa.php?group=2'>个人天地</a>]&nbsp;&nbsp;
	 * <a href='bbsdoc.php?board=richmond'>彦语南邻</a>,&nbsp;
	 * <a href='bbsdoc.php?board=yupeijie'>于无声处</a>,&nbsp;
	 * <a href='bbsdoc.php?board=seanlee'>香梨天空</a>,&nbsp;
	 * <a href='bbsdoc.php?board=flyingless'>五月的天</a>,&nbsp;
	 * <a href='bbsdoc.php?board=liyiji'>小千世界</a>,&nbsp;
	 * <a href='bbsboa.php?group=2'>更多&gt;&gt;</a>
	 * </dt>
	 */
	public static void parseBlockRecommendBoardList(String source, List<BlockObject> blockList) {
		if (blockList == null) return;
		if (source == null) return;
		
		boolean empty = (blockList.size()==0);
		String[] blockHtml = HTMLUtil.findStrs(source, "<dt>", "</dt>");
		for (String block : blockHtml) {
			String blockId = HTMLUtil.findStr(block, "bbsboa.php?group=", "'>");
			String blockName = HTMLUtil.findStr(block, "'>", "</a>");
			
			if (blockId == null || blockName == null) continue;
			
			BlockObject b = null;
			if (empty) {
				b = new BlockObject(blockId, blockName);
				blockList.add(b);
			} else {
				for (BlockObject bo : blockList) {
					if (blockId.equals(bo.getId())) {b = bo;break;}
				}
			}
			if (b == null) continue;
			
			String[] boardHtml = HTMLUtil.findStrs(block, "<a href='bbsdoc.php?board=", "</a>");
			for (String board : boardHtml) {
				int i = board.indexOf("'>");
				if (i == -1) continue;
				
				String name = board.substring(0, i);
				String ch = board.substring(i+2);
				b.getRecommendBoardList().add(new BoardObject(null, name, ch));
			}
		}
		
	}
	
	/**
	<script type="text/javascript"><!--
	var o = new brdWriter(0, 0);
	o.o(true,1,257,0,'[目录]','RoomC','普通会议室','[会议室]',0,0,0);
	o.o(true,1,243,0,'[目录]','RoomB','版务会议室','[会议室]',0,0,0);
	o.o(true,1,79,0,'[目录]','RoomA','常务会议室','[会议室]',0,0,0);
	o.o(true,1,31,0,'[目录]','Hall.of.Fame','燕曦名人堂','[个人版]',0,0,0);
	o.o(false,1,47,6307,'[燕曦]','Advice','燕曦发展','SmartQ',4493,0,0);
	o.o(false,1,34,2755,'[燕曦]','Announcement','燕曦公告板','SYSOPs',2074,0,0);
	o.o(false,1,30,22633,'[燕曦]','BBS_Help','燕曦社区使用帮助','va now',2568,0,0);
	o.o(false,1,3,13872,'[燕曦]','BBSLists','燕曦数据中心','SYSOPs',11706,0,0);
	o.o(false,1,23,90668,'[燕曦]','BM_Home','版主之家','BMS submarine',2849,0,0);
	o.o(false,1,80,9186,'[燕曦]','Mediate','争议处理中心','Mediate coffan midstxyz Alpenliebe',1764,0,0);
	o.o(false,1,9,1104,'[燕曦]','New_Board','新版面新风采','SYSOPs',843,0,0);
	o.o(false,1,8,5661,'[燕曦]','Notepad','一缸酸甜苦辣水','Notepad',4637,0,0);
	o.o(false,1,129,32984,'[燕曦]','Notice','封禁处罚公告栏','SYSOPs',6557,0,0);
	o.o(false,1,183,941,'[燕曦]','Recommend','精彩文章大家读','SYSOPs',574,0,0);
	o.o(false,1,1,59325,'[燕曦]','SysOp','燕曦系统讨论区','SYSOP',10047,0,0);
	o.o(false,1,21,11010,'[燕曦]','Test','大家来测试','lululala',6751,0,0);
	o.o(false,1,176,3101,'[燕曦]','YanXiFund','燕曦基金','agag',1953,0,0);
	o.o(false,1,99,8314,'[燕曦]','YanXiStory','燕曦故事','netro duidui',3394,0,0);
	o.o(false,1,177,17095,'[燕曦]','YanXiStudio','妆点燕曦','Daviddvd',2839,0,0);
	o.t();
	//-->
	</script>
	 */
	public static void parseBlockBoardList(String source, BlockObject block) {
		List<BoardObject> ret = parseBoardList(source);
		block.setAllBoardList(ret);
	}
	
	/**
	<script type="text/javascript"><!--
	var o = new brdWriter(0, 257);
	o.o(false,1,137,2858,'[会议]','RoomC1','普通会议室1','greattobe lucyguoguo',2755,0,0);
	o.o(false,1,297,23192,'[会议]','RoomC2','普通会议室2','pb ninimi ovian xxxxxx',2753,0,0);
	o.o(false,1,102,12020,'[会议]','RoomC3','普通会议室3','sscy xiaoyubaobao sfox',11887,0,0);
	o.o(false,1,461,14786,'[会议]','RoomC4','普通会议室4','fdssfy yenan',2370,0,0);
	o.o(false,1,300,781,'[会议]','RoomC5','普通会议室5','oicq',229,0,0);
	o.o(false,1,393,11558,'[会议]','RoomC7','普通会议室7','cj keithprayer jediwarrior',5045,0,0);
	o.o(false,1,258,18209,'[会议]','RoomC8','普通会议室8','cokky SSskywalker lxfind',4923,0,0);
	o.o(false,1,426,16231,'[会议]','RoomC9','普通会议室9','lisaguo ARC fangfangfish',3092,0,0);
	o.t();
	//-->
	</script>
	**/
	public static void parseChildBoardList(String source, BoardObject dirBoard) {
		List<BoardObject> ret = parseBoardList(source);
		dirBoard.setChildBoardList(ret);
	}
	
	/**
	 * 解析版面html
	 * o.o(true,1,257,0,'[目录]','RoomC','普通会议室','[会议室]',0,0,0);
	 * o.o(false,1,177,17095,'[燕曦]','YanXiStudio','妆点燕曦','Daviddvd',2839,0,0);
	 */
	private static List<BoardObject> parseBoardList(String source) {
		List<BoardObject> ret = new ArrayList<BoardObject>();
		if (source == null) return ret;
		
		String tableSource = HTMLUtil.findStr(source, "<script type=\"text/javascript\"><!--\n", "//-->\n</script>");
		if (tableSource == null) return ret;
		
		//矩阵数据
		String[] rows = HTMLUtil.findStrs(tableSource, "o.o(", ");\n");
		for (int i = 0; i < rows.length; i ++) {
			String[] cells = HTMLUtil.splitArguments(rows[i]);
			if (cells.length != 11) continue;
			
			String id = HTMLUtil.removeHtmlTag(cells[2]); 
			String name = cells[5];
			String type = cells[4];
			String ch = cells[6];
			String master = cells[7];
			String docNumber = cells[8];
			boolean dir = "true".equals(cells[0]);
			BoardObject b = new BoardObject(id, name, type, ch, master, docNumber, dir);
			
			ret.add(b);
		}
		return ret;
	}


}
