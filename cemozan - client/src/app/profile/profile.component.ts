import { Component } from '@angular/core';
import { AuthService } from '../_services/auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { tokenName } from '@angular/compiler';


@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})

export class ProfileComponent {
  form: any = {
    id: null,
  };

  isDataReceived = false;
  centroData: Array<any> = [];

  constructor(private http: HttpClient){}
  
  token = window.sessionStorage.getItem('auth-user');
  objectToken = JSON.parse(this.token == null ? "yok":this.token)

  authorizationToken = 'Bearer ' +  this.objectToken.token; 
  
  onSubmit(){
    

    

    let url = "http://localhost:8080/allaccounts";
    
    fetch(url,{headers:{'Authorization': this.authorizationToken}})
    .then(data => {
      return data.json();})
    .then(post => {
      this.centroData = post;
      this.isDataReceived = true;
    });
  }

  
}

  

