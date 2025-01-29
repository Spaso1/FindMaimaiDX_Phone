package org.ast.findmaimaidx.been;
// RequestData.java
import java.util.List;

public class RequestData {
    private String model;
    private List<Message> messages;

    public RequestData(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

