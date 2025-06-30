package com.interswitch.middleware.configs;

import com.interswitch.middleware.params.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
public class ExceptionAdvise {

    private static final Logger logger = Logger.getLogger(ExceptionAdvise.class.getName());

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentException(MethodArgumentNotValidException exception) {
        ArrayList<Object> errors = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            HashMap<String, String> error = new HashMap<>();
            error.put(fieldError.getField(), fieldError.getDefaultMessage());
            errors.add(error);
        }

        ApiResponse<?> response = new ApiResponse<>(ApiResponse.ERROR_CODE, "Invalid request parameters");
        response.setErrors(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(value = Exception.class)
//    public ResponseEntity<?> handleException(Exception exception) {
//        logger.log(Level.SEVERE, exception.getMessage(), exception);
//        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//        if (exception instanceof HttpStatusCodeException) {
//            httpStatus = (HttpStatus) ((HttpStatusCodeException) exception).getStatusCode();
//        }
//
//        if (exception instanceof MethodArgumentNotValidException) {
//            httpStatus = HttpStatus.BAD_REQUEST;
//        }
//
//        ApiResponse<?> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
//        return new ResponseEntity<>(response, httpStatus);
//    }

}

