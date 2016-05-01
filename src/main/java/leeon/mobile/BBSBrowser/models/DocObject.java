package leeon.mobile.BBSBrowser.models;

import java.io.Serializable;


public class DocObject implements Serializable {
	
	private static final long serialVersionUID = 3L;

	private String id = null;
	
	private String status = null;
	
	private String author = null;
	
	private String date = null;
	
	private String title = null;
		
	private String content = null;
	
	private String content1 = null;
	
	private String docNumber = null;
	
	private boolean sticky = false;
	
	private BoardObject board = null;
	
	private String gid = null;
	
	private String rid = null;

	public DocObject(String id, String status, String author, String date, String title, boolean sticky, BoardObject board) {
		this.id = id;
		this.author = author;
		this.status = status;
		this.date = date;
		this.title = title;
		this.sticky = sticky;
		this.board = board;		
	}
	
	public DocObject(String id, String author, String date, String title, String content, BoardObject board) {
		this.id = id;
		this.author = author;
		this.content = content;
		this.board = board;	
		this.date = date;
		this.title = title;
	}
	
	public DocObject(String id, String gid, String title, BoardObject board) {
		this.id = id;
		this.gid =gid;
		this.board = board;	
		this.title = title;
	}
	
	public DocObject(String id, String author, String title, String docNumber, String boardName) {
		this.id = id;
		this.author = author;
		this.title = title;
		this.docNumber = docNumber;
		this.board = new BoardObject(boardName);
	}
	
	public DocObject(String title, String content, BoardObject board) {
		this.content = content;
		this.board = board;	
		this.title = title;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public BoardObject getBoard() {
		return board;
	}

	public void setBoard(BoardObject board) {
		this.board = board;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
	
	public String getDocNumber() {
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}
	
	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}
	
	public String getContent1() {
		return content1;
	}

	public void setContent1(String content1) {
		this.content1 = content1;
	}
	
	@Override
	public String toString() {
		if (this.content != null) return content;
		else return title + "[" + board + "]" + "(" + id +")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (board == null || ((DocObject)obj).board == null) return false;
		return id.equals(((DocObject)obj).id) && board.equals(((DocObject)obj).board);
	}
}
