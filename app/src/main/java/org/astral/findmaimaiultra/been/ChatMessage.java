package org.astral.findmaimaiultra.been;

public class ChatMessage {
    private String message;
    private String time;
    private boolean isUser;
    private boolean isThinking;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.isThinking = false;
    }
    public ChatMessage(String message, boolean isUser,String time) {
        this.message = message;
        this.isUser = isUser;
        this.isThinking = false;
        this.time = time;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setThinking(boolean thinking) {
        isThinking = thinking;
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
