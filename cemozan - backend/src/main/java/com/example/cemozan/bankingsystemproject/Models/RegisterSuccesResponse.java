package com.example.cemozan.bankingsystemproject.Models;

public class RegisterSuccesResponse {
	private boolean success;
	private String message;
	private String userDetails;
	public boolean getSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUserDetails() {
		return userDetails;
	}
	public void setUserDetails(String userDetails) {
		this.userDetails = userDetails;
	}
	
	
}
