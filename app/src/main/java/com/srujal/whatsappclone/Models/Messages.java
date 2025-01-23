package com.srujal.whatsappclone.Models;

public class Messages {

    String uId, message, messageId;
    Long timeStamp;

    public Messages(Long timeStamp, String message, String uId) {
        this.timeStamp = timeStamp;
        this.message = message;
        this.uId = uId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Messages(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public Messages(){}

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
