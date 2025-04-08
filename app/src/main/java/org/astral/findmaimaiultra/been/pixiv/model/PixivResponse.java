package org.astral.findmaimaiultra.been.pixiv.model;

public class PixivResponse {
    public boolean error;
    public Body body;


    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

}