package leeon.mobile.BBSBrowser;

import java.util.List;

import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;

/**
 * 版面操作的相关接口
 * @author leeon
 */
public interface IBoardAction extends IAction {
		
	/**
	 * 获取所有讨论区的对象
	 * 包括获取讨论区的推荐版面列表
	 * @return 讨论区列表
	 * @throws NetworkException
	 */
	public List<BlockObject> allBlock() throws NetworkException;
	
	/**
	 * 获取所有版面对象
	 * @return 版面列表
	 * @throws NetworkException
	 */
	public List<BoardObject> allBoard() throws NetworkException;
	
	/**
	 * 根据讨论区，获取讨论区对应的版面列表，获取到的信息加载到讨论区对象的boardList中
	 * @param block 讨论区对象
	 * @throws NetworkException
	 */
	public void blockBoard(BlockObject block) throws NetworkException;
	
	
	/**
	 * 根据版面组对象，获取版面组对应的版面列表，获取到的信息加载到版面组对象的boardList中
	 * @param dirBorad 版面组对象
	 * @throws NetworkException
	 */
	public void dirBoard(BoardObject dirBoard) throws NetworkException;
	
}
