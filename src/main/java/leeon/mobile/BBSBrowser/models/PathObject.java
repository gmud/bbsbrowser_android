package leeon.mobile.BBSBrowser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathObject implements Serializable {
	
	private static final long serialVersionUID = 7400510764491469057L;

	private String path;
	private String author;
	private String name;
	private String time;
	private String type;
	private String content;
	
	private boolean fetched;
	
	private PathObject parent;
	private List<PathObject> children = new ArrayList<PathObject>();
	private Map<String, PathObject> childrenMap = new HashMap<String, PathObject>();
	
	public PathObject() {
	}
	
	public PathObject(String path, String type) {
		this.path = path;
		this.type = type;
	}

	public PathObject(String path, String author, String name, String time, String type) {
		this.path = path;
		this.author = author;
		this.name = name;
		this.time = time;
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public PathObject getParent() {
		return parent;
	}

	public void setParent(PathObject parent) {
		this.parent = parent;
	}

	public List<PathObject> getChildren() {
		return children;
	}
	
	public void addChild(PathObject child) {
		this.children.add(child);
		this.childrenMap.put(child.getPath(), child);
		child.setParent(this);
	}
	
	public void removeChild(PathObject child) {
		this.children.remove(child);
		this.childrenMap.remove(child.getPath());
		child.setParent(null);
	}
	
	public void clearChildren() {
		for (PathObject child : this.children) {
			this.removeChild(child);
		}
	}
	
	public PathObject getChild(String path) {
		return this.childrenMap.get(path);
	}
	
	public boolean hasChild(String path) {
		return this.childrenMap.containsKey(path);
	}
	
	public void reIndexChild(String path) {
		PathObject child = getChild(path);
		if (child != null) {
			this.removeChild(child);
			this.addChild(child);
		}
	}
	
	public void copyChildrenFrom(PathObject another) {
		this.children = another.children;
		this.childrenMap =another.childrenMap;
		this.fetched = true;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isFetched() {
		return fetched;
	}

	public void setFetched(boolean fetched) {
		this.fetched = fetched;
	}
	
}
