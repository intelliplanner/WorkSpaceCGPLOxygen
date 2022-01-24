package com.ipssi.dashboard.customization;
public class DashBean {
	private int id;
	private String pg_context;
	private String pg_title;
	private int status;
	private int portNodeId;
	private int userId;
	private String help;
	private String pg_action;
	private String updatedOn;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPgContext() {
		return pg_context;
	}
	public void setPgContext(String pg_context) {
		this.pg_context = pg_context;
	}
	public String getPgTitle() {
		return pg_title;
	}
	public void setPgTitle(String pg_title) {
		this.pg_title = pg_title;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getPortNodeId() {
		return portNodeId;
	}
	public void setPortNodeId(int portNodeId) {
		this.portNodeId = portNodeId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}
	public String getPgAction() {
		return pg_action;
	}
	public void setPgAction(String pg_action) {
		this.pg_action = pg_action;
	}
	public String getHelp() {
		return help;
	}
	public void setHelp(String help) {
		this.help = help;
	}

}

