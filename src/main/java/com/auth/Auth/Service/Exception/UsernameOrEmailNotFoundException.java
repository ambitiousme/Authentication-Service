package com.auth.Auth.Service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsernameOrEmailNotFoundException  extends RuntimeException{

    public UsernameOrEmailNotFoundException (String message)
    {
        super(message);
    }

}
