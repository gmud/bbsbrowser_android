package leeon.mobile.BBSBrowser.sjtu;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.IBoardAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.utils.HTMLUtil;

public class BBSSjtuBoardAction implements IBoardAction {
	
	/**
	 * 所有讨论区列表
	 */
	private static List<BlockObject> blockList;

	@Override
	public List<BlockObject> allBlock() throws NetworkException {
		if (blockList != null) return blockList;
		
		blockList = new ArrayList<BlockObject>();
		blockList.add(new BlockObject("0", "BBS 系统"));
		blockList.add(new BlockObject("1", "上海交大"));
		blockList.add(new BlockObject("2", "学子院校"));
		blockList.add(new BlockObject("3", "电脑技术"));
		blockList.add(new BlockObject("4", "学术科学"));
		blockList.add(new BlockObject("5", "艺术文化"));
		blockList.add(new BlockObject("6", "体育运动"));
		blockList.add(new BlockObject("7", "休闲娱乐"));
		blockList.add(new BlockObject("8", "知性感性"));
		blockList.add(new BlockObject("9", "社会信息"));
		blockList.add(new BlockObject("10", "社团群体"));
		blockList.add(new BlockObject("11", "游戏专区"));
		
		String url = SjtuHttpConfig.BBS_URL + SjtuHttpConfig.BBS_BLOCK;
		String html = BBSSjtuDocAction.getUrlHtmlContent(url);
		
		BBSSjtuBodyParseHelper.parseBlockRecommendBoardList(html, blockList);
		
		return blockList;
	}
	
	/**
	 * 获取所有版面
	 */
	public List<BoardObject> allBoard() throws NetworkException {
		String url = SjtuHttpConfig.BBS_URL + SjtuHttpConfig.BBS_ALL_BOARD;
		String html = BBSSjtuDocAction.getUrlHtmlContent(url);
		
		return BBSSjtuBodyParseHelper.parseAllBoardList(html);
	}

	@Override
	public void blockBoard(BlockObject block) throws NetworkException {
		String url = SjtuHttpConfig.BBS_URL + SjtuHttpConfig.BBS_BLOCK_BOARD + block.getId();
		String html = BBSSjtuDocAction.getUrlHtmlContent(url);
		
		BBSSjtuBodyParseHelper.parseBlockBoardList(html, block);
		
		for (BoardObject board : block.getAllBoardList()) {
			if (board.isDir()) dirBoard(board);
		}
	}

	@Override
	public void dirBoard(BoardObject dirBoard) throws NetworkException {
		String url = SjtuHttpConfig.BBS_URL + SjtuHttpConfig.BBS_SUB_BOARD + dirBoard.getName();
		String html = BBSSjtuDocAction.getUrlHtmlContent(url);
		
		BBSSjtuBodyParseHelper.parseChildBoardList(html, dirBoard);
	}
	
	public static void main(String[] args) throws Exception {
		String html = BBSSjtuDocAction.getUrlHtmlContent("https://bbs.sjtu.edu.cn/php/bbsindex.html");
		//HTMLUtil.dealEnclosingTags(html, "table", "width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"");
		HTMLUtil.dealHrefs(html);
	}

}
