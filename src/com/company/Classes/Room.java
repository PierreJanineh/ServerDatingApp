package com.company.Classes;

import com.company.Clients.ClientThread;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.company.Classes.DBConnection.*;

public class Room {
    public static final String CHATROOMS = "chatrooms";

    /*
    rooms table in MySQL database columns:
                1. uid              int PK      (room uid)
                2. seenby           longtext    (json array of users' uids)
                3. messages         longtext    (json array of messages' uids)
                4. recipients       longtext    (json array of users' uids)
                5. lastmessage      longtext    (last message content)

     */

    /*
    Room class in Android project contains:
                  1. uid            int
                  2. seenBy         ArrayList<Integer>
                  3. messages       ArrayList<Integer>
                  4. recipients     ArrayList<Integer>
                  5. lastMessage    Message
     */

    private int uid;
    private ArrayList<Integer> seenBy;
    private ArrayList<Integer> messages; //uids
    private ArrayList<Integer> recipients; //uids
    private Message lastMessage;

    public Room(int uid, ArrayList<Integer> seenBy, ArrayList<Integer> messages, ArrayList<Integer> recipients, Message lastMessage) {
        this.uid = uid;
        this.seenBy = seenBy;
        this.messages = messages;
        this.recipients = recipients;
        this.lastMessage = lastMessage;
    }


    /**
     * Creates a Room object from JsonObject.
     * @param jsonObject
     * Room jsonObject.
     */
    public Room(String jsonObject){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        Room room = gson.fromJson(jsonObject, Room.class);
        this.uid = room.getUid();
        this.seenBy = room.getSeenBy();
        this.messages = room.getMessages();
        this.recipients = room.getRecipients();
        this.lastMessage = room.getLastMessage();
    }

    /**
     * Gets a room from InputStream by reading string and creating a new Room.
     * @param inputStream
     * InputStream object from the Socket
     */
    public Room(InputStream inputStream) {
        Room jsonRoom = new Room(ClientThread.readStringFromInptStrm(inputStream));
        this.uid = jsonRoom.getUid();
        this.seenBy = jsonRoom.getSeenBy();
        this.messages = jsonRoom.getMessages();
        this.recipients = jsonRoom.getRecipients();
        this.lastMessage = jsonRoom.getLastMessage();
    }

    /**
     * Get the list of chat rooms for a user by UID.
     * @param uid
     * Classes.User UID to get Rooms for.
     * @return
     * String Of Json array of Rooms.
     */
    public static String getJsonStringOfAllRoomsForUser(int uid){

        return getJsonStringOfArrayOfRooms(getAllRoomsForUser(uid));
    }

    /*
            DB Functions
     */
    /**
     * Get a Room by UID. This function communicates with the DB.
     * @param uid
     * User UID to get Room for.
     * @return
     * Room object.
     */
    public static Room getRoomFromUID(int uid) {
        Room room = null;
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM rooms WHERE uid=? LIMIT 1")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()) {
                        JsonReader reader;
                        int[] seenBy = null;
                        if (resultSet.getString(2) != null && !resultSet.getString(2).isEmpty()) {
                            reader = getGson().newJsonReader(resultSet.getCharacterStream(2));
                            reader.setLenient(true);
                            seenBy = getGson().fromJson(reader, int[].class);
                        }
                        reader = getGson().newJsonReader(resultSet.getCharacterStream(3));
                        reader.setLenient(true);
                        int[] messages = getGson().fromJson(reader, int[].class);
                        reader = getGson().newJsonReader(resultSet.getCharacterStream(4));
                        int[] recipients = getGson().fromJson(reader, int[].class);
                        room = new Room(
                                resultSet.getInt(1),
                                getArrayListFromArray(seenBy),
                                getArrayListFromArray(messages),
                                getArrayListFromArray(recipients),
                                getGson().fromJson(JsonParser.parseReader(resultSet.getCharacterStream(5)), Message.class));
                    }
                }catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return room;
    }

    /**
     * Get ArrayLis of Rooms by looping around getRoomFromUID method. This function communicates with the DB.
     * @param uids
     * Rooms uids as int[].
     * @return
     * ArrayList of Rooms.
     */
    public static ArrayList<Room> getRoomsFromUIDs(int[] uids){
        ArrayList<Room> rooms = new ArrayList<>();
        for (int room :
                uids) {
            rooms.add(Room.getRoomFromUID(room));
        }
        return rooms;
    }

    /**
     * Get the list of chat rooms for a user by UID. This function communicates with the DB.
     * @param uid
     * Classes.User UID to get Rooms for.
     * @return
     * ArrayList of Rooms.
     */
    public static ArrayList<Room> getAllRoomsForUser(int uid){
        ArrayList<Room> rooms = new ArrayList<>();

        try(Connection conn = DBConnection.getConn()){
            try(PreparedStatement statement = conn.prepareStatement("SELECT chatrooms FROM users WHERE uid=? LIMIT 1")){
                statement.setInt(1, uid);
                try(ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()){
                        if (resultSet.getString(CHATROOMS) != null && !resultSet.getString(CHATROOMS).isEmpty()){
                            int[] chatrooms = getGson().fromJson(JsonParser.parseReader(resultSet.getCharacterStream(CHATROOMS)), int[].class);
                            for (int room : chatrooms) {
                                rooms.add(getRoomFromUID(room));
                            }
                        }else {
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    /**
     * Get Json array of all room objects.
     * @param rooms
     * ArrayList of Rooms.
     * @return
     * Json array containing all Room objects provided.
     */
    public static String getJsonStringOfArrayOfRooms(ArrayList<Room> rooms) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        return builder.create().toJson(rooms);
    }

    /**
     * Add new Room to DB. If Room exists, update (seenBy,messages,lastMessage) fields. This function communicates with the DB.
     * @param room
     * Room object to add/update to/in DB.
     * @return
     * new room uid, 0 if room already exists.
     */
    public static int addRoom(Room room){
        try(Connection conn = getConn()){
            boolean roomAlreadyExists = false;
            if (room.getUid() != 0) {
                try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM rooms WHERE uid=?")) {
                    statement.setInt(1, room.getUid());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next())
                            roomAlreadyExists = true;
                    }
                }
            }
            if(roomAlreadyExists){
                try(PreparedStatement statement = conn.prepareStatement(
                        "UPDATE rooms SET seenBy=?,messages=?,lastmessage=? WHERE uid=?")){
                    statement.setString(1, createJsonArrayOf(room.getSeenBy()));
                    statement.setString(2, createJsonArrayOf(room.getMessages()));
                    statement.setString(3, room.getLastMessage().toString());
                    statement.setInt(4, room.getUid());
                    statement.execute();
                    return 0;
                }
            }
            try(PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO rooms(" +
                            "seenby,messages," +
                            "recipients,lastmessage)" +
                            " VALUES (?,?,?,?)")){
                statement.setString(1, room.getSeenBy() == null ? null : createJsonArrayOf(room.getSeenBy()));
                statement.setString(2, createJsonArrayOf(room.getMessages()));
                statement.setString(3, createJsonArrayOf(room.getRecipients()));
                statement.setString(4, room.getLastMessage().toString());
                statement.execute();
                try (PreparedStatement getNewID = conn.prepareStatement("SELECT LAST_INSERT_ID()")){
                    try (ResultSet resultSet = getNewID.executeQuery()){
                        if (resultSet.next())
                            return resultSet.getInt(1);
                    }
                }
            }
        }catch (SQLIntegrityConstraintViolationException e){
            System.out.println("this key already exists in the table");
//            if(e.getMessage().contains("'PRIMARY'")){
//
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Add messageUid to Room field (messages), by getting the field value and adding the messageUid to the array and updating it in DB.
     * @param messageUid
     * New Message UID.
     * @param roomUid
     * Room UID to add Message to.
     * @return
     * true if succeeded, false if didn't.
     */
    public static boolean addMessageToRoom(int messageUid, int roomUid){
        try(Connection conn = getConn()){
            try(PreparedStatement statement = conn.prepareStatement("SELECT messages FROM rooms WHERE uid=?")){
                statement.setInt(1, roomUid);
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()) {
                        String messagesArray = resultSet.getString(1);
                        ArrayList<Integer> ints = new ArrayList<>();
                        if (messagesArray != null && !messagesArray.isEmpty())
                            ints = DBConnection.getArrayListFromArray(getGson().fromJson(messagesArray, int[].class));
                        ints.add(messageUid);
                        try (PreparedStatement updateMessagesField = conn.prepareStatement("UPDATE rooms SET messages=? WHERE uid=?")){
                            updateMessagesField.setString(1, createJsonArrayOf(ints));
                            updateMessagesField.setInt(2, roomUid);
                            return statement.execute();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Override toString function to create a json object.
     * @return
     * String: json object of Class Room.
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

    public ArrayList<Integer> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(ArrayList<Integer> seenBy) {
        this.seenBy = seenBy;
    }

    public ArrayList<Integer> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Integer> messages) {
        this.messages = messages;
    }

    public ArrayList<Integer> getRecipients() {
        return recipients;
    }

    public void setRecipients(ArrayList<Integer> recipients) {
        this.recipients = recipients;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
