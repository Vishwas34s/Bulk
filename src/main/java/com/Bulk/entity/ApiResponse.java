package com.Bulk.entity;
public class ApiResponse {

    private String status;  // "success" or "error"
    private String message;
    private Object data; // Optional field for additional data

    // Constructor for success response
    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Constructor for success response with data
    public ApiResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
