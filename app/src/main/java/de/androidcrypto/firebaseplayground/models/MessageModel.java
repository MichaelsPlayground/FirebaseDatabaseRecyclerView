package de.androidcrypto.firebaseplayground.models;

import java.util.HashMap;
import java.util.Map;

public class MessageModel {

    String message;
    String senderId;
    long messageTime;
    boolean messageEncrypted;

    public MessageModel() {

    }

    public MessageModel(String senderId, String message, long messageTime, boolean messageEncrypted) {
        this.senderId = senderId;
        this.message = message;
        this.messageTime = messageTime;
        this.messageEncrypted = messageEncrypted;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sender", senderId);
        result.put("message", message);
        result.put("messageTime", messageTime);
        result.put("messageEncrypted", messageEncrypted);
        return result;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public boolean isMessageEncrypted() {
        return messageEncrypted;
    }

    public void setMessageEncrypted(boolean messageEncrypted) {
        this.messageEncrypted = messageEncrypted;
    }
}
