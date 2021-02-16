package com.company.Classes;

import com.company.Clients.ClientThread;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;

public class Message {

    /*
    messages table in MySQL database columns:
                1. uid          int PK          (message uid)
                2. content      longtext        (content of the message)
                3. to           int             (user's uid)
                4. from         int             (user's uid)
                5. timestamp    timestamp       (date and time the message sent on)
                6. room         int             (room's uid)
     */

    /*
    Message class in Android project contains:
                1. uid          int
                2. to           SmallUser
                3. from         SmallUser
                4. content      String
                4. timestamp    java.util.Date.Timestamp
                5. isItForMeMe  boolean
     */

    private int uid;
    private User to, from;
    private String content;
    private Timestamp timestamp;
    private boolean isItMe;

    public Message(int uid, String content, Timestamp timestamp, User to, User from) {
        this.uid = uid;
        this.content = content;
        this.timestamp = timestamp;
        this.to = to;
        this.from = from;
    }

    public Message(InputStream inputStream) throws IOException {
        Message jsonMessage = getMessageFromJson(ClientThread.readStringFromInptStrm(inputStream));
        this.uid = jsonMessage.getUid();
        this.content = jsonMessage.getContent();
        this.timestamp = jsonMessage.getTimestamp();
        this.to = jsonMessage.getTo();
        this.from = jsonMessage.getFrom();
    }

    /**
     * Get Message object from Json String.
     * @param json
     * Json String.
     * @return
     * Message Object.
     */
    public static Message getMessageFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, Message.class);
    }

    /**
     * Get Classes.Message object by UID. This function communicates with the DB.
     * @param uid
     * Classes.Message UID int.
     * @return
     * Classes.Message object referred by the UID provided.
     */
    public static Message getMessageByUID(int uid){
        Message message = null;
        try (Connection conn = DBConnection.getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users WHERE uid=?")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    message = new Message(
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getTimestamp(3),
                            User.getUserByUID(resultSet.getInt(4)),
                            User.getUserByUID(resultSet.getInt(5)));
                }catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Write Json object to OutputStream.
     * @param outputStream
     * OutputStream to write to.
     */
    public void write(OutputStream outputStream) throws IOException {

        byte[] bytes = this.toString().getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    /**
     * Override toString function to create a json object.
     * @return
     * String: json object of Class Message.
     */
    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        return builder.create().toJson(this);
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isItMe() {
        return isItMe;
    }

    public void setItMe(boolean itMe) {
        isItMe = itMe;
    }
}
