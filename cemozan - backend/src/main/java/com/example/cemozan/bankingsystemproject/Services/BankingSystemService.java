package com.example.cemozan.bankingsystemproject.Services;




import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
//import com.cemozan.bankingsystem.Models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.cemozan.bankingsystemproject.Interfaces.IBankingSystem;
import com.example.cemozan.bankingsystemproject.Models.Account;
import com.example.cemozan.bankingsystemproject.Models.AccountCreateRequest;
import com.example.cemozan.bankingsystemproject.Models.AccountCreateResponse;
import com.example.cemozan.bankingsystemproject.Models.AccountCreateSuccessResponse;
import com.example.cemozan.bankingsystemproject.Models.AccountDelete;
import com.example.cemozan.bankingsystemproject.Models.Bank;
import com.example.cemozan.bankingsystemproject.Models.CreateBankFailedResponse;
import com.example.cemozan.bankingsystemproject.Models.CreateBankRequest;
import com.example.cemozan.bankingsystemproject.Models.CreateBankSuccessResponse;
import com.example.cemozan.bankingsystemproject.Models.Deposit;
import com.example.cemozan.bankingsystemproject.Models.EnableRequest;
import com.example.cemozan.bankingsystemproject.Models.EnableResponseMessage;
import com.example.cemozan.bankingsystemproject.Models.IncreaseBalance;
import com.example.cemozan.bankingsystemproject.Models.LoginRequest;
import com.example.cemozan.bankingsystemproject.Models.LoginResponse;
import com.example.cemozan.bankingsystemproject.Models.MoneyTransferRequest;
import com.example.cemozan.bankingsystemproject.Models.MoneyTransferResponse;
import com.example.cemozan.bankingsystemproject.Models.NoAccountFoundResponse;
import com.example.cemozan.bankingsystemproject.Models.RegisterFailedResponse;
import com.example.cemozan.bankingsystemproject.Models.RegisterSuccesResponse;
import com.example.cemozan.bankingsystemproject.Models.UnauthorizedAccessResponse;
import com.example.cemozan.bankingsystemproject.Models.UserRegisterRequest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import JWT.JWTTokenUtil;




@Service
public class BankingSystemService implements IBankingSystem{
	


	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JWTTokenUtil jwtTokenUtil;
	
	@Autowired
	private DatabaseUserDetailsService userDetailsService;
	
	
	public ResponseEntity<?> createAccount(@RequestBody AccountCreateRequest request) {
		
		String[] validTypes = {"TL","DOLAR","Altın"};
		boolean isTypeValid = false;
		for(String type : validTypes) {
			
			if (type.equals(request.getType())) {
				isTypeValid = true;
				break;
			}
		}
		
		if (isTypeValid == false) {

			String message = "Invalid Account Type " + request.getType();
			AccountCreateResponse accountCreateResponse = new AccountCreateResponse();
			accountCreateResponse.setMessage(message);
			return ResponseEntity
					.badRequest()
					.body(accountCreateResponse);
			
		}else {
			

			User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				
			Reader reader;
			 //read configuration and create session factory
			try {
				reader = Resources.getResourceAsReader("myBatis_conf.xml");
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
				// open session, session is connection between database and us(like JDBC connection)
				SqlSession session = sqlSessionFactory.openSession();
				// call query from mapper.xml files with defined id
				com.example.cemozan.bankingsystemproject.Models.User user = session.selectOne("findByUsername",authUser.getUsername());
				
				Account account = new Account();
				
				long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
				String accountNo = Long.toString(number);
				
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String currentTime = Long.toString(timestamp.getTime());

				
				account.setAccountNumber(accountNo);
				account.setBalance(0);
				account.setDeleted(0);
				account.setLastModified(currentTime);
				account.setType(request.getType());
				account.setUserId(user.getId());
				account.setBankId(Integer.parseInt(request.getBankId()));
				account.setCreationDate(currentTime);
				account.setLastModified(currentTime);
				
				
				session.insert("createAccount", account);
				session.commit();
				
				AccountCreateSuccessResponse accountCreateSuccessResponse = new AccountCreateSuccessResponse();
				accountCreateSuccessResponse.setSuccess(true);
				accountCreateSuccessResponse.setMessage("Account Created");
				accountCreateSuccessResponse.setAccountDetails("Account No :"+ accountNo+" User Id: "+ user.getId());
				return ResponseEntity
						.ok()
						.lastModified(timestamp.getTime())
						.body(accountCreateSuccessResponse);
			
			}catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity
						.internalServerError()
						.body(null);
			}	
		}
	}

	
	public ResponseEntity<?> getAccountDetails(@PathVariable String accountNumber){
		
		Reader reader;
        
		try {
		    //read configuration and create session factory
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			Account account = session.selectOne("findByAccountNumber",accountNumber);
			
			if (account != null) {
			    User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			    com.example.cemozan.bankingsystemproject.Models.User u = session.selectOne("findByUsername",authUser.getUsername());
			    
		    
				if(account.getUserId() == (u.getId())) {
					
					return ResponseEntity
							.ok()
							.lastModified(Long.parseLong(account.getLastModified()))
							.body(account);
					
				}else {
					MoneyTransferResponse rsp = new MoneyTransferResponse();
					rsp.setMessage("Unauthorized access");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rsp);
				}
				
			}else {
				return ResponseEntity.badRequest().body(null);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}
		
	}


	
	
	@SuppressWarnings({ "unchecked", "rawtypes", "resource" })
	public ResponseEntity<?> increaseBalance(@PathVariable String accountNumber, @RequestBody IncreaseBalance request) {
		
		Properties props = new Properties();
	    props.put("bootstrap.servers", "localhost:9092");
	    props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
	    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	    Producer<Integer, String> producer = new KafkaProducer<Integer, String>(props);
	    
	    String writedTxtToDb = "";
	    Reader reader;
		String amount = request.getAmount();
		
		try {
		    //read configuration and create session factory
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			Account account = session.selectOne("findByAccountNumber",accountNumber);
			
			Deposit deposit = new Deposit();
			
			if (account != null) {
				//User authUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				com.example.cemozan.bankingsystemproject.Models.User u = session.selectOne("findByUsername",authUser.getUsername());
				
				if(account.getUserId() == (u.getId())) {
					Float oldBalance = account.getBalance();
					Float newBalance = oldBalance + Float.parseFloat(amount);
					
					
					Map<String, Object> params2 = new HashMap<String, Object>();
					params2.put("balance",newBalance);
					params2.put("accountNumber", accountNumber);
					
					int row2 = session.update("updateBalance", params2);
					
					
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					String currentTime = Long.toString(timestamp.getTime());
					
					
					Map<String, Object> params1 = new HashMap<String, Object>();
					params1.put("lastModified",currentTime);
					params1.put("accountNumber", accountNumber);
					
					int row1 = session.update("updateLastModified", params1);
					session.commit();

					if(row1 == 1 && row2 == 1) {
						
						// [hesap numarası], [deposit miktarı] : deposited.
						writedTxtToDb = accountNumber + ", " + request.getAmount() + ": deposited";
						//   2341231231 nolu hesaba 100 [hesap tipi] yatırılmıştır.
						ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
						producer.send(producerRecord);
						
						deposit.setBalance(newBalance);
						deposit.setSuccess(true);
						deposit.setMessage("Deposit Successfully");
						return ResponseEntity.ok().body(deposit);
					}else {
						
						deposit.setSuccess(false);
						deposit.setMessage("Deposit Unsuccessfull");
						return ResponseEntity.internalServerError().body(deposit);
					}
				}else {
					MoneyTransferResponse rsp = new MoneyTransferResponse();
					rsp.setMessage("Unauthorized access");
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rsp);
				}
				
			}else {
				
				deposit.setSuccess(false);
				deposit.setMessage("Deposit Unsuccessful");
				return ResponseEntity.badRequest().body(deposit);
			}
			
			
		}catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}
		
		
		
		
		
	}
	
	
	@SuppressWarnings({ "resource", "unused", "rawtypes", "unchecked" })
	@Transactional
	public ResponseEntity<MoneyTransferResponse> moneyTransfer(@PathVariable String fromAccountNumber, @RequestBody MoneyTransferRequest request) throws IOException{
		
		
		Reader reader;
		String amount = request.getAmount();
		
		try {
		    //read configuration and create session factory
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			
			MoneyTransferResponse moneyTransferResponse = new MoneyTransferResponse();
			
			Account fromAccount = session.selectOne("findByAccountNumber",fromAccountNumber);
			Account toAccount = session.selectOne("findByAccountNumber", request.getTransferredAccountNumber());
			
			Connection conn = null;
			 
			Properties props = new Properties();
		    props.put("bootstrap.servers", "localhost:9092");
		    props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
		    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		    Producer<Integer, String> producer = new KafkaProducer<Integer, String>(props);
		    
		    String writedTxtToDb = "";
		    
		    
		    if (fromAccount != null && toAccount != null) {
				//User authUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			    com.example.cemozan.bankingsystemproject.Models.User u = session.selectOne("findByUsername",authUser.getUsername());
				if(fromAccount.getUserId() == (u.getId())) {
					if(fromAccount.getType().equals("DOLAR") && toAccount.getType().equals("DOLAR")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Float.parseFloat(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							
							
							if(fromAccount.getBankId() != toAccount.getBankId()) {
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());
								newFromAccountBalance = newFromAccountBalance - 1; // EFT
								Float newToAccountBalance = oldToAccountBalance + Float.parseFloat(request.getAmount());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();

							}else {
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

								Float newToAccountBalance = oldToAccountBalance + Float.parseFloat(request.getAmount());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();
							}
							

							moneyTransferResponse.setMessage("Transferred Successfully");
							
							writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
							ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
							producer.send(producerRecord);
							
							
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							String currentTime = Long.toString(timestamp.getTime());
							
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("lastModified",currentTime);
							params.put("accountNumber", fromAccountNumber);
							
							Map<String, Object> params2 = new HashMap<String, Object>();
							params2.put("lastModified",currentTime);
							params2.put("accountNumber", toAccount.getAccountNumber());
							
							
							
							int row1 = session.update("updateLastModified",params);
							int row2 = session.update("updateLastModified",params2);
							
							session.commit();

							return ResponseEntity.ok().body(moneyTransferResponse);  
							
							
						}
						
						
					}else if(fromAccount.getType().equals("DOLAR") && toAccount.getType().equals("TL")) {
						
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {
								HttpResponse<JsonNode> response =  Unirest.get("https://api.collectapi.com/economy/exchange?int="+request.getAmount()+"&to=TRY&base=USD")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								// Parsing the "response"
								JSONObject responsejson = response.getBody().getObject();
								JSONObject result = responsejson.getJSONObject("result");
								org.json.JSONArray data = result.getJSONArray("data");
								JSONObject object = data.getJSONObject(0);
								 
								Float calculated =  Float.parseFloat(Double.toString((double) object.get("calculated")));
								
						
								if(fromAccount.getBankId() != toAccount.getBankId()) {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());
									newFromAccountBalance = newFromAccountBalance - 1; // EFT
									Float newToAccountBalance = oldToAccountBalance + calculated;
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();

								}else {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

									Float newToAccountBalance = oldToAccountBalance + calculated;
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();
								}
								
								moneyTransferResponse.setMessage("Transferred Successfully");
								
								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("lastModified",currentTime);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("lastModified",currentTime);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row1 = session.update("updateLastModified",params);
								int row2 = session.update("updateLastModified",params2);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse);
								

							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
						
						
						
					}else if(fromAccount.getType().equals("TL") && toAccount.getType().equals("DOLAR")) {
						
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {
								HttpResponse<JsonNode> response =  Unirest.get("https://api.collectapi.com/economy/exchange?int="+request.getAmount()+"&to=USD&base=TRY")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								// Parsing the "response"
								JSONObject responsejson = response.getBody().getObject();
								JSONObject result = responsejson.getJSONObject("result");
								org.json.JSONArray data = result.getJSONArray("data");
								JSONObject object = data.getJSONObject(0);
								 
								Float calculated =  Float.parseFloat(Double.toString((double) object.get("calculated")));
								
								
								if(fromAccount.getBankId() != toAccount.getBankId()) {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());
									newFromAccountBalance = newFromAccountBalance - 3; // EFT
									Float newToAccountBalance = oldToAccountBalance + calculated;
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();

								}else {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

									Float newToAccountBalance = oldToAccountBalance + calculated;
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();
								}
								
								moneyTransferResponse.setMessage("Transferred Successfully");
								
								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("lastModified",currentTime);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("lastModified",currentTime);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row1 = session.update("updateLastModified",params);
								int row2 = session.update("updateLastModified",params2);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse); 

							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
						
						
						
					}else if(fromAccount.getType().equals("TL") && toAccount.getType().equals("TL")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Float.parseFloat(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							
							
							if(fromAccount.getBankId() != toAccount.getBankId()) {
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());
								newFromAccountBalance = newFromAccountBalance - 3; // EFT
								Float newToAccountBalance = oldToAccountBalance + Float.parseFloat(request.getAmount());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();

							}else {
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

								Float newToAccountBalance = oldToAccountBalance + Float.parseFloat(request.getAmount());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();
							}
							
							moneyTransferResponse.setMessage("Transferred Successfully");
							
							writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
							ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
							producer.send(producerRecord);
							
							
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							String currentTime = Long.toString(timestamp.getTime());
							
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("lastModified",currentTime);
							params.put("accountNumber", fromAccountNumber);
							
							Map<String, Object> params2 = new HashMap<String, Object>();
							params2.put("lastModified",currentTime);
							params2.put("accountNumber", toAccount.getAccountNumber());
							
							
							
							int row1 = session.update("updateLastModified",params);
							int row2 = session.update("updateLastModified",params2);
							
							session.commit();

							return ResponseEntity.ok().body(moneyTransferResponse); 

							
						}
						
						
						
					}else if(fromAccount.getType().equals("Altın") && toAccount.getType().equals("DOLAR")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {
								
								HttpResponse<JsonNode> response1 =  Unirest.get("https://api.collectapi.com/economy/goldPrice")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								JSONObject responsejson1 = response1.getBody().getObject();
								org.json.JSONArray result1 = responsejson1.getJSONArray("result");
								JSONObject object1 = result1.getJSONObject(0);
								
								Float goldPriceTRY =  Float.parseFloat(Double.toString((double) object1.get("buying"))) * Float.parseFloat(request.getAmount());
								String goldPriceTRYInString = Float.toString(goldPriceTRY);
								
								HttpResponse<JsonNode> response =  Unirest.get("https://api.collectapi.com/economy/exchange?int="+goldPriceTRYInString+"&to=USD&base=TRY")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								// Parsing the "response"
								JSONObject responsejson = response.getBody().getObject();
								JSONObject result = responsejson.getJSONObject("result");
								org.json.JSONArray data = result.getJSONArray("data");
								JSONObject object = data.getJSONObject(0);
								 
								Float calculated =  Float.parseFloat(Double.toString((double) object.get("calculated")));
								
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

								Float newToAccountBalance = oldToAccountBalance + calculated;
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();
								
								if(row1 == 1 && row2 == 1) {
									moneyTransferResponse.setMessage("Transferred Successfully");
								}

								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params3 = new HashMap<String, Object>();
								params3.put("lastModified",currentTime);
								params3.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params4 = new HashMap<String, Object>();
								params4.put("lastModified",currentTime);
								params4.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row3 = session.update("updateLastModified",params3);
								int row4 = session.update("updateLastModified",params4);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse); 


							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
						
						
						
						
						
					}else if(fromAccount.getType().equals("Altın") && toAccount.getType().equals("TL")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {
								
								HttpResponse<JsonNode> response1 =  Unirest.get("https://api.collectapi.com/economy/goldPrice")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								JSONObject responsejson1 = response1.getBody().getObject();
								org.json.JSONArray result1 = responsejson1.getJSONArray("result");
								JSONObject object1 = result1.getJSONObject(0);
								
								Float goldPriceTRY =  ((Float.parseFloat(Double.toString((double) object1.get("buying")))) * Float.parseFloat(request.getAmount()));
								String goldPriceTRYInString = Float.toString(goldPriceTRY);
							
								
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

								Float newToAccountBalance = oldToAccountBalance + goldPriceTRY;
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();
								
								
								if(row1 == 1 && row2 == 1) {
									moneyTransferResponse.setMessage("Transferred Successfully");
								}

								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params3 = new HashMap<String, Object>();
								params3.put("lastModified",currentTime);
								params3.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params4 = new HashMap<String, Object>();
								params4.put("lastModified",currentTime);
								params4.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row3 = session.update("updateLastModified",params3);
								int row4 = session.update("updateLastModified",params4);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse); 


							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
						
						
						
					}else if(fromAccount.getType().equals("Altın") && toAccount.getType().equals("Altın")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {				
								
								Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

								Float newToAccountBalance = oldToAccountBalance + Float.parseFloat(request.getAmount());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("balance", newFromAccountBalance);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("balance", newToAccountBalance);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								int row1 = session.update("updateBalance", params);
								int row2 = session.update("updateBalance", params2);
								
								session.commit();
								
								
								if(row1 == 1 && row2 == 1) {
									moneyTransferResponse.setMessage("Transferred Successfully");
								}

								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params3 = new HashMap<String, Object>();
								params3.put("lastModified",currentTime);
								params3.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params4 = new HashMap<String, Object>();
								params4.put("lastModified",currentTime);
								params4.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row3 = session.update("updateLastModified",params3);
								int row4 = session.update("updateLastModified",params4);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse); 


							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
						
						
						
					}else if(fromAccount.getType().equals("TL") && toAccount.getType().equals("Altın")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {
								
								HttpResponse<JsonNode> response1 =  Unirest.get("https://api.collectapi.com/economy/goldPrice")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								JSONObject responsejson1 = response1.getBody().getObject();
								org.json.JSONArray result1 = responsejson1.getJSONArray("result");
								JSONObject object1 = result1.getJSONObject(0);
								
								Float goldPriceTRY =  Float.parseFloat(Double.toString((double) object1.get("buying"))) * Float.parseFloat(request.getAmount());
								
								if(fromAccount.getBankId() != toAccount.getBankId()) {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());
									newFromAccountBalance = newFromAccountBalance - 3; // EFT
									Float newToAccountBalance = oldToAccountBalance + goldPriceTRY;
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();

								}else {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

									Float newToAccountBalance = oldToAccountBalance + goldPriceTRY;
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();
								}
								
								moneyTransferResponse.setMessage("Transferred Successfully");
								
								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("lastModified",currentTime);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("lastModified",currentTime);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row1 = session.update("updateLastModified",params);
								int row2 = session.update("updateLastModified",params2);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse); 


							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
						
						
						
					}else if(fromAccount.getType().equals("DOLAR") && toAccount.getType().equals("Altın")) {
						Float oldFromAccountBalance = fromAccount.getBalance();
						Float oldToAccountBalance = toAccount.getBalance();
						
						if(Long.parseLong(request.getAmount()) > oldFromAccountBalance) {
							moneyTransferResponse.setMessage("Insufficient Balance");
							return ResponseEntity.badRequest().body(moneyTransferResponse);
						}else {
							try {
								
								HttpResponse<JsonNode> response1 =  Unirest.get("https://api.collectapi.com/economy/goldPrice")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								JSONObject responsejson1 = response1.getBody().getObject();
								org.json.JSONArray result1 = responsejson1.getJSONArray("result");
								JSONObject object1 = result1.getJSONObject(0);
								
								Float goldPriceTRY =  Float.parseFloat(Double.toString((double) object1.get("buying"))) * Float.parseFloat(request.getAmount());
								String goldPriceTRYInString = Float.toString(goldPriceTRY);
								
								HttpResponse<JsonNode> response =  Unirest.get("https://api.collectapi.com/economy/exchange?int="+goldPriceTRYInString+"&to=USD&base=TRY")
										  .header("content-type", "application/json")
										  .header("authorization", "apikey 1HsyxyFajXCgMXrvgZOo6t:215vHnXoOm34zQoezFbax3")
										  .asJson();
								
								// Parsing the "response"
								JSONObject responsejson = response.getBody().getObject();
								JSONObject result = responsejson.getJSONObject("result");
								org.json.JSONArray data = result.getJSONArray("data");
								JSONObject object = data.getJSONObject(0);
								 
								Float goldPriceUSD =  Float.parseFloat(Double.toString((double) object.get("calculated")));
								
								
								if(fromAccount.getBankId() != toAccount.getBankId()) {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());
									newFromAccountBalance = newFromAccountBalance - 1; // EFT
									Float newToAccountBalance = oldToAccountBalance + (Float.parseFloat(request.getAmount()) / goldPriceUSD);
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();

								}else {
									Float newFromAccountBalance = oldFromAccountBalance - Float.parseFloat(request.getAmount());

									Float newToAccountBalance = oldToAccountBalance + (Float.parseFloat(request.getAmount()) / goldPriceUSD);
									
									Map<String, Object> params = new HashMap<String, Object>();
									params.put("balance", newFromAccountBalance);
									params.put("accountNumber", fromAccountNumber);
									
									Map<String, Object> params2 = new HashMap<String, Object>();
									params2.put("balance", newToAccountBalance);
									params2.put("accountNumber", toAccount.getAccountNumber());
									
									int row1 = session.update("updateBalance", params);
									int row2 = session.update("updateBalance", params2);
									
									session.commit();
								}
								
								moneyTransferResponse.setMessage("Transferred Successfully");
								
								writedTxtToDb = request.getAmount() + ", " + fromAccountNumber + " to " + toAccount.getAccountNumber()+": transferred";
								ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToDb);
								producer.send(producerRecord);
								
								
								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								String currentTime = Long.toString(timestamp.getTime());
								
								Map<String, Object> params = new HashMap<String, Object>();
								params.put("lastModified",currentTime);
								params.put("accountNumber", fromAccountNumber);
								
								Map<String, Object> params2 = new HashMap<String, Object>();
								params2.put("lastModified",currentTime);
								params2.put("accountNumber", toAccount.getAccountNumber());
								
								
								
								int row1 = session.update("updateLastModified",params);
								int row2 = session.update("updateLastModified",params2);
								
								session.commit();

								return ResponseEntity.ok().body(moneyTransferResponse); 


							}catch (Exception e) {
								System.out.println(e);
								return ResponseEntity.internalServerError().body(moneyTransferResponse);
							}
						}
					}
					
				}else {
					MoneyTransferResponse rsp = new MoneyTransferResponse();
					rsp.setMessage("Unauthorized access");
					ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rsp);
				}
				
			}
		    
		    
		}catch (Exception e) {
			e.printStackTrace();
			ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
		}
		return null;

	}


	public ResponseEntity<?> removeAccount(String id) {
		
		Reader reader;
        
		try {
		    //read configuration and create session factory
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			Account account = session.selectOne("findByAccountNumber",id);
			
			AccountDelete accountDelete = new AccountDelete();
			try {
				if(account != null) {
					
					 //User authUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
					 User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
					 com.example.cemozan.bankingsystemproject.Models.User u = session.selectOne("findByUsername",authUser.getUsername());
						if(account.getUserId() == (u.getId())) {
							
							int row1 = session.update("deleteAccount", id);
	
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							String currentTime = Long.toString(timestamp.getTime());
						
							Map<String, Object> params = new HashMap<String, Object>();
							params.put("lastModified",currentTime);
							params.put("accountNumber", id);
							
							int row2 = session.update("updateLastModified", params);
							session.commit();
							
							if(row1 == 1 && row2 == 1) {
								accountDelete.setStatus(true);
								accountDelete.setMessage("Account Deleted");
								return ResponseEntity.ok()
										.lastModified(Long.parseLong(currentTime))
										.body(accountDelete);
							}else {
								accountDelete.setStatus(false);
								accountDelete.setMessage("Bad Request");
								return ResponseEntity.badRequest().body(accountDelete);
							}
							
						}else {
							MoneyTransferResponse rsp = new MoneyTransferResponse();
							rsp.setMessage("Unauthorized access");
							return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rsp);
						}			
				}else {
					accountDelete.setStatus(false);
					accountDelete.setMessage("Bad Request");
					return ResponseEntity.badRequest().body(accountDelete);
				}
				
			}catch (Exception e) {
				accountDelete.setStatus(false);
				accountDelete.setMessage("Internal Server Error");
				return ResponseEntity.internalServerError().body(accountDelete);
			}
		}catch (Exception e) {
			e.printStackTrace();
			
			return ResponseEntity.internalServerError().body(null);
		}
		
		
		
		
		
	}
	
	
	static boolean isWordPresent(String sentence, String word)
	{
	    // To break the sentence in words
	    String []s = sentence.split(" ");
	 
	    // To temporarily store each individual word
	    for ( String temp :s)
	    {
	 
	        // Comparing the current word
	        // with the word to be searched
	        if (temp.compareTo(word) == 0)
	        {
	            return true;
	        }
	    }
	    return false;
	}


	public ResponseEntity<?> createBank(CreateBankRequest request) {
		Reader reader;
		 //read configuration and create session factory
		try {
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			Bank bank = session.selectOne("findByBankName",request.getName());

			
			if(bank == null) {
				
				//SqlSession session1 = sqlSessionFactory.openSession();
				Bank b = new Bank();
				b.setName(request.getName());
				session.insert("createBank", b);
				session.commit();
				
				CreateBankSuccessResponse createBankSuccessResponse = new CreateBankSuccessResponse();
				createBankSuccessResponse.setSuccess(true);
				createBankSuccessResponse.setMessage("Created Successfully");
				createBankSuccessResponse.setBankDetails("Name:"+request.getName());
				
				
				return ResponseEntity.created(null).body(createBankSuccessResponse);
				
		
			}else {
				CreateBankFailedResponse createBankFailedResponse = new CreateBankFailedResponse();
				createBankFailedResponse.setSuccess(false);
				createBankFailedResponse.setMessage("Given name Already Used : "+ request.getName());
				return  ResponseEntity.unprocessableEntity().body(createBankFailedResponse);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}
	}

	public ResponseEntity<?> register(@RequestBody UserRegisterRequest request){
		Reader reader;
		 //read configuration and create session factory
		try {
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			com.example.cemozan.bankingsystemproject.Models.User user = session.selectOne("findByUsername",request.getUsername());
			
			
			if(user == null) {
				
				com.example.cemozan.bankingsystemproject.Models.User userr = session.selectOne("findByEmail",request.getEmail());
				
				if(userr == null) {
					
					com.example.cemozan.bankingsystemproject.Models.User u = new com.example.cemozan.bankingsystemproject.Models.User();
					u.setUsername(request.getUsername());
					u.setPassword(passwordEncoder.encode(request.getPassword()));
					u.setEmail(request.getEmail());
					u.setAuthorities("CREATE_ACCOUNT");
					session.insert("registerUser", u);
					session.commit();
					
					
					com.example.cemozan.bankingsystemproject.Models.User u2 = session.selectOne("findByUsername",request.getUsername());
					
					RegisterSuccesResponse registerSuccesResponse = new RegisterSuccesResponse();
					registerSuccesResponse.setSuccess(true);
					registerSuccesResponse.setMessage("Created Successfully");
					registerSuccesResponse.setUserDetails("Username:"+u2.getUsername()+", Password: "+u2.getPassword()+", Email: "+u2.getEmail()+", Enabled: "+u2.getEnabled());
					
					
					
					return ResponseEntity.created(null).body(registerSuccesResponse);
				}else {
					RegisterFailedResponse registerFailedResponse = new RegisterFailedResponse();
					registerFailedResponse.setSuccess(false);
					registerFailedResponse.setMessage("Given email Already Used : "+ request.getEmail());
					return  ResponseEntity.unprocessableEntity().body(registerFailedResponse);
				}
				
				
		
			}else {
				
				RegisterFailedResponse registerFailedResponse = new RegisterFailedResponse();
				registerFailedResponse.setSuccess(false);
				registerFailedResponse.setMessage("Given username Already Used : "+ request.getUsername());
				return  ResponseEntity.unprocessableEntity().body(registerFailedResponse);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body(null);
		}
	}


	
	public ResponseEntity<?> enableDisableUser(String id, EnableRequest request) {
		Reader reader;
		EnableResponseMessage enableResponseMessage = new EnableResponseMessage();
		 //read configuration and create session factory
		try {
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			com.example.cemozan.bankingsystemproject.Models.User user = session.selectOne("findById",id);
			
			if(user != null) {
				
				
				if(request.getEnabled() == false) {
					// update object name
					user.setEnabled(0);
					//send object as parameter
					session.update("updateUserEnabled", user);
					session.commit();
					enableResponseMessage.setStatus("success");
					enableResponseMessage.setMessage("User disabled");
					return ResponseEntity.ok().body(enableResponseMessage);
				}else {
					// update object name
					user.setEnabled(1);
					//send object as parameter
					session.update("updateUserEnabled", user);
					session.commit();
					enableResponseMessage.setStatus("success");
					enableResponseMessage.setMessage("User enabled");
					return ResponseEntity.ok().body(enableResponseMessage);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public ResponseEntity<?> login(LoginRequest request) {
		try {
			Reader reader;
			 //read configuration and create session factory
			try {
				reader = Resources.getResourceAsReader("myBatis_conf.xml");
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
				// open session, session is connection between database and us(like JDBC connection)
				SqlSession session = sqlSessionFactory.openSession();
				// call query from mapper.xml files with defined id
				com.example.cemozan.bankingsystemproject.Models.User user = session.selectOne("findByUsername",request.getUsername());
				
				
				if (user.getEnabled() == 0) {
					return ((BodyBuilder) ResponseEntity.notFound()).body("User not found");
				}else {
					authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
				}
				
				
			}catch (Exception e) {
				e.printStackTrace();
				return ((BodyBuilder) ResponseEntity.notFound()).body("User not found");
			}
			
			
		} catch (BadCredentialsException e) {
			return ResponseEntity.badRequest().body("Bad credentials");
		} catch (DisabledException e) {
		}
		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(request.getUsername());

		final String token = jwtTokenUtil.generateToken(userDetails);
		LoginResponse resp = new LoginResponse();
		resp.setStatus("success");
		resp.setToken(token);
        return ResponseEntity.ok().body(resp);
	}


	
	public ResponseEntity<?> getAllAccounts() {
		Reader reader;
		 //read configuration and create session factory
		try {
			reader = Resources.getResourceAsReader("myBatis_conf.xml");
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			// open session, session is connection between database and us(like JDBC connection)
			SqlSession session = sqlSessionFactory.openSession();
			// call query from mapper.xml files with defined id
			
			
			User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			
			com.example.cemozan.bankingsystemproject.Models.User u = session.selectOne("findByUsername",authUser.getUsername());
			
			List<Account> accounts = session.selectList("findAllAccountsByUserId",u.getId());
			
			if (u.getUsername().equals(authUser.getUsername())) {
				return ResponseEntity.ok().body(accounts);
			}else {
				UnauthorizedAccessResponse unauthorizedAccessResponse = new UnauthorizedAccessResponse();
				unauthorizedAccessResponse.setMessage("Unauthorized Access");
				unauthorizedAccessResponse.setSuccess(false);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedAccessResponse);
			}
					
		}catch (Exception e) {
			e.printStackTrace();
			NoAccountFoundResponse noAccountFoundResponse = new NoAccountFoundResponse();
			noAccountFoundResponse.setMesssage("No Account Found");
			noAccountFoundResponse.setSuccess(false);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(noAccountFoundResponse);
		}
	}

	
	
}
