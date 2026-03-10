package com.auth.Auth.Service.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistException extends RuntimeException{

    public UserAlreadyExistException (String message)
    {
        super(message);
    }

}
