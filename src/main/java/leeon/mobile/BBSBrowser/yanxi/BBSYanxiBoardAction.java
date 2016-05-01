package leeon.mobile.BBSBrowser.yanxi;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.IBoardAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;

public class BBSYanxiBoardAction implements IBoardAction {
	
	/**
	 * 所有讨论区列表
	 */
	private static List<BlockObject> blockList;

	@Override
	public List<BlockObject> allBlock() throws NetworkException {
		if (blockList != null) return blockList;
		
		blockList = new ArrayList<BlockObject>();
		blockList.add(new BlockObject("0", "燕曦系统"));
		blockList.add(new BlockObject("1", "快乐老家"));
		blockList.add(new BlockObject("2", "个人风采"));
		blockList.add(new BlockObject("3", "文化娱乐"));
		blockList.add(new BlockObject("4", "三岔路口"));
		blockList.add(new BlockObject("5", "校园风采"));
		blockList.add(new BlockObject("6", "合作服务"));
		
		String url = YanxiHttpConfig.BBS_URL + YanxiHttpConfig.BBS_BLOCK;
		String html = BBSYanxiDocAction.getUrlHtmlContent(url);
		
		BBSYanxiBodyParseHelper.parseBlockRecommendBoardList(html, blockList);
		
		return blockList;
	}

	/**
	 * 通过递归获取所有版面
	 */
	@Override
	public List<BoardObject> allBoard() throws NetworkException {
		List<BoardObject> ret = new ArrayList<BoardObject>();
		
		List<BlockObject> bs = allBlock();
		for (BlockObject block : bs) {
			blockBoard(block);
			allBoard(block.getAllBoardList(), ret);
		}
		return ret;
	}
	
	private void allBoard(List<BoardObject> list1, List<BoardObject> ret) {
		for (BoardObject b1 : list1) {
			if (b1.isDir()) {
				allBoard(b1.getChildBoardList(), ret);
			} else {
				ret.add(b1);
			}
		}
	}

	@Override
	public void blockBoard(BlockObject block) throws NetworkException {
		String url = YanxiHttpConfig.BBS_URL + YanxiHttpConfig.BBS_BLOCK_BOARD + block.getId();
		String html = BBSYanxiDocAction.getUrlHtmlContent(url);
		
		BBSYanxiBodyParseHelper.parseBlockBoardList(html, block);
		
		for (BoardObject board : block.getAllBoardList()) {
			if (board.isDir()) dirBoard(board);
		}	
	}

	@Override
	public void dirBoard(BoardObject dirBoard) throws NetworkException {
		String url = YanxiHttpConfig.BBS_URL + YanxiHttpConfig.BBS_SUB_BOARD + dirBoard.getName();
		String html = BBSYanxiDocAction.getUrlHtmlContent(url);
		
		BBSYanxiBodyParseHelper.parseChildBoardList(html, dirBoard);
	}
	
	public static void main(String[] args) throws Exception {
	}

}
