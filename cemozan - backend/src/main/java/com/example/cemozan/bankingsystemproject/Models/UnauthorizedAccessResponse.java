package com.example.cemozan.bankingsystemproject.Models;

public class UnauthorizedAccessResponse {
	private boolean success;
	private String message;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	
}
