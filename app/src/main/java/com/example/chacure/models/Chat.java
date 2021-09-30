package com.example.chacure.models;

import java.util.ArrayList;

public class Chat {

    private String idChat;
    private String idUser1;
    private String idUser2;
    private int idNotification;
    private boolean isWriting;
    private long timestamp;
    private ArrayList<String> ids;

    public Chat(){}

    public Chat(String idChat, String idUser1, String idUser2, int idNotification, boolean isWriting, long timestamp, ArrayList<String> ids) {
        this.idChat = idChat;
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
        this.idNotification = idNotification;
        this.isWriting = isWriting;
        this.timestamp = timestamp;
        this.ids = ids;
    }

    public boolean isWriting() {
        return isWriting;
    }

    public void setWriting(boolean writing) {
        isWriting = writing;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdUser1() {
        return idUser1;
    }

    public void setIdUser1(String idUser1) {
        this.idUser1 = idUser1;
    }

    public String getIdUser2() {
        return idUser2;
    }

    public void setIdUser2(String idUser2) {
        this.idUser2 = idUser2;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }

    public ArrayList<String> getIds() {
        return ids;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }
}
