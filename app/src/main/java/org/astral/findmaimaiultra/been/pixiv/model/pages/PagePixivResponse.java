package org.astral.findmaimaiultra.been.pixiv.model.pages;

public class PagePixivResponse {
    private boolean error;
    private String message;
    private PageBody body;

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

    public PageBody getBody() {
        return body;
    }

    public void setBody(PageBody body) {
        this.body = body;
    }
}
