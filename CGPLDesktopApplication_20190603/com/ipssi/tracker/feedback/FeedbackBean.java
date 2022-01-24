/**
 * 
 */
package com.ipssi.tracker.feedback;

/**
 * @author jai
 *
 */
public class FeedbackBean {
	private int userId;
	private String feedback;
	private String userName;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getFeedback() {
		return feedback;
	}
	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserName() {
		return userName;
	}
	
	
}
