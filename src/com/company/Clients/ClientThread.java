package com.company.Clients;

import com.company.Classes.*;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {

    /*MESSAGE*/
    public static final int SEND_MESSAGE = 101;
    public static final int GET_MESSAGES = 102;
    /*USER*/
    public static final int GET_USER_FROM_UID = 120;
    public static final int GET_SMALL_USER = 121;
    public static final int GET_CURRENT_USER = 122;
    public static final int GET_NEARBY_USERS = 123;
    public static final int GET_NEW_USERS = 124;
    public static final int GET_USERINFO = 125;
    public static final int GET_USERDISTANCE = 126;
    public static final int GET_FAVS = 127;
    public static final int GET_IMAGES = 128;
    public static final int ADD_USER = 129;
    public static final int ADD_FAV = 130;
    public static final int REM_FAV = 131;
    public static final int UPDATE_USERINFO = 132;
    public static final int UPDATE_USER_FIELDS = 133;

    /*GEO_POINT*/
    public static final int UPDATE_LOCATION = 150;
    /*USER_INFO*/
    public static final int GET_ALL_ROOMS = 160;
    public static final int GET_USERS_OF_ROOMS_FOR_USER = 161;
    public static final int PROFILE_VIEW = 162;

    /*SERVER_CODES*/
    public static final int OKAY = 200;
    public static final int FAILURE = 500;
    public static final int READY = 300;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public ClientThread(Socket socket) {
        this.socket = socket;
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
                    getWholeUserFromUID();
                    break;
                case GET_CURRENT_USER:
                    System.out.println("GET CURRENT USER");
                    getCurrentUser();
                    break;
                case GET_IMAGES:
                    System.out.println("GET IMAGES");
                    getImages();
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
                case UPDATE_LOCATION:
                    System.out.println("UPDATE LOCATION");
                    updateLocation();
                    break;
                case GET_ALL_ROOMS:
                    System.out.println("GET ALL ROOMS");
                    getAllRooms();
                    break;
                case GET_USERS_OF_ROOMS_FOR_USER:
                    System.out.println("GET USERS OF ROOMS FOR USER");
                    getUsersOfRoomsForUser();
                    break;
                case PROFILE_VIEW:
                    System.out.println("PROFILE VIEW");
                    profileView();
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

    public static String readStringFromInptStrm(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try{
            int nRead;
            int dataLength = inputStream.read();
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                if (nRead < 1024){
                    break;
                }
            }
            buffer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }

    private void sendMessage() throws IOException{
        Room room = new Room(inputStream);
        int msgID = Message.addMessage(room.getLastMessage());
        room.getLastMessage().setUid(msgID);

        if (room.getMessages() == null || room.getMessages().size() == 0){//if doesn't have messages, create new ArrayList and add message
            room.setMessages(new ArrayList<>());
        }
        room.getMessages().add(msgID);

        int roomID = Room.addRoom(room);//if room already exists, update room.
        if (roomID == 0){
            roomID = room.getUid();
            //No need to add room for user. room already exists.
        }else{
            room.setUid(roomID);
            User.addRoomToBothUsers(roomID, room.getLastMessage().getTo().getUid(), room.getLastMessage().getFrom().getUid());
        }
        Message.addRoomIdToMessage(roomID, msgID);
        Room.addMessageToRoom(msgID, roomID);
        room.write(outputStream);
//        outputStream.write(roomID);
    }

    private void getMessages() throws IOException{
        int room = inputStream.read();
        int from = inputStream.read();
        if (from < 0)
            from = 0;

        Room roomObj = Room.getRoomFromUID(room);
        System.out.println("before loop");
        for (int i = from; i < roomObj.getMessages().size(); i++) {
            Message message = Message.getMessageByUID(roomObj.getMessages().get(i));
            System.out.println("looping message :"+message.toString());
            message.write(outputStream);
        }
    }

    private int readInt(InputStream inputStream) throws IOException {
        byte[] fromBytes = new byte[4];
        int actuallyRead = inputStream.read(fromBytes);
        if(actuallyRead != 4)
            return 0;
        return ByteBuffer.wrap(fromBytes).getInt();
    }

    private void getImages() throws IOException{
        int uid = inputStream.read();
        ArrayList<Image> images = Image.getImages(uid);
        String json = Image.getJsonStringFromArray(images);
        byte[] bytes = json.getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    private void getNearbyUsers() throws IOException{
        int uid = inputStream.read();
        List<UserDistance> users = UserDistance.getNearbyUsers(uid);
        String json = UserDistance.getJsonStringFromListOfUserDistances(users);
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

    private void getWholeUserFromUID() throws IOException{
        int uid = inputStream.read();
        User.getWholeUserByUID(uid).write(outputStream);
    }

    private void getCurrentUser() throws IOException{
        int uid = inputStream.read();
        User user = User.getWholeCurrentUserByUID(uid);
        user.write(outputStream);
    }

    private void addFavouriteUser() throws IOException{
        int currentUser = inputStream.read();
        int favUser = inputStream.read();
        int status = User.addOrRemoveFavouriteUsersOrRooms(User.ADD_DBFUNC, User.FAV_DBFUNC, currentUser, favUser);
        outputStream.write(status);
    }

    private void removeFavouriteUser() throws IOException{
        int currentUser = inputStream.read();
        int favUser = inputStream.read();
        int status = User.addOrRemoveFavouriteUsersOrRooms(User.REMOVE_DBFUNC, User.FAV_DBFUNC, currentUser, favUser);
        outputStream.write(status);
    }

    private void getFavourites() throws IOException{
        int currentUser = inputStream.read();
        List<UserDistance> favs = UserDistance.getDistanceFromFavUsers(User.getWholeCurrentUserByUID(currentUser));
        String json = "";
        if (favs != null){
            json = UserDistance.getJsonStringFromListOfUserDistances(favs);
        }
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

        outputStream.write(UserInfo.updateUserInfo(uid, userInfo));
    }

    private void getNewUsers() throws IOException{
        int currentUser = inputStream.read();
        List<UserDistance> users = UserDistance.getNewUsers(currentUser);
        String json = UserDistance.getJsonStringFromListOfUserDistances(users);

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

    private void getUsersOfRoomsForUser() throws IOException {
        int uid = inputStream.read();
        String json = User.getJsonStringFromArrayListOfUsers(Room.getUsersForRoomsOfUser(uid));

        byte[] bytes = json.getBytes();

        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    private void profileView() throws IOException {
        int uid = inputStream.read();
        int otherUID = inputStream.read();

        User.profileView(uid, otherUID);
    }
}
