package com.interswitch.middleware.params;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 10;
    public static final String GENERAL_SUCCESS_MESSAGE = "Operation successfully";
    public static final String GENERAL_ERROR_MESSAGE = "Operation failed";

    private T data;
    private Object errors;
    private String message;
    private int statusCode;

    public ApiResponse(int statusCode, String message) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public boolean isSuccessful() {
        return statusCode == SUCCESS_CODE;
    }


    public static <T> ApiResponse<T> Success(T data) {
        ApiResponse<T> response = new ApiResponse<>(SUCCESS_CODE, GENERAL_SUCCESS_MESSAGE);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> Error() {
        ApiResponse<T> response = new ApiResponse<>(ERROR_CODE, GENERAL_ERROR_MESSAGE);
        return response;
    }

    public static <T> ApiResponse<T> Error(String message) {
        ApiResponse<T> response = new ApiResponse<>(ERROR_CODE, message);
        return response;
    }
}
