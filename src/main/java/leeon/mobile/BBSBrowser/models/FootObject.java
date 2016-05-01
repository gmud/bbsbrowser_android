package leeon.mobile.BBSBrowser.models;

public class FootObject {
	
	//当前ID
	private String id = null;
	
	//在线人数
	private String onlineNumber = null;
	
	//邮件数
	private String mailNumber = null;
	
	//登陆时间
	private String loginTime = null;
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("当前用户:").append(id).append("    ");
		s.append("在线用户:").append(onlineNumber).append("    ");
		if (mailNumber != null)
			s.append("用户信箱:").append(mailNumber).append("    ");
		s.append("在线时间:").append(loginTime);
		
		return s.toString();
	}
	
	public FootObject(String id, String onlineNumber, String mailNumber, String loginTime) {
		this.id = id;
		this.onlineNumber = onlineNumber;
		this.mailNumber = mailNumber;
		this.loginTime = loginTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	public String getMailNumber() {
		return mailNumber;
	}

	public void setMailNumber(String mailNumber) {
		this.mailNumber = mailNumber;
	}

	public String getOnlineNumber() {
		return onlineNumber;
	}

	public void setOnlineNumber(String onlineNumber) {
		this.onlineNumber = onlineNumber;
	}
	
	
}
