package leeon.mobile.BBSBrowser;

import java.util.List;

import leeon.mobile.BBSBrowser.models.BoardObject;

import org.apache.http.cookie.Cookie;

/**
 * 用户相关基本操作接口
 * 包括登录，收藏等操作
 * @author leeon
 */
public interface ILogAction extends IAction {
	
	/**
	 * 登录bbs，相当于login(userId, password, null)
	 * @param userId 用户id
	 * @param password 用户密码
	 * @return 成功返回true，失败有异常抛出
	 * @throws ContentException 可恢复异常，比如密码不正确，用户不存在等等
	 * @throws NetworkException 网络异常
	 */
	public boolean login(String userId, String password) throws ContentException, NetworkException;
	
	
	/**
	 * 登录bbs
	 * @param userId 用户id
	 * @param password 用户密码
	 * @param cookieList 传入一个List对象，通过该对象获取cookie的值，如果传null，则不能获取。
	 * @return 成功返回true，失败有异常抛出
	 * @throws ContentException 可恢复异常，比如密码不正确，用户不存在等等
	 * @throws NetworkException 网络异常
	 */
	public boolean login(String userId, String password, List<Cookie> cookieList) throws ContentException, NetworkException;
	
	/**
	 * 退出bbs，相当于logout(null)
	 * @throws NetworkException 网络异常
	 */
	public void logout() throws NetworkException;
	
	/**
	 * 退出bbs，并且根据传入的cookieString踢到服务器端的session
	 * @param cookieString
	 * @throws NetworkException 网络一场
	 */
	public void logout(String cookieString) throws NetworkException;
	
	/**
	 * 当前用户的收藏版面
	 * @return 返回收藏版面列表，如果为空，则是empty list
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有登录
	 */
	public List<BoardObject> favBoard() throws NetworkException, ContentException;
	
	/**
	 * 添加收藏版面
	 * @param board 需要增加的收藏版面对象
	 * @return 成功返回true，失败抛出异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如版面已经存在，或者没有登录等
	 */
	public boolean addFavBoard(BoardObject board) throws NetworkException, ContentException;
	
	/**
	 * 设置收藏版面，通过该方法可以添加也可以删除收藏版面
	 * @param list 需要设置为收藏版面的版面列表对象
	 * @return 成功返回true，失败抛出异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如版面已经存在，或者没有登录等
	 */
	public boolean setFavBoard(List<BoardObject> list) throws NetworkException, ContentException;
}
