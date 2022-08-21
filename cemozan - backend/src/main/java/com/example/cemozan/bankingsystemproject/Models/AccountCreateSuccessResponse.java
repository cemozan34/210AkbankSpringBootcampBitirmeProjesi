package com.example.cemozan.bankingsystemproject.Models;

public class AccountCreateSuccessResponse {
	private boolean success;
	
	private String message;
	
	private String accountDetails;

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

	public String getAccountDetails() {
		return accountDetails;
	}

	public void setAccountDetails(String accountDetails) {
		this.accountDetails = accountDetails;
	}
	
	
}
