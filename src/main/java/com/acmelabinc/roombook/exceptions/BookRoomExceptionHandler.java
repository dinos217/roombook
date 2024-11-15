package com.acmelabinc.roombook.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class BookRoomExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<ApiExceptionMessage> processValidationError(NotFoundException e) {

        logger.error("API MESSAGE: {}", e.getMessage());

        ApiExceptionMessage response = buildApiExceptionMessage(e, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ApiExceptionMessage> processValidationError(BadRequestException e) {

        logger.error("API MESSAGE: {}", e.getMessage());

        ApiExceptionMessage response = buildApiExceptionMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<ApiExceptionMessage> processValidationError(AlreadyExistsException e) {

        logger.error("API MESSAGE: {}", e.getMessage());

        ApiExceptionMessage response = buildApiExceptionMessage(e, HttpStatus.CONFLICT);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    private ApiExceptionMessage buildApiExceptionMessage(RuntimeException e, HttpStatus status) {
        ApiExceptionMessage response = new ApiExceptionMessage();
        response.setStatus(status);
        response.setMessage(e.getMessage());
        return response;
    }
}
