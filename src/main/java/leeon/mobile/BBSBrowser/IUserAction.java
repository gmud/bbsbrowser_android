package leeon.mobile.BBSBrowser;

import java.util.List;

import leeon.mobile.BBSBrowser.models.UserObject;

/**
 * 用户相关基本操作接口
 * 包括信息，查好友，查用户等操作
 * 该接口目前没有使用
 * @author leeon
 */
public interface IUserAction extends IAction {
	
	/**
	 * 发送即时消息
	 * @param msg 消息
	 * @param user 对方用户
	 * @return 成功放回true，失败抛异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有权限，用户不存在等。
	 */
	public boolean sendMsg(String msg, String user) throws NetworkException, ContentException;
	
	/**
	 * 获取最新的消息
	 * @return 返回消息列表，每个list对象里的String包含一条消息的一些基本信息，比如时间，对方用户，消息内容等
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有权限，没有登录等。
	 */
	public List<String> receiveMsg() throws NetworkException, ContentException;
	
	/**
	 * 在线好友列表
	 * @return 好友列表信息，没有返回empty list
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有登录等。
	 */
	public List<UserObject> onlineFriends() throws NetworkException, ContentException;
	
	/**
	 * 添加好友
	 * @param user 好友id
	 * @return 成功放回true，失败抛异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有权限，用户不存在等。
	 */
	public boolean addFriend(String user) throws NetworkException, ContentException;
	
	/**
	 * 删除好友
	 * @param user 好友id
	 * @return 成功放回true，失败抛异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有权限，用户不存在等。
	 */
	public boolean delFriend(String user) throws NetworkException, ContentException;
	
	/**
	 * 查找某个用户
	 * @return 返回找到的用户，失败抛异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如没有权限，没有登录，没有对应的用户等。
	 */
	public UserObject findUser() throws NetworkException, ContentException;
}
