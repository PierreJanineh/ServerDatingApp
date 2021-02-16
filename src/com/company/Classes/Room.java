package com.company.Classes;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.company.Classes.DBConnection.getConn;
import static com.company.Classes.DBConnection.getGson;

public class Room {

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
    private int[] seenBy;
    private int[] messages; //uids
    private int[] recipients; //uids
    private Message lastMessage;

    public Room(int uid, int[] seenBy, int[] messages, int[] recipients, Message lastMessage) {
        this.uid = uid;
        this.seenBy = seenBy;
        this.messages = messages;
        this.recipients = recipients;
        this.lastMessage = lastMessage;
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
                        JsonReader reader = getGson().newJsonReader(new StringReader(resultSet.getString(2)));
                        reader.setLenient(true);
                        int[] seenBy = getGson().fromJson(reader, int[].class);
                        reader = getGson().newJsonReader(new StringReader(resultSet.getString(3)));
                        int[] messages = getGson().fromJson(reader, int[].class);
                        reader = getGson().newJsonReader(new StringReader(resultSet.getString(4)));
                        int [] recipients = getGson().fromJson(reader, int[].class);
                        room = new Room(
                                resultSet.getInt(1),
                                seenBy,
                                messages,
                                recipients,
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
                statement.setString(1, uid+"");
                try(ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()){
                        int[] chatrooms = getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream("chatrooms")).getAsJsonArray(), int[].class);
                        for (int room:
                             chatrooms) {
                            rooms.add(getRoomFromUID(room));
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

    public int[] getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(int[] seenBy) {
        this.seenBy = seenBy;
    }

    public int[] getMessages() {
        return messages;
    }

    public void setMessages(int[] messages) {
        this.messages = messages;
    }

    public int[] getRecipients() {
        return recipients;
    }

    public void setRecipients(int[] recipients) {
        this.recipients = recipients;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
