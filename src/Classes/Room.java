package Classes;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static Classes.DBConnection.getConn;
import static Classes.DBConnection.getGson;

public class Room {

    private int roomUid;
    private int[] seenBy;
    private int[] messages; //uids
    private int[] recipients; //uids
    private Message lastMessage;

    public Room(int roomUid, int[] seenBy, int[] messages, int[] recipients, Message lastMessage) {
        this.roomUid = roomUid;
        this.seenBy = seenBy;
        this.messages = messages;
        this.recipients = recipients;
        this.lastMessage = lastMessage;
    }

    public static ArrayList<Room> getRoomsFromUIDs(int[] uids){
        ArrayList<Room> rooms = new ArrayList<>();
        for (int room :
                uids) {
            rooms.add(Room.getRoomFromUID(room));
        }
        return rooms;
    }

    public static Room getRoomFromUID(int uid) {
        Room room = null;
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM rooms WHERE uid=? LIMIT 1")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()) {
                        room = new Room(
                                resultSet.getInt(1),
                                getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream(3)).getAsJsonArray(), int[].class),
                                getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream(3)).getAsJsonArray(), int[].class),
                                getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream(4)).getAsJsonArray(), int[].class),
                                getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream(5)), Message.class));
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
     * Get the list of chat rooms for a user by UID.
     * @param uid
     * Classes.User UID to get Rooms for.
     * @return
     * String Of Json array of Rooms.
     */
    public static String getJsonStringOfAllRoomsForUser(int uid){

        return getJsonStringOfArrayOfRooms(getAllRoomsForUser(uid));
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

    public int getRoomUid() {
        return roomUid;
    }

    public void setRoomUid(int roomUid) {
        this.roomUid = roomUid;
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
