package com.ss.utopia.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason = "Email was not sent by AWS")
public class EmailNotSentException extends RuntimeException{

}
