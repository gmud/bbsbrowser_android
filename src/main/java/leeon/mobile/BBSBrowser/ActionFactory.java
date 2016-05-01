package leeon.mobile.BBSBrowser;

import java.util.HashMap;
import java.util.Map;

import leeon.mobile.BBSBrowser.actions.BBSBoardAction;
import leeon.mobile.BBSBrowser.actions.BBSDocAction;
import leeon.mobile.BBSBrowser.actions.BBSLogAction;
import leeon.mobile.BBSBrowser.actions.BBSMailAction;
import leeon.mobile.BBSBrowser.actions.BBSPathAction;
import leeon.mobile.BBSBrowser.actions.BBSPostAction;
import leeon.mobile.BBSBrowser.sjtu.BBSSjtuBoardAction;
import leeon.mobile.BBSBrowser.sjtu.BBSSjtuDocAction;
import leeon.mobile.BBSBrowser.yanxi.BBSYanxiBoardAction;
import leeon.mobile.BBSBrowser.yanxi.BBSYanxiDocAction;

/**
 * 构造action的工厂类
 * 根据入参不同，产生不同的action实现
 * 加入新的接口实现时，修改
 * @author leeon
 */
public class ActionFactory {
	
	public static final int FDU_PARAM = 0;
	public static final int SJTU_PARAM = 1;
	public static final int YANXI_PARAM = 2;
	
	public static final int DEFAULT_PARAM = FDU_PARAM;
	public static final int[] PARAMS = new int[] {FDU_PARAM, SJTU_PARAM, YANXI_PARAM};
	public static final String[] PARAMS_DESC = new String[] {"日月光华", "饮水思源", "燕曦"};
	
	private static final Map<String, Class<? extends IAction>> IMPL = 
		new HashMap<String, Class<? extends IAction>>() {
		private static final long serialVersionUID = -273482579445006035L;{
			put(SJTU_PARAM + ".IBoardAction", BBSSjtuBoardAction.class);
			put(YANXI_PARAM + ".IBoardAction", BBSYanxiBoardAction.class);
			
			put(SJTU_PARAM + ".IDocAction", BBSSjtuDocAction.class);
			put(YANXI_PARAM + ".IDocAction", BBSYanxiDocAction.class);

			put(FDU_PARAM + ".IBoardAction", BBSBoardAction.class);
			put(FDU_PARAM + ".IDocAction", BBSDocAction.class);
			put(FDU_PARAM + ".IMailAction", BBSMailAction.class);
			put(FDU_PARAM + ".IPostAction", BBSPostAction.class);
			put(FDU_PARAM + ".ILogAction", BBSLogAction.class);
			put(FDU_PARAM + ".IPathAction", BBSPathAction.class);
		}
	};
	
	private static IAction newAction(String param) {
		try {
			return IMPL.get(param).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ActionFactory newInstance() {
		return new ActionFactory();
	}
	
	//版面
	public IBoardAction newBoardAction() {
		return newBoardAction(DEFAULT_PARAM);
	}
	public IBoardAction newBoardAction(int param) {
		return (IBoardAction)newAction(param + ".IBoardAction");
	}
	
	//文章
	public IDocAction newDocAction() {
		return newDocAction(DEFAULT_PARAM);
	}	
	public IDocAction newDocAction(int param) {
		return (IDocAction)newAction(param + ".IDocAction");	
	}
	
	//精华
	public IPathAction newPathAction() {
		return newPathAction(DEFAULT_PARAM);
	}	
	public IPathAction newPathAction(int param) {
		return (IPathAction)newAction(param + ".IPathAction");	
	}
	
	//登录
	public ILogAction newLogAction() {
		return newLogAction(DEFAULT_PARAM);
	}	
	public ILogAction newLogAction(int param) {
		return (ILogAction)newAction(param + ".ILogAction");	
	}
	
	//发文
	public IPostAction newPostAction() {
		return newPostAction(DEFAULT_PARAM);
	}
	public IPostAction newPostAction(int param) {
		return (IPostAction)newAction(param + ".IPostAction");
	}
	
	//邮件
	public IMailAction newMailAction() {
		return newMailAction(DEFAULT_PARAM);
	}
	public IMailAction newMailAction(int param) {
		return (IMailAction)newAction(param + ".IMailAction");
	}
	
	//好友
	public IUserAction newUserAction() {
		return newUserAction(DEFAULT_PARAM);
	}
	public IUserAction newUserAction(int param) {
		return (IUserAction)newAction(param + ".IUserAction");
	}
	
}
