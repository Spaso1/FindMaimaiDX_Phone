package org.ast.findmaimaidx.been;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private boolean isThinking;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.isThinking = false;
    }

    public ChatMessage(String message, boolean isUser, boolean isThinking) {
        this.message = message;
        this.isUser = isUser;
        this.isThinking = isThinking;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUser() {
        return isUser;
    }

    public boolean isThinking() {
        return isThinking;
    }
}
