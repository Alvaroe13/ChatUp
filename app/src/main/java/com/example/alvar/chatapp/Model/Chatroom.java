package com.example.alvar.chatapp.Model;

public class Chatroom {

    private String member1ID;
    private String member2ID;

    public Chatroom() {
    }

    public Chatroom(String member1ID, String member2ID) {
        this.member1ID = member1ID;
        this.member2ID = member2ID;
    }

    public String getMember1ID() {
        return member1ID;
    }

    public void setMember1ID(String member1ID) {
        this.member1ID = member1ID;
    }

    public String getMember2ID() {
        return member2ID;
    }

    public void setMember2ID(String member2ID) {
        this.member2ID = member2ID;
    }
}
