package de.androidcrypto.firebaseplayground.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Message2Model {
    @Exclude
    public String key;
    String message;
    String senderId;
    long messageTime;
    String messageTimeString;
    boolean messageEncrypted;

    public Message2Model() {

    }

    public Message2Model(String senderId, String message, long messageTime, String messageTimeString, boolean messageEncrypted) {
        this.senderId = senderId;
        this.message = message;
        this.messageTime = messageTime;
        this.messageTimeString = messageTimeString;
        this.messageEncrypted = messageEncrypted;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sender", senderId);
        result.put("message", message);
        result.put("messageTime", messageTime);
        result.put("messageTimeString", messageTimeString);
        result.put("messageEncrypted", messageEncrypted);
        return result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getMessageTimeString() { return messageTimeString; }

    public void setMessageTimeString(String messageTimeString) { this.messageTimeString = messageTimeString; }

    public boolean isMessageEncrypted() {
        return messageEncrypted;
    }

    public void setMessageEncrypted(boolean messageEncrypted) {
        this.messageEncrypted = messageEncrypted;
    }
}
