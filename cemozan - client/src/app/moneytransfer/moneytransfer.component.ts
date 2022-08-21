import { Component } from '@angular/core';
import { AuthService } from '../_services/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { tokenName } from '@angular/compiler';


@Component({
  selector: 'app-profile',
  templateUrl: './moneytransfer.component.html',
  styleUrls: ['./moneytransfer.component.css']
})

export class MoneytransferComponent {
  form: any = {
    transferredAccountNumber: null,
    amount: null,
    fromAccountNumber:null
  };

  isTransferSuccessful = false;
  
  token = window.sessionStorage.getItem('auth-user');
  objectToken = JSON.parse(this.token == null ? "yok":this.token)

  authorizationToken = 'Bearer ' +  this.objectToken.token; 
  
  onSubmit(){

    console.log(this.authorizationToken);

    const { transferredAccountNumber, amount,fromAccountNumber} = this.form;

    let url = "http://localhost:8080/accounts/"+fromAccountNumber;
    
    fetch(url, { headers:{'Authorization': this.authorizationToken, 'Content-Type':'application/json'}, method:'PUT', body:JSON.stringify({transferredAccountNumber: transferredAccountNumber, amount: amount}) })
    .then(data => {
      console.log(data);
      return data.json();})
    .then(post => {
      console.log(post)
      this.isTransferSuccessful = true;
    });
  }

  
}

  

