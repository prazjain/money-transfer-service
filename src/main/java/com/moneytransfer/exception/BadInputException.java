package com.moneytransfer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class BadInputException extends RuntimeException {
	public BadInputException() { }
	public BadInputException(String reason) { super(reason); }
}

