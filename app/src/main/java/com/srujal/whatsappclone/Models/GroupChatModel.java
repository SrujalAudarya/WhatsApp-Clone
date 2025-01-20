package com.srujal.whatsappclone.Models;

public class GroupChatModel {
        String uId, message, senderName;
        Long timeStamp;

        public GroupChatModel(Long timeStamp, String message, String uId, String senderName) {
            this.timeStamp = timeStamp;
            this.message = message;
            this.uId = uId;
            this.senderName = senderName;
        }

        public GroupChatModel(String uId, String message, String senderName) {
            this.uId = uId;
            this.message = message;
            this.senderName = senderName;
        }

        public GroupChatModel() {}

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

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
    }
