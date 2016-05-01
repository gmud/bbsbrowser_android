package leeon.mobile.BBSBrowser;

import java.util.List;

import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.PathObject;

/**
 * 精华区访问接口
 */
public interface IPathAction extends IAction {

	/**
	 * 返回精华区的根目录对象
	 * @return 根目录对象，实现时一般用静态变量写死
	 */
	public PathObject fetchRoot();
	
	/**
	 * 从版面入口查看精华区，获取该版面的精华区一级目录列表
	 * @param board 版面对象
	 * @return 该版面精华区一级目录列表
	 * @throws NetworkException 网络异常
	 */
	public List<PathObject> fetchPath(BoardObject board) throws NetworkException;
	
	/**
	 * 从查看某个目录的下级目录列表方法
	 * @param parent 父目录对象
	 * @return 该父目录下一级目录列表
	 * @throws NetworkException 网络异常
	 */
	public List<PathObject> fetchPath(PathObject parent) throws NetworkException;
	
	/**
	 * 查看精华区的具体文章内容 
	 * @param file 具体精华区的文章的路径对象
	 * @return 文章内容
	 * @throws NetworkException 网络异常
	 */
	public String fetchContent(PathObject file) throws NetworkException;
}
