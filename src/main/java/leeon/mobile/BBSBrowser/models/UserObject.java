package leeon.mobile.BBSBrowser.models;

public class UserObject {
	
	private String number = null;
	
	private String id = null;
	
	private String nick = null;
	
	private String status = null;
	
	private String from = null;
	
	private boolean friend = false;
	
	public UserObject(String number, String id, String nick, String status, String from, boolean friend) {
		this.number = number;
		this.id = id;
		this.nick = nick;
		this.status = status;
		this.from = from;
		this.friend = friend;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isFriend() {
		return friend;
	}

	public void setFriend(boolean friend) {
		this.friend = friend;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
}
