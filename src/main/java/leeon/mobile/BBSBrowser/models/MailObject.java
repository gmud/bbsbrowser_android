package leeon.mobile.BBSBrowser.models;

import java.io.Serializable;

public class MailObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8063229568288935445L;

	private String number = null;

	private String status = null;
	
	private String sender = null;
	
	private String date = null;
	
	private String title = null;
	
	private String id = null;
	
	private String content = null;
	
	private String reContent = null;
	
	public MailObject(String number, String status, String sender, String date, String title, String id) {
		this.number = number;
		this.sender = sender;
		this.status = status;
		this.date = date;
		this.title = title;
		this.id = id;
	}
	
	public MailObject(String sender, String title, String content) {
		this.sender = sender;
		this.title = title;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
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

	public String getReContent() {
		return reContent;
	}

	public void setReContent(String reContent) {
		this.reContent = reContent;
	}
	
}
