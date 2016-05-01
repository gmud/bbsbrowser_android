package leeon.mobile.BBSBrowser;

import java.io.File;

import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;

/**
 * 和发帖转帖等帖子操作有关的方法
 * @author leeon
 */
public interface IPostAction extends IAction {

	/**
	 * 发文时获取版面信息，及某篇文章的原文的引文
	 * 用于re某篇文章，读取re文时的一些被re的文章内容
	 * @param board 版面对象
	 * @param doc 被re的文章
	 * @return 引文信息对象
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，没有登录或者没有发文权限等
	 */
	public DocObject inPostDoc(BoardObject board, DocObject doc) throws NetworkException, ContentException;
	
	/**
	 * 发文时获取该发文版面信息
	 * 用于发新文章，相当于调用inPostDoc(board, null);
	 * @param board 版面对象
	 * @return 引文信息对象
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，没有登录或者没有发文权限等
	 */
	public DocObject inPostDoc(BoardObject board) throws NetworkException, ContentException;
	
	/**
	 * 发表文章
	 * @param newdoc 被发表的新帖对象
	 * @param olddoc 被re的旧帖，或者被修改的旧帖，发新帖时该值为null
	 * @param anony 是否匿名，匿名版时使用
	 * @param edit 是否是修改原帖
	 * @param sig 使用哪一个签名档
	 * @return 成功返回true，失败抛出异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，没有登录或者没有发文权限等
	 */
	public boolean sendPostDoc(DocObject newdoc, DocObject olddoc, boolean anony, boolean edit, int sig) throws NetworkException, ContentException;
	
	/**
	 * 删帖操作
	 * @param doc 被删的帖子
	 * @return 成功返回true，失败抛出异常
	 * @throws NetworkException
	 * @throws ContentException
	 */
	public boolean delPostDoc(DocObject doc) throws NetworkException, ContentException;
	
	/**
	 * 上传附件到某个版面的附件区
	 * @param file 被上传的文件
	 * @param board 上传到的版面
	 * @param mimeType 文件类型mimeType，比如image/jpeg
	 * @return 成功后返回访问该附件的url
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，没有登录或者没有上传权限等
	 */
	public String sendAttFile(File file, BoardObject board, String mimeType) throws NetworkException, ContentException;
	
	/**
	 * 转寄帖子
	 * @param doc 被转的帖子
	 * @param user 转信的用户
	 * @return 成功返回true，失败抛出异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，没有登录或者用户不存在等
	 */
	public boolean fwdPostDoc(DocObject doc, String user) throws NetworkException, ContentException;
	
	/**
	 * 转载帖子
	 * @param doc 被转的帖子
	 * @param to 转载到的版面
	 * @return 成功返回true，失败抛出异常
	 * @throws NetworkException 网络异常
	 * @throws ContentException 可恢复异常，没有登录或者没有发文权限等
	 */
	public boolean cccPostDoc(DocObject doc, BoardObject to) throws NetworkException, ContentException;
}
