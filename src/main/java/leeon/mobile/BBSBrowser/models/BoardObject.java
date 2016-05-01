package leeon.mobile.BBSBrowser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BoardObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id = null;
	
	private String name = null;
	
	private String type = null;
	
	private String ch = null;
	
	private String master = null;
	
	private String docNumber = null;
	
	private int total = 0;
	
	private int totalG = 0;
	
	private boolean dir = false;
	
	private boolean attach = false;		
	
	private List<BoardObject> childBoardList = new ArrayList<BoardObject>();

	public BoardObject(String id, String name, String type, String ch, String master, String docNumber, boolean dir, int total) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.ch = ch;
		this.master = master;
		this.docNumber = docNumber;
		this.dir = dir;
		this.total = total;
	}
	
	public BoardObject(String id, String name, String type, String ch, String master, String docNumber, boolean dir) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.ch = ch;
		this.master = master;
		this.docNumber = docNumber;
		this.dir = dir;
	}
	
	public BoardObject(String name, String ch) {
		this.name = name;
		this.ch = ch;
	}
	
	public BoardObject(String id, String name, String ch) {
		this.id = id;
		this.name = name;
		this.ch = ch;
	}
	
	public BoardObject(String name) {
		this.name = name;
	}


	public String getCh() {
		return ch;
	}


	public void setCh(String ch) {
		this.ch = ch;
	}


	public String getDocNumber() {
		return docNumber;
	}


	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}


	public String getMaster() {
		return master;
	}


	public void setMaster(String master) {
		this.master = master;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public boolean isDir() {
		return dir;
	}

	public void setDir(boolean dir) {
		this.dir = dir;
	}

	public List<BoardObject> getChildBoardList() {
		return childBoardList;
	}

	public void setChildBoardList(List<BoardObject> childBoardList) {
		this.childBoardList = childBoardList;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	public int getTotalG() {
		return totalG;
	}

	public void setTotalG(int totalG) {
		this.totalG = totalG;
	}

	public boolean isAnony() {
		return "Heart".equals(name);
	}

	public boolean isAttach() {
		return attach;
	}

	public void setAttach(boolean attach) {
		this.attach = attach;
	}
	
	@Override
	public String toString() {
		return name + "[" + ch + "]" + "(" + id +")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return name.equals(((BoardObject)obj).name);
	}

}
