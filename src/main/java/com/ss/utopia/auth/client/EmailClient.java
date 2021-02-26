package com.ss.utopia.auth.client;

import com.ss.utopia.auth.dto.EmailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmailClient {


  public ResponseEntity<String> sendForgetPasswordEmail(String token, String email){

    //Will replace with lambda and API gateway
    String mailUrl = "https://c0uenga9m6.execute-api.us-east-1.amazonaws.com/beta";

    //Set email Dto
    EmailDto emailToSend = new EmailDto();
    emailToSend.setEmail(email);
    emailToSend.setSubject("Reset Utopia password");
    String customerUrl = "http://localhost:4200/login/password/resetform/" + token;
    String content =
        "<h1>Utopia Password Change</h1>" +
            "<span>Please use this link to change your password (Link will expire in one day) </span>" +
            "<h2><a href='" + customerUrl + "'>Change password</a></h2>" +
            "<h3><span>Thanks</span></h3>" +
            "<h3>The Utopia team</h3>";
    emailToSend.setContent(content);

    //Send response
    RestTemplate emailRestTemplate = new RestTemplate();
    return emailRestTemplate.postForEntity(mailUrl, emailToSend, String.class);
  }
}