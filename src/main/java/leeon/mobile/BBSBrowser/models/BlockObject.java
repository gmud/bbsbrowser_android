package leeon.mobile.BBSBrowser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BlockObject implements Serializable {
	
	private static final long serialVersionUID = 2L;
	
	private String id = null;
	private String name = null;
	
	private List<BoardObject> recommendBoardList = new ArrayList<BoardObject>();
	private List<BoardObject> allBoardList = new ArrayList<BoardObject>();
	
	public BlockObject(String id, String name) {
		this.id = id;
		this.name = name;		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}	

	public List<BoardObject> getRecommendBoardList() {
		return recommendBoardList;
	}

	public void setRecommendBoardList(List<BoardObject> recommendBoardList) {
		this.recommendBoardList = recommendBoardList;
	}

	public List<BoardObject> getAllBoardList() {
		return allBoardList;
	}

	public void setAllBoardList(List<BoardObject> allBoardList) {
		this.allBoardList = allBoardList;
	}
	
	@Override
	public String toString() {
		return name + "(" + id + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return id.equals(((BlockObject)obj).id);
	}
}
