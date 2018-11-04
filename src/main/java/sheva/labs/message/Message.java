package sheva.labs.message;


import sheva.labs.constants.MessageConstants;

import java.io.Serializable;


public class Message implements Serializable, MessageConstants {

    private final MessageType type;
    private final String data;

    public Message(MessageType type) {
        this.type = type;
        this.data = EMPTY;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
