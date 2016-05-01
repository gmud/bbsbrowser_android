package leeon.mobile.BBSBrowser.sjtu;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import leeon.mobile.BBSBrowser.utils.HTMLUtil;
import leeon.mobile.BBSBrowser.utils.HTMLUtil.PatternListener;

public class BBSSjtuBodyParseHelper {
	/*
	 * 默认的编码
	 */
	public static String BBS_CHARSET = "GB2312";
	
	
	/*
	 * 解析版面文章列表
	 */
	public static List<DocObject> parseBoardDoc(BoardObject board, String source, boolean isTitle) {
		List<DocObject> ret = new ArrayList<DocObject>();
		if (source == null) return ret;
		
		String tableSource = HTMLUtil.dealTables(source, isTitle?1:6);
		if (tableSource == null) return ret;
		
		//最大记录数
		int max = 0;
		
		//矩阵数据
		String[] rows = HTMLUtil.dealTableRows(tableSource);
		for (int i = 1; i < rows.length; i ++) {
			String[] cells = HTMLUtil.dealTableCells(rows[i]);
			if (cells.length != 5) continue;
			
			String author = HTMLUtil.removeHtmlTag(cells[2]);
			String status = "+";
			String date = cells[3];
			String title = HTMLUtil.removeHtmlTag(cells[4]);
			boolean sticky = (cells[0].indexOf('<')!=-1);
			String id = sticky?HTMLUtil.findStrBeforeTag(cells[4], ">", "=", ">"):
				HTMLUtil.findStrBeforeTag(cells[4], ">", ",", ".html>");
			if (!sticky) {
				max = Integer.parseInt(cells[0].trim());
			}
			
			DocObject d = new DocObject(id, status, author, date, title, sticky, board);
			ret.add(d);
		}
		//总页数,没有提取过，再进行提取
		if (board.getTotal() == 0) {
			board.setTotal(max);
		}
		
		return ret;
	}
	
	/*
	 * 解析文章详细
	 */
	public static List<DocObject> parseDocContent(DocObject d, String source, boolean isTitle) {
		if (isTitle) return parseTitleContent(d, source);
		else return parseContent(d, source);
	}
	
	private static List<DocObject> parseTitleContent(DocObject d, String source) {
		List<DocObject> ret = new ArrayList<DocObject>();
		if (source == null) return ret;
		
		String[] contents = HTMLUtil.findStrs(source, "<pre>", "</pre>");
		for (String content : contents) {
			//公共属性处理
			DocObject doc = buildContent(content, d.getBoard());
			
			//处理id
			doc.setId(HTMLUtil.findStrBeforeTag(content, "回复本文</a>", "=", "'"));
			
			ret.add(doc);
		}		
		return ret;
	}
	
	private static List<DocObject> parseContent(DocObject d, String source) {
		List<DocObject> ret = new ArrayList<DocObject>();
		if (source == null) return ret;
		
		//公共属性处理
		String content = HTMLUtil.findStr(source, "<pre>", "</pre>");
		DocObject doc = buildContent(content, d.getBoard());
		
		//处理id和gid
		doc.setId(d.getId());
		doc.setGid(HTMLUtil.findStrBeforeTag(source, "同主题阅读</a>", ",", "."));
		ret.add(doc);
		
		return ret;
	}
	
	private static DocObject buildContent(String content, BoardObject board) {
		String content1 = content;
		
		//处理作者，日期，标题
		content = HTMLUtil.removeHtmlTag(content);
		String author = HTMLUtil.findStr(content, "发信人: ", ", 信区:");
		String title = HTMLUtil.findStr(content, "标  题: ", "\n");
		String date = HTMLUtil.findStr(content, "发信站: 饮水思源 (", " 星期");
		if (date == null) date = HTMLUtil.findStr(content, "发信站: 饮水思源自动发信系统 (", " 星期");
		
		//处理内容		
		content = content.substring(content.indexOf('\n', content.indexOf("发信站"))+1);
		content = content.replace("·饮水思源 bbs.sjtu.edu.cn·", "");
		
		//处理图片
		//先替换掉onload中的>号
		content = content.replace("onload=\"if(this.width > screen.width - 200){this.width = screen.width - 200}\"", "");
		content = HTMLUtil.replacePattern(content, "<IMG[^>]*>", new PatternListener(){
			@Override
			public String onPatternMatch(String source) {
				String src = HTMLUtil.findStrRegex(source, "src=[\"']", "[\"']");
				if (src == null) return "";
				else if (src.startsWith("/")) return SjtuHttpConfig.BBS_URL + src;
				else return src;
			}
		});
		
		DocObject ret = new DocObject(null, author, date, title, content, board);
		ret.setContent1(content1);
		
		return ret;
	}

	/**
	<table width="100%" cellpadding="2" cellspacing="0" bgcolor="#DDDDDD">
	<tr valign="top">  
	<td width=114px>&nbsp;３　<strong><a class=bd href="/bbsboa?sec=3">电脑技术</a></strong> 　
	</td>
	<td align=left>|<a
		   href="/bbsdoc?board=ITCareer"
		   target="_self">IT职场</a>|<a
		   href="/bbsdoc?board=C"
		   target="_self">C/C++</a>|<a
		   href="/bbsdoc?board=GNULinux"
		   target="_self">自由软件和Linux</a>|<a
		   href="/bbsdoc?board=DotNet"
		   target="_self">.Net技术</a>|<a
		   href="/bbsdoc?board=Java"
		   target="_self">Java语言</a>|<a
		   href="/bbsdoc?board=SingleChip"
		   target="_self">单片机</a>|<a
		   href="/bbsdoc?board=WebDevelop"
		   target="_self">Web开发</a>|<a
		   href="/bbsdoc?board=ITNews"
		   target="_self">IT资讯</a>|<a
		   href="/bbsdoc?board=network"
		   target="_self">网络互联</a>| </td>
	<td align=right width=48px><a href="/bbsboa?sec=3">更多...</a></td>
	</tr>
	</table> 
	 */
	public static void parseBlockRecommendBoardList(String source, List<BlockObject> blockList) {
		if (blockList == null) return;
		if (source == null) return;
		
		boolean empty = (blockList.size()==0);
		String[] blockHtml = HTMLUtil.findStrs(source, "<table width=\"100%\" cellpadding=\"2\" cellspacing=\"0\" bgcolor=\"#DDDDDD\">", "</table>");
		for (String block : blockHtml) {
			String blockInfo = HTMLUtil.findStr(block, "<strong>", "</strong>");
			String blockId = HTMLUtil.findStr(blockInfo, "bbsboa?sec=", "\">");
			String blockName = HTMLUtil.findStr(blockInfo, "\">", "</a>");
			
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
			
			String[] boardHtml = HTMLUtil.findStrs(block, "bbsdoc?board=", "</a>");
			for (String board : boardHtml) {
				int i1 = board.indexOf("\"");
				int i2 = board.indexOf(">");
				if (i1 == -1 || i2 == -1) continue;
				
				String name = board.substring(0, i1);
				String ch = board.substring(i2+1);
				b.getRecommendBoardList().add(new BoardObject(name, name, ch));
			}
		}
	}
	
	
	/**
	<table width=700>
	<tr bgcolor=e8e8e8><td>序号<td>未<td>讨论区名称<td>更新时间<td>类别<td>中文描述<td>板主<td>文章数
	<tr><td>1<td>＋<td><a href=bbssubboard,name,Admin.html>Admin</a><td>            <td>[站务]<td><a href=bbssubboard,name,Admin.html> ○ 站务管理</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>2<td>＋<td><a href=bbssubboard,name,BBSTech.html>BBSTech</a><td>            <td>[BBS] <td><a href=bbssubboard,name,BBSTech.html> ○ BBS技术</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>3<td>＋<td><a href=bbssubboard,name,Celebration.html>Celebration</a><td>            <td>[聚会]<td><a href=bbssubboard,name,Celebration.html> ○ 水源聚会</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>4<td>＋<td><a href=bbssubboard,name,Claim.html>Claim</a><td>            <td>[法务]<td><a href=bbssubboard,name,Claim.html> ○ 举报与投诉</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>5<td>＋<td><a href=bbssubboard,name,cnBBS.html>cnBBS</a><td>            <td>[转信]<td><a href=bbssubboard,name,cnBBS.html> ○ 转信</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>6<td>＋<td><a href=bbssubboard,name,SJTUBBS.html>SJTUBBS</a><td>            <td>[本站]<td><a href=bbssubboard,name,SJTUBBS.html> ○ 本站</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>7<td>＋<td><a href=bbssubboard,name,System.html>System</a><td>            <td>[系统]<td><a href=bbssubboard,name,System.html> ○ 系统发文</a><td><a href=bbsqry?userid=SYSOP>SYSOP</a><td>-
	<tr><td>8<td>◇<td><a href=bbsdoc,board,sysop.html>sysop</a><td>Dec 15 17:51<td>[站内]<td><a href=bbsdoc,board,sysop.html> ○ 站长的工作室</a><td><a href=bbsqry?userid=诚征板主中>诚征板主中</a><td>7809
	<tr><td>9<td>◇<td><a href=bbsdoc,board,YSSYWiki.html>YSSYWiki</a><td>Dec 15 17:47<td>[本站]<td><a href=bbsdoc,board,YSSYWiki.html> ○ 水源百科</a><td><a href=bbsqry?userid=YSSYWiki>YSSYWiki</a><td>845
	</table>
	 */
	public static void parseBlockBoardList(String source, BlockObject block) {
		List<BoardObject> ret = parseBoardList(source);
		block.setAllBoardList(ret);
	}
	
	/**
	<table width=700>
	<tr bgcolor=e8e8e8><td>序号<td>未<td>讨论区名称<td>更新时间<td>类别<td>中文描述<td>板主<td>文章数
	<tr><td>1<td>◇<td><a href=bbsdoc,board,Advice.html>Advice</a><td>Dec 15 17:46<td>[本站]<td><a href=bbsdoc,board,Advice.html> ○ 共建水源</a><td><a href=bbsqry?userid=melodyolans>melodyolans</a><td>5835
	<tr><td>2<td>◇<td><a href=bbsdoc,board,announce.html>announce</a><td>Dec 15 17:53<td>[本站]<td><a href=bbsdoc,board,announce.html> ○ 站务公告</a><td><a href=bbsqry?userid=诚征板主中>诚征板主中</a><td>12772
	<tr><td>3<td>◇<td><a href=bbsdoc,board,BBSHelp.html>BBSHelp</a><td>Dec 15 17:43<td>[本站]<td><a href=bbsdoc,board,BBSHelp.html> ○ 新手求助</a><td><a href=bbsqry?userid=Ellias>Ellias</a><td>2985
	<tr><td>4<td>◇<td><a href=bbsdoc,board,Bygones.html>Bygones</a><td>Dec 15 17:43<td>[本站]<td><a href=bbsdoc,board,Bygones.html> ○ 流金岁月</a><td><a href=bbsqry?userid=flyeagle>flyeagle</a><td>12494
	<tr><td>5<td>◇<td><a href=bbsdoc,board,Contact.html>Contact</a><td>Dec 15 17:43<td>[本站]<td><a href=bbsdoc,board,Contact.html> ○ 外联宣传</a><td><a href=bbsqry?userid=wk>wk</a><td>2354
	<tr><td>6<td>◇<td><a href=bbsdoc,board,Digest.html>Digest</a><td>Dec 15 17:44<td>[本站]<td><a href=bbsdoc,board,Digest.html> ○ 水源精华文集</a><td><a href=bbsqry?userid=Missing>Missing</a><td>4309
	<tr><td>7<td>◇<td><a href=bbsdoc,board,FundForBBS.html>FundForBBS</a><td>Dec 15 17:45<td>[本站]<td><a href=bbsdoc,board,FundForBBS.html> ○ 饮水思源基金</a><td><a href=bbsqry?userid=FundForBBS>FundForBBS</a><td>4179
	<tr><td>8<td>◇<td><a href=bbsdoc,board,Rules.html>Rules</a><td>Dec 15 17:30<td>[站务]<td><a href=bbsdoc,board,Rules.html> ○ 水源站规修订</a><td><a href=bbsqry?userid=诚征板主中>诚征板主中</a><td>1429
	<tr><td>9<td>◇<td><a href=bbsdoc,board,Souvenir.html>Souvenir</a><td>Dec 15 17:30<td>[本站]<td><a href=bbsdoc,board,Souvenir.html> ○ 水源纪念品</a><td><a href=bbsqry?userid=starseeker>starseeker</a><td>502
	<tr><td>10<td>◇<td><a href=bbsdoc,board,test.html>test</a><td>Dec 15 17:46<td>[本站]<td><a href=bbsdoc,board,test.html> ● 请在此测试</a><td><a href=bbsqry?userid=soso>soso</a><td>606
	</table>
	**/
	public static void parseChildBoardList(String source, BoardObject dirBoard) {
		List<BoardObject> ret = parseBoardList(source);
		dirBoard.setChildBoardList(ret);
	}
	/**
	<tr><td>序号<td>讨论区名称<td>类别<td>中文描述<td>板主
	<tr><td>1<td><a href=bbsdoc,board,Accounting.html>Accounting</a><td>[科学]<td><a href=bbsdoc,board,Accounting.html> ○ 会计</a><td><a href="bbsqry?userid=sonichen">sonichen</a></a>
	**/
	public static List<BoardObject> parseAllBoardList(String source) {
		List<BoardObject> ret = new ArrayList<BoardObject>();
		if (source == null) return ret;
		
		String tableSource = HTMLUtil.dealTables(source, 1);
		if (tableSource == null) return ret;
		
		//矩阵数据
		String[] rows = HTMLUtil.dealTableRows(tableSource);
		for (int i = 1; i < rows.length; i ++) {
			String[] cells = HTMLUtil.dealTableCells(rows[i]);
			if (cells.length != 5) continue;
			
			String id = HTMLUtil.removeHtmlTag(cells[1]); 
			String name = id;
			String type = cells[2];
			String ch = HTMLUtil.removeHtmlTag(cells[3]);
			String master = HTMLUtil.removeHtmlTag(cells[4]);
			BoardObject b = new BoardObject(id, name, type, ch, master, null, false);
			
			ret.add(b);
		}
		return ret;
	}
	
	/**
	 * 解析版面html
	 */
	private static List<BoardObject> parseBoardList(String source) {
		List<BoardObject> ret = new ArrayList<BoardObject>();
		if (source == null) return ret;
		
		String tableSource = HTMLUtil.dealTables(source, 1);
		if (tableSource == null) return ret;
		
		//矩阵数据
		String[] rows = HTMLUtil.dealTableRows(tableSource);
		for (int i = 1; i < rows.length; i ++) {
			String[] cells = HTMLUtil.dealTableCells(rows[i]);
			if (cells.length != 8) continue;
			
			String id = HTMLUtil.removeHtmlTag(cells[2]); 
			String name = id;
			String type = cells[4];
			String ch = HTMLUtil.removeHtmlTag(cells[5]);
			String master = HTMLUtil.removeHtmlTag(cells[6]);
			String docNumber = "-".equals(cells[7])?null:cells[7];
			boolean dir = "-".equals(cells[7]);
			BoardObject b = new BoardObject(id, name, type, ch, master, docNumber, dir);
			
			ret.add(b);
		}
		return ret;
	}


}
