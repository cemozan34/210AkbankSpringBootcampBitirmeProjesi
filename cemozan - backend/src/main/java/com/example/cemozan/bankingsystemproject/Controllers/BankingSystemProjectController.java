package com.example.cemozan.bankingsystemproject.Controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.cemozan.bankingsystemproject.Interfaces.IBankingSystem;
import com.example.cemozan.bankingsystemproject.Models.AccountCreateRequest;
import com.example.cemozan.bankingsystemproject.Models.CreateBankRequest;
import com.example.cemozan.bankingsystemproject.Models.EnableRequest;
import com.example.cemozan.bankingsystemproject.Models.IncreaseBalance;
import com.example.cemozan.bankingsystemproject.Models.LoginRequest;
import com.example.cemozan.bankingsystemproject.Models.MoneyTransferRequest;
import com.example.cemozan.bankingsystemproject.Models.MoneyTransferResponse;
import com.example.cemozan.bankingsystemproject.Models.UserRegisterRequest;



@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class BankingSystemProjectController {
	
	@Autowired
	private IBankingSystem service;
	
	
	
	@PostMapping(path = "/accounts", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> createAccount(@RequestBody AccountCreateRequest request){

		return this.service.createAccount(request);
    }
	
	@GetMapping(path = "/accounts/{id}")
	public ResponseEntity<?> getAccountDetails(@PathVariable String id){
		
		return this.service.getAccountDetails(id);
	}
	
	@PatchMapping(path="accounts/{accountNumber}", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> increaseBalance(@PathVariable String accountNumber, @RequestBody IncreaseBalance request){
		
		return this.service.increaseBalance(accountNumber, request);
	}

	@PutMapping(path="accounts/{fromAccountNumber}", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<MoneyTransferResponse> moneyTransfer(@PathVariable String fromAccountNumber, @RequestBody MoneyTransferRequest request) throws IOException{
		
		return this.service.moneyTransfer(fromAccountNumber, request);
	}
	
	
	@DeleteMapping("/accounts/{id}")
	public ResponseEntity<?> removeAccount(@PathVariable String id) {
		
		return this.service.removeAccount(id);
	}
	
	
	@PostMapping("/auth")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		
		return this.service.login(request);
	}
	
	
	@PostMapping("/createBank")
	public ResponseEntity<?> createBank(@RequestBody CreateBankRequest request) {
		
		return this.service.createBank(request);
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
		
		return this.service.register(request);
	}
	
	@PatchMapping("/users/{id}")
	public ResponseEntity<?> enableDisableUser(@PathVariable String id, @RequestBody EnableRequest request) {
		
		return this.service.enableDisableUser(id, request);
	}
	
	@GetMapping("/allaccounts")
	public ResponseEntity<?> getAllAccounts() {
		
		return this.service.getAllAccounts();
	}


}
