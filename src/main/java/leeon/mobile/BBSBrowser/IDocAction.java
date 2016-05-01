package leeon.mobile.BBSBrowser;

import java.util.List;

import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;


/**
 * 文章帖子读取操作的相关接口
 * @author leeon
 */
public interface IDocAction extends IAction {

	/**
	 * 获取十大的接口
	 * @return
	 * @throws NetworkException
	 */
	public List<DocObject> topTenDocList() throws NetworkException;
			
	/**
	 * 获取版面的文章列表
	 * 文章列表的总文章数通过board对象返回
	 * 用于提取默认页的文章数，一般实现的时候相当于调用boardDoc(board, title, 0, 0);
	 * @param board 版面对象
	 * @param isTitle 是否主题模式
	 * @return 版面文章列表
	 * @throws NetworkException
	 */
	public List<DocObject> boardDoc(BoardObject board, boolean isTitle) throws NetworkException;
	
	/**
	 * 获取版面的文章列表
	 * 文章列表的总文章数通过board对象返回
	 * 如果有重复的文章，在实现中解决
	 * @param board 版面对象
	 * @param isTitle 是否主题模式
	 * @param fetchedTotalCount 界面一已经提取的文章数,通过该参数和stickyCount可以计算出分页的逻辑
	 * @param stickyCount 置底的文章数
	 * @return 版面文章列表
	 * @throws NetworkException
	 */
	public List<DocObject> boardDoc(BoardObject board, boolean isTitle, int fetchedTotalCount, int stickyCount) throws NetworkException;
	
	/**
	 * 获取文档的内容
	 * @param doc 需要获取内容的文章对象
	 * @param isTitle 是否主题模式
	 * @return 文章内容
	 * @throws NetworkException
	 * @throws ContentException 
	 */
	public List<DocObject> docContent(DocObject doc, boolean isTitle) throws NetworkException, ContentException;
	
	/**
	 * 获取文档的内容
	 * @param doc 需要获取内容的文章对象
	 * @param isTitle 是否主题模式
	 * @param tag 用来获取文章的上篇，下篇，上楼，下楼，上主题、下主题的标志
	 * @return 文章内容
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public List<DocObject> docContent(DocObject doc, boolean isTitle, String tag) throws NetworkException, ContentException;
	
	/**
	 * 获取文档的内容
	 * @param doc 文章对象
	 * @param isTitle 是否主题模式
	 * @return 文章内容
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public List<DocObject> docContent(DocObject doc, boolean isTitle, DocObject gdoc) throws NetworkException, ContentException;
}
