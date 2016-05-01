package leeon.mobile.BBSBrowser.actions;

import java.util.ArrayList;
import java.util.List;

import leeon.mobile.BBSBrowser.IBoardAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.utils.XmlOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;

/**
 * 和版面操作有关的方法
 */
public class BBSBoardAction implements IBoardAction {
	
	/**
	 * 所有讨论区列表
	 */
	private static List<BlockObject> blockList;
	
	/**
	 * 登录后进入的第一个action
	 * 拿到讨论区、推荐版面
	 * @throws NetworkException 
	 */
	public List<BlockObject> allBlock() throws NetworkException {
		if (blockList != null) return blockList;
		
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_BOARD);
		
		blockList = new ArrayList<BlockObject>();		
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
						
			Document doc = XmlOperator.readDocument(entity.getContent());
			blockList = BBSBodyParseHelper.parseBlockList(doc); 
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
		return blockList;
	}
	
	/**
	 * 获取所有版面，没有用到不做实现
	 */
	public List<BoardObject> allBoard() throws NetworkException {
		return null;
	}
	
	/**
	 * 通过讨论区拿到讨论区所有的版面
	 * @param block
	 * @throws NetworkException 
	 */
	public void blockBoard(BlockObject block) throws NetworkException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_BLOCK + block.getId());

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			BBSBodyParseHelper.parseBlockBoardList(XmlOperator.readDocument(entity.getContent()), block);
			
			for (BoardObject board : block.getAllBoardList()) {
				if (board.isDir()) dirBoard(board);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * zone版面的解析
	 * @param dirBoard
	 * @throws NetworkException 
	 */
	public void dirBoard(BoardObject dirBoard) throws NetworkException {
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_BLOCK_DIR + dirBoard.getName());

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			
			BBSBodyParseHelper.parseChildBoardList(XmlOperator.readDocument(entity.getContent()), dirBoard);
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
}
