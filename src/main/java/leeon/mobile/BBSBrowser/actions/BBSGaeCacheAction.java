package leeon.mobile.BBSBrowser.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import leeon.mobile.BBSBrowser.IBoardAction;
import leeon.mobile.BBSBrowser.IDocAction;
import leeon.mobile.BBSBrowser.NetworkException;
import leeon.mobile.BBSBrowser.models.BlockObject;
import leeon.mobile.BBSBrowser.models.BoardObject;
import leeon.mobile.BBSBrowser.models.DocObject;
import leeon.mobile.BBSBrowser.utils.XmlOperator;

import org.w3c.dom.Document;


/**
 * 由于GAE里无法使用http client，所以使用该类
 * 来获取版面文档等信息，该类没有要求登录的方法
 * @author leeon
 */
public class BBSGaeCacheAction implements IBoardAction, IDocAction {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BoardObject b = new BoardObject("130", "News", "新闻信息");
		DocObject d = new DocObject("2237692", null, null, null, null, b);
		new BBSGaeCacheAction().docContent(d, false);
	}
		
	/**
	 * 版面文章
	 */
	@Override
	public List<DocObject> boardDoc(BoardObject board, boolean isTitle) throws NetworkException {
		return boardDoc(board, isTitle, 0, 0);
	}

	/**
	 * 版面文章
	 */
	@Override
	public List<DocObject> boardDoc(BoardObject board, boolean isTitle, int fetchedTotalCount, int stickyCount) throws NetworkException {
		int start = BBSDocAction.calculateStartFromFetched(board, fetchedTotalCount, stickyCount); 
		try {
			String url = BBSDocAction.boardDocUrl(board, isTitle, start);
			Document doc = XmlOperator.readDocument(readUrl(url));			
			return BBSBodyParseHelper.parseDocList(doc, board); 
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

	/**
	 * 文章内容
	 */
	@Override
	public List<DocObject> docContent(DocObject doc, boolean isTitle) throws NetworkException {
		return docContent(doc, isTitle, (String)null);
	}
	
	/**
	 * 文章内容
	 */
	@Override
	public List<DocObject> docContent(DocObject doc, boolean isTitle, String tag) throws NetworkException {
		return null;//gae无需实现
	}
	
	/**
	 * 文章内容
	 */
	@Override
	public List<DocObject> docContent(DocObject doc, boolean isTitle, DocObject gdoc) throws NetworkException {
		try {
			String url = BBSDocAction.docContentUrl(doc, isTitle, gdoc, null);
			Document document1 = XmlOperator.readDocument(readUrl(url));
			Document document2 = XmlOperator.readDocument(readUrl(url+"&new=1"));
			return BBSBodyParseHelper.parseContentListNewForGAE(document1, document2, doc);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

	/**
	 * 十大
	 */
	@Override
	public List<DocObject> topTenDocList() throws NetworkException {
		try {
			Document doc = XmlOperator.readDocument(readUrl(HttpConfig.bbsURL() + HttpConfig.BBS_TOPTEN));
			return BBSBodyParseHelper.parseTopTenList(doc); 						
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

	/**
	 * 所有板块
	 */
	@Override
	public List<BlockObject> allBlock() throws NetworkException {
		try {
			Document doc = XmlOperator.readDocument(readUrl(HttpConfig.bbsURL() + HttpConfig.BBS_BOARD));
			List<BlockObject> blockList = BBSBodyParseHelper.parseBlockList(doc); 
			
			for (BlockObject block : blockList) {
				blockBoard(block);
			}
			return blockList;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	/**
	 * 获取所有版面，没有用到不做实现
	 */
	public List<BoardObject> allBoard() throws NetworkException {
		return null;
	}
	
	/**
	 * 板块版面
	 */
	@Override
	public void blockBoard(BlockObject block) throws NetworkException {
		try {
			Document doc = XmlOperator.readDocument(readUrl(HttpConfig.bbsURL() + HttpConfig.BBS_BLOCK + block.getId()));
			BBSBodyParseHelper.parseBlockBoardList(doc, block);
			
			for (BoardObject board : block.getAllBoardList()) {
				if (board.isDir()) dirBoard(board);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}

	/**
	 * 版面组版面
	 */
	@Override
	public void dirBoard(BoardObject dirBoard) throws NetworkException {
		try {
			Document doc = XmlOperator.readDocument(readUrl(HttpConfig.bbsURL() + HttpConfig.BBS_BLOCK_DIR + dirBoard.getName()));
			BBSBodyParseHelper.parseChildBoardList(doc, dirBoard);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkException(e);
		}
	}
	
	
	/**
	 * 通过get方法读取信息
	 * @param url url
	 * @return 输出流
	 * @throws IOException
	 */
	private static final int MAX_RETRY = 3;
	private InputStream readUrl(String url) throws IOException {
		int i = 0;
		while (true) {
			try {
				URL u = new URL(url);
				return u.openStream();
			} catch (IOException e) {
				if (i++ >= MAX_RETRY) throw e;
			}
		}
	}

	/*
	private byte[] readUrlByte(String url) throws IOException {
		InputStream is = readUrl(url);
		StringBuffer sb = new StringBuffer();
		try {
			byte[] b = new byte[1024];
			int i = 0;
			while ((i = is.read(b)) != -1) {
				sb.append(new String(b, 0, i, BBSBodyParseHelper.BBS_CHARSET));
				
			}
		} finally {
			is.close();
		}
		return sb.toString().getBytes(BBSBodyParseHelper.BBS_CHARSET);
	}
	*/

}
