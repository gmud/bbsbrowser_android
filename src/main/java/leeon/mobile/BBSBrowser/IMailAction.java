package leeon.mobile.BBSBrowser;

import java.util.List;

import leeon.mobile.BBSBrowser.models.MailObject;

/**
 * 邮件相关接口
 */
public interface IMailAction extends IAction {
	
	/**
	 * 返回总邮件数用于分页 
	 * @return 当前用户邮件总数，该方法必须在mailList方法调用后调用才有意义
	 */
	public int totalMailCount();
	
	/**
	 * 获取邮件列表
	 * @param start 分页标志
	 * @return 邮件列表信息
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如当前没有登录，或者没有邮件权限等。
	 */
	public List<MailObject> mailList(int start) throws NetworkException, ContentException;
	
	/**
	 * 获取邮件列表，默认调用mailList(0);
	 */
	public List<MailObject> mailList() throws NetworkException, ContentException;
	
	/**
	 * 检查是否有新邮件
	 * @return 如果有新邮件返回新邮件列表，否则返回empty list
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如当前没有登录，或者没有邮件权限等。
	 */
	public List<MailObject> newMailList() throws NetworkException, ContentException;
	
	/**
	 * 获取邮件的内容
	 * @param mail 要获取邮件content的邮件mailObject，content放入mailObject中返回
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如当前没有登录，或者没有邮件权限等。
	 */
	public void conMail(MailObject mail) throws NetworkException, ContentException;
	
	/**
	 * 获取回复邮件的re文内容，也可以不实现该方法
	 * 这样回信时，将看不到被回的信原文
	 * @param mail 要回复的邮件
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如当前没有登录，或者没有邮件权限等。
	 */
	public void conReMail(MailObject mail) throws NetworkException, ContentException;
	
	/**
	 * 发邮件 
	 * @param mail 被发送的邮件
	 * @param backup 是否备份给自己
	 * @return 成功返回true，失败有异常抛出
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如当前没有登录，或者没有邮件权限等。
	 */
	public boolean sendMail(MailObject mail, boolean backup) throws NetworkException, ContentException;
	
	/**
	 * 删除邮件，
	 * @param mail 需要被删除的邮件对象
	 * @return 成功返回true，失败有异常抛出
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，比如当前没有登录，或者没有邮件权限等。
	 */
	public boolean delMail(MailObject mail) throws NetworkException, ContentException;

}
