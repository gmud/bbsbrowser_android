package leeon.mobile.BBSBrowser.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leeon.mobile.BBSBrowser.IPathAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.PathObject;
import leeon.mobile.BBSBrowser.utils.XmlOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;

public class BBSPathAction implements IPathAction {

	/**
	 * 精华区根目录
	 */
	public static PathObject root = new PathObject("", "d");
	
	/**
	 * 版面精华区的路径映射表
	 */
	public static Map<String, PathObject> boardPathMap = new HashMap<String, PathObject>();
	
	/**
	 * 获取根目录
	 */
	public PathObject fetchRoot() {
		return root;
	}
	
	/**
	 * 从版面路径进入获取精华区
	 */
	public List<PathObject> fetchPath(BoardObject board) throws NetworkException {
		if (boardPathMap.containsKey(board.getId())) {
			return boardPathMap.get(board.getId()).getChildren();
		}
		
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_0AN_BOARD + board.getId());
		
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
						
			Document doc = XmlOperator.readDocument(entity.getContent());
			PathObject parent = new PathObject();
			BBSBodyParseHelper.parsePathList(doc, parent);
			parent = searchAndCreatePathFromRoot(parent);
			boardPathMap.put(board.getId(), parent);
			return parent.getChildren();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 从parent路径进入获取精华区
	 */
	public List<PathObject> fetchPath(PathObject parent) throws NetworkException {
		if (parent.isFetched()) {
			return parent.getChildren();
		} else if (!"d".equals(parent.getType())) {
			return null;
		}
		
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_0AN + parent.getPath());
		
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
						
			Document doc = XmlOperator.readDocument(entity.getContent());
			BBSBodyParseHelper.parsePathList(doc, parent);
			return parent.getChildren();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 获取精华区文章内容
	 */
	public String fetchContent(PathObject file) throws NetworkException {
		if (file.isFetched()) {
			return file.getContent();
		} if (!"f".equals(file.getType())) {
			return null;
		}
		
		HttpClient client = HttpConfig.newInstance();
		HttpGet get = new HttpGet(HttpConfig.bbsURL() + HttpConfig.BBS_ANC + file.getPath());
		
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
						
			Document doc = XmlOperator.readDocument(entity.getContent());
			return BBSBodyParseHelper.parsePathContent(doc, file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 查看如果从board进入的时候该精华区是否已经被fetch过
	 */
	private static PathObject searchAndCreatePathFromRoot(PathObject path) {
		int start = 0;
		int end = path.getPath().indexOf('/', start+1);
		PathObject c = root;
		while (end != -1) {
			String p = path.getPath().substring(0, end);
			if (c.hasChild(p)) {
				c = c.getChild(p);
			} else {
				PathObject po = new PathObject(p, "d");
				c.addChild(po);
				c = po;
			}

			start = end;
			end = path.getPath().indexOf('/', start+1);			
		}
		
		if (c.hasChild(path.getPath())) {
			c.getChild(path.getPath()).copyChildrenFrom(path);
			return c.getChild(path.getPath());
		} else {
			c.addChild(path);
			return path;
		}
	}
	
	/**
	 * @param args
	 * @throws NetworkException 
	 */
	public static void main(String[] args) throws NetworkException {
//		fetchPath(root);
//		fetchPath(new BoardObject("11", null, null));
//		fetchPath(root.getChild("/groups"));
//		fetchPath(boardPathMap.get("11").getParent());
//		fetchContent(root.getChild("/masters"));
//		System.out.println(root);
	}

}
