package com.company.Clients;

import com.company.Classes.*;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.company.Classes.DBConnection.getConn;
import static com.company.Classes.DBConnection.getGson;

public class ClientThread extends Thread {

    /*MESSAGE*/
    public static final int SEND_MESSAGE = 101;
    public static final int GET_MESSAGES = 102;
    /*USER*/
    public static final int GET_NEARBY_USERS = 120;
    public static final int ADD_USER = 121;
    public static final int GET_USER_FROM_UID = 122;
    public static final int ADD_FAV = 123;
    public static final int REM_FAV = 124;
    public static final int GET_FAVS = 125;
    public static final int GET_USERINFO = 126;
    public static final int UPDATE_USERINFO = 127;
    public static final int GET_NEW_USERS = 128;
    public static final int GET_USERDISTANCE = 129;
    public static final int GET_SMALL_USER = 130;
    public static final int UPDATE_USER_FIELDS = 131;

    public static final int TEST = 140;
    /*GEO_POINT*/
    public static final int UPDATE_LOCATION = 150;
    /*USER_INFO*/
    public static final int GET_ALL_ROOMS = 160;

    /*SERVER_CODES*/
    public static final int OKAY = 200;
    public static final int FAILURE = 500;
    public static final int READY = 300;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private List<Message> messages;
    private Map<Integer, User> users;

    public ClientThread(Socket socket, List<Message> messages, Map<Integer, User> users) {
        this.socket = socket;
        this.messages = messages;
        this.users = users;
    }

    @Override
    public void run() {
        try{
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            int action = inputStream.read();
            switch (action){
                case SEND_MESSAGE:
                    System.out.println("SEND MESSAGE");
                    sendMessage();
                    break;
                case GET_MESSAGES:
                    System.out.println("GET MESSAGE");
                    getMessages();
                    break;
                case GET_NEARBY_USERS:
                    System.out.println("GET NEARBY USERS");
                    getNearbyUsers();
                    break;
                case ADD_USER:
                    System.out.println("ADD USER");
                    addUser();
                    break;
                case GET_USER_FROM_UID:
                    System.out.println("GET USER");
                    getUserFromUID();
                    break;
                case ADD_FAV:
                    System.out.println("ADD FAV");
                    addFavouriteUser();
                    break;
                case REM_FAV:
                    System.out.println("REM FAV");
                    removeFavouriteUser();
                    break;
                case GET_FAVS:
                    System.out.println("GET FAVS");
                    getFavourites();
                    break;
                case GET_USERINFO:
                    System.out.println("GET USERINFO");
                    getUserInfo();
                    break;
                case UPDATE_USERINFO:
                    System.out.println("UPDATE USERINFO");
                    updateUserInfo();
                    break;
                case GET_NEW_USERS:
                    System.out.println("GET NEW USERS");
                    getNewUsers();
                    break;
                case GET_USERDISTANCE:
                    System.out.println("GET USER DISTANCE");
                    getUserDistance();
                    break;
                case GET_SMALL_USER:
                    System.out.println("GET SMALL USER");
                    getSmallUser();
                    break;
                case UPDATE_USER_FIELDS:
                    System.out.println("UPDATE USER FIELDS");
                    updateUserFields();
                    break;
                case TEST:
                    System.out.println("TEST");
                    testFunction();
                    break;
                case UPDATE_LOCATION:
                    System.out.println("UPDATE LOCATION");
                    updateLocation();
                    break;
                case GET_ALL_ROOMS:
                    System.out.println("GET ALL ROOMS");
                    getAllRooms();
                    break;
                default:
                    System.out.println("NOT AN ACTION CODE " + action);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readStringFromInptStrm(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try{
            int nRead;
            int dataLength = inputStream.read();
            byte[] data = new byte[dataLength];
            while ((nRead = inputStream.read(data, 0, dataLength)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }

    private void sendMessage() throws IOException{
        Message message = new Message(inputStream);
        messages.add(message);
        outputStream.write(OKAY);
    }

    private void getMessages() throws IOException{
        byte[] fromBytes = new byte[4];
        int actuallyRead = inputStream.read(fromBytes);
        if(actuallyRead != 4)
            return;
        int from = ByteBuffer.wrap(fromBytes).getInt();
        for (int i = from; i < messages.size(); i++) {
            Message message = messages.get(i);
            message.write(outputStream);
        }
    }

    private void getNearbyUsers() throws IOException{
        int uid = inputStream.read();
        List<UserDistance> users = User.getNearbyUsers(uid);
        String json = UserDistance.getJsonStringFromListOfUserDistances(users);
        System.out.println("nearbyUsers: "+json);
        byte[] bytes = json.getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    private void addUser() throws IOException{
        User user = new User(inputStream);
        if (User.addUser(user)) {
            outputStream.write(OKAY);
            return;
        }
        outputStream.write(FAILURE);
    }

    private void getUserFromUID() throws IOException{
        int uid = inputStream.read();
        User.getUserByUID(uid).write(outputStream);
    }

    private void addFavouriteUser() throws IOException{
        int currentUser = inputStream.read();
        int favUser = inputStream.read();

        int status = User.addOrRemoveFavouriteUsersOrRooms(User.ADD_DBFUNC, User.FAV_DBFUNC, favUser, currentUser);
        outputStream.write(status);
    }

    private void removeFavouriteUser() throws IOException{
        int favUser = inputStream.read();
        int currentUser = inputStream.read();

        User.addOrRemoveFavouriteUsersOrRooms(User.REMOVE_DBFUNC, User.FAV_DBFUNC, favUser, currentUser);
        outputStream.write(OKAY);
    }

    private void getFavourites() throws IOException{
        int currentUser = inputStream.read();
        String json = User.getJsonStringFromArrayOfUsers(User.getUsersByUIDs(User.getUserByUID(currentUser).getFavs()));
        byte[] bytes = json.getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    private void getUserInfo() throws IOException{
        int uid = inputStream.read();
        UserInfo.getUserInfoByUID(uid).write(outputStream);
    }

    private void updateUserInfo() throws IOException{
        int uid = inputStream.read();
        UserInfo userInfo = new UserInfo(inputStream);
        UserInfo.updateUserInfo(uid, userInfo);
    }

    private void getNewUsers() throws IOException{
        int uid = inputStream.read();
        List<UserDistance> users = User.getNewUsers(uid);
        String json = UserDistance.getJsonStringFromListOfUserDistances(users);
        System.out.println("newUsers: "+json);
        byte[] bytes = json.getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    private void getUserDistance() throws IOException, InterruptedException {
        int currentUID = inputStream.read();
        int otherUID = inputStream.read();
        UserDistance.getUserByUID(currentUID, otherUID).write(outputStream);
    }

    private void getSmallUser() throws IOException{
        int uid = inputStream.read();
        User.getSmallUserByUID(uid).write(outputStream);
    }

    private void updateUserFields() throws IOException{
        int uid = inputStream.read();
        int field = inputStream.read();
        String updatedField = readStringFromInptStrm(inputStream);

        User.updateUserFields(field, uid, updatedField);
    }

    private void updateLocation() throws IOException{
        int uid = inputStream.read();
        GeoPoint geoPoint = new GeoPoint(inputStream);

        User.updateUserLocation(uid, geoPoint);
    }

    private void getAllRooms() throws IOException{
        int uid = inputStream.read();
        String json = Room.getJsonStringOfArrayOfRooms(Room.getAllRoomsForUser(uid));
        byte[] bytes = json.getBytes();

        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    private void testFunction() throws IOException{
        String statement1 = readStringFromInptStrm(inputStream);
        System.out.println(statement1);
        User user1 = new User(1, "Pierre", new GeoPoint(1,1), "", null, null, null);
        User user2 = new User(2, "Michael", new GeoPoint(2,1), "", null, null, null);
        Message message = new Message(2, "this is the last message between us bitch", Timestamp.valueOf("2021-02-14 18:08:18"), user1, user2);

        String json = getGson().toJson(message);
        try (Connection conn = getConn()) {
            try (PreparedStatement statement = conn.prepareStatement(statement1)){

                statement.setString(1, json);
                statement.execute();
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
