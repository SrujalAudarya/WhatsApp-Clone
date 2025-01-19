package com.srujal.whatsappclone.Models;

public class Messages {

    String uId, message;
    Long timeStamp;

    public Messages(Long timeStamp, String message, String uId) {
        this.timeStamp = timeStamp;
        this.message = message;
        this.uId = uId;
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
