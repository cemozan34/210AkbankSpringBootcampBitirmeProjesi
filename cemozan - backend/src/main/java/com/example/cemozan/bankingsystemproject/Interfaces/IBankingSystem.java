package com.example.cemozan.bankingsystemproject.Interfaces;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.cemozan.bankingsystemproject.Models.AccountCreateRequest;
import com.example.cemozan.bankingsystemproject.Models.CreateBankRequest;
import com.example.cemozan.bankingsystemproject.Models.EnableRequest;
import com.example.cemozan.bankingsystemproject.Models.IncreaseBalance;
import com.example.cemozan.bankingsystemproject.Models.LoginRequest;
import com.example.cemozan.bankingsystemproject.Models.MoneyTransferRequest;
import com.example.cemozan.bankingsystemproject.Models.MoneyTransferResponse;
import com.example.cemozan.bankingsystemproject.Models.UserRegisterRequest;


public interface IBankingSystem {
	public ResponseEntity<?> createAccount(@RequestBody AccountCreateRequest request);
	public ResponseEntity<?> getAccountDetails(@PathVariable String accountNumber);
	public ResponseEntity<?> increaseBalance(@PathVariable String accountNumber, @RequestBody IncreaseBalance request);
	public ResponseEntity<MoneyTransferResponse> moneyTransfer(@PathVariable String fromAccountNumber, @RequestBody MoneyTransferRequest request) throws IOException;
	public ResponseEntity<?> removeAccount(@PathVariable String id);
	public ResponseEntity<?> createBank(@RequestBody CreateBankRequest request);
	public ResponseEntity<?> register(@RequestBody UserRegisterRequest request);
	public ResponseEntity<?> enableDisableUser(@PathVariable String id, @RequestBody EnableRequest request);
	public ResponseEntity<?> login(LoginRequest request);
	public ResponseEntity<?> getAllAccounts();
}
