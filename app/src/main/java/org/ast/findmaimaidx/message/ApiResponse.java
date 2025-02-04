package org.ast.findmaimaidx.message;

public class ApiResponse {
    private int status;
    private String message;
    private long timestamp;
    public ApiResponse() {
    }
    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(String uid) {
        this.status = 200;
        this.message = uid;
        this.timestamp = System.currentTimeMillis();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
