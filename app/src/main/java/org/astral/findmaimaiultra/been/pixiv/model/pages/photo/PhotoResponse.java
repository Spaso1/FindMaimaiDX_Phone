package org.astral.findmaimaiultra.been.pixiv.model.pages.photo;

import java.util.List;

public class PhotoResponse {
    private boolean error;
    private String message;
    private List<Photo> body;

    // Getters and Setters
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Photo> getBody() {
        return body;
    }

    public void setBody(List<Photo> body) {
        this.body = body;
    }
}
