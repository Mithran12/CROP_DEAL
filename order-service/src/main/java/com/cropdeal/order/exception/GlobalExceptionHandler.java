package com.cropdeal.order.exception;

import java.time.LocalDateTime;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.cropdeal.order.dto.response.ErrorResponseDto;

import com.cropdeal.order.exception.ExternalServiceException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception,
			WebRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto();

		errorResponse.setTimestamp(LocalDateTime.now());

		errorResponse.setStatus(HttpStatus.NOT_FOUND.value());

		errorResponse.setError(HttpStatus.NOT_FOUND.getReasonPhrase());

		errorResponse.setMessage(exception.getMessage());

		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponseDto> handleBusinessException(BusinessException exception, WebRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto();

		errorResponse.setTimestamp(LocalDateTime.now());

		errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());

		errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

		errorResponse.setMessage(exception.getMessage());

		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(ExternalServiceException.class)
	public ResponseEntity<ErrorResponseDto> handleExternalServiceException(ExternalServiceException exception,
			WebRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto();

		errorResponse.setTimestamp(LocalDateTime.now());

		errorResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

		errorResponse.setError(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());

		errorResponse.setMessage(exception.getMessage());

		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
	}

	@ExceptionHandler(UnauthorizedResourceException.class)
	public ResponseEntity<ErrorResponseDto> handleUnauthorizedResourceException(UnauthorizedResourceException exception,
			WebRequest request) {

		ErrorResponseDto errorResponse = new ErrorResponseDto();

		errorResponse.setTimestamp(LocalDateTime.now());

		errorResponse.setStatus(HttpStatus.FORBIDDEN.value());

		errorResponse.setError(HttpStatus.FORBIDDEN.getReasonPhrase());

		errorResponse.setMessage(exception.getMessage());

		errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationException(
	        MethodArgumentNotValidException exception,
	        WebRequest request) {

	    ErrorResponseDto errorResponse = new ErrorResponseDto();

	    String message = exception.getBindingResult()
	            .getFieldErrors()
	            .stream()
	            .map(error -> error.getField() + ": " + error.getDefaultMessage())
	            .findFirst()
	            .orElse("Validation failed");

	    errorResponse.setTimestamp(LocalDateTime.now());
	    errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
	    errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
	    errorResponse.setMessage(message);
	    errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(errorResponse);
	}
}