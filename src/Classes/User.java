package Classes;

import Clients.ClientThread;
import com.google.gson.*;
import com.mysql.cj.exceptions.FeatureNotAvailableException;

import java.io.*;

import java.sql.*;
import java.util.*;

import static Classes.DBConnection.*;
import static Clients.ClientThread.FAILURE;

public class User {

    /*
    users table in MySQL database columns:
                1. uid              int PK      (user uid)
                2. username         tinytext    (username string - 255 bytes)
                3. lat              float       (user's geopoint latitude)
                4. lng              float       (user's geopoint longitude)
                5. img_url          longtext    (image link - uploaded on REPLACE_IMAGE action)
                6. favs             longtext    (json array of user's uids)
                7. chatrooms        longtext    (json array of rooms' uids)
                8. info             int         (user_info uid AKA 'user's uid')
                9. joined           date        (date joined on - for searching new users)

     */

    /*
    WholeUser class in Android project contains:
                  1. uid            int
                  2. username       String
                  3. geoPoint       GeoPoint
                  4. img_url        String
                  5. favs           ArrayList<Integer>
                  6. chatrooms      ArrayList<Room>
                  7. info           UserInfo
     */

    /*
    SmallUser class in Android project contains:
                  1. uid            int
                  2. username       String
                  3. geoPoint       GeoPoint
                  4. img_url        String
     */

    public static final int USERNAME_DBFUNC = 100;
    public static final int IMGURL_DBFUNC = 200;
    public static final int FAV_DBFUNC = 300;
    public static final int ROOM_DBFUNC = 400;
    public static final int ADD_DBFUNC = 1;
    public static final int REMOVE_DBFUNC = 2;
    public static final String UID = "uid";
    public static final String USERNAME = "username";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String IMG_URL = "img_url";
    public static final String FAVS = "favs";
    public static final String INFO = "info";
    public static final String ROOMS = "rooms";

    private int uid;
    private String username;
    private GeoPoint geoPoint;
    private String img_url;
    private ArrayList<Integer> favs;
    private ArrayList<Room> chatRooms;
    private UserInfo info;

    public User(int uid, String username, GeoPoint geoPoint, String img_url, ArrayList<Integer> favs, ArrayList<Room> chatRooms, UserInfo info) {
        this.uid = uid;
        this.username = username;
        this.geoPoint = geoPoint;
        this.img_url = img_url;
        this.favs = favs;
        this.chatRooms = chatRooms;
        this.info = info;
    }

    /**
     * Creates a User object from JsonObject.
     * @param jsonObject
     * User jsonObject.
     */
    public User(JsonObject jsonObject){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        User user = gson.fromJson(jsonObject, User.class);
        this.uid = user.getUid();
        this.username = user.getUsername();
        this.geoPoint = user.getGeoPoint();
        this.img_url = user.getImg_url();
        this.favs = user.getFavs();
        this.chatRooms = user.getChatRooms();
        this.info = user.getInfo();
    }

    /**
     * Creates a User object from JsonObject string.
     * @param jsonObject
     * User jsonObject string.
     */
    public User(String jsonObject){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        User user = gson.fromJson(jsonObject, User.class);
        this.uid = user.getUid();
        this.username = user.getUsername();
        this.geoPoint = user.getGeoPoint();
        this.img_url = user.getImg_url();
        this.favs = user.getFavs();
        this.chatRooms = user.getChatRooms();
        this.info = user.getInfo();
    }

    /**
     * Gets a user from InputStream by reading string and creating a new User
     * @param inputStream
     * InputStream object from the Socket
     * @throws IOException
     * throws IOException if reading from InputStream fails.
     */
    public User(InputStream inputStream) throws IOException {
        User jsonUser = new User(ClientThread.readStringFromInptStrm(inputStream));
        this.uid = jsonUser.getUid();
        this.username = jsonUser.getUsername();
        this.geoPoint = jsonUser.getGeoPoint();
        this.img_url = jsonUser.getImg_url();
        this.favs = jsonUser.getFavs();
        this.chatRooms = jsonUser.getChatRooms();
        this.info = jsonUser.getInfo();
    }

    public User(ResultSet resultSet, boolean isSmall) throws SQLException{
        this.uid = resultSet.getInt(1);
        this.username = resultSet.getString(2);
        this.geoPoint = new GeoPoint(resultSet.getInt(3), resultSet.getInt(4));
        this.img_url = resultSet.getString(5);
        if (!isSmall){
            if ((this.info = UserInfo.getUserInfoByUID(resultSet.getInt(1))) == null){
                this.info = new UserInfo(this.getUid());
            }
        }
    }

    public static List<UserDistance> getNearbyUsers(int uid){
        List<UserDistance> users = new ArrayList<>();
        try (Connection conn = getConn()){
            try (CallableStatement statement = conn.prepareCall(
                    "CALL geodist(?,10)")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    while (resultSet.next()){
                        users.add(
                                new UserDistance(
                                        new User(resultSet, true),
                                        resultSet.getFloat(10)));
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
        return users;
    }

    /**
     * Get User object from Json String.
     * @param json
     * Json String.
     * @return
     * User Object.
     */
    public static User getUserFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, User.class);
    }

    /**
     * Adds a new User. This functions communicates with the DB.
     * @param user
     * New User to add.
     * @return
     * True if succeeded, false if not.
     */
    public static boolean addUser(User user){
        try(Connection conn = getConn()){
            boolean userAlreadyExists = false;
            try(PreparedStatement statement = conn.prepareStatement("SELECT * FROM users WHERE uid=?")){
                statement.setString(1, user.getUid()+"");
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next())
                        userAlreadyExists = true;
                }
            }
            if(userAlreadyExists)
                return false;
            try(PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO users(" +
                            "uid/*INT*/,username/*TINYTEXT*/," +
                            "lat/*DOUBLE*/,lng/*DOUBLE*/," +
                            "img_url/*LONGTEXT*/,favs/*LONGTEXT*/," +
                            "chatrooms/*LONGTEXT*/,info/*INT*/)" +
                            " VALUES (?,?,?,?,?,?,?,?)")){
                statement.setInt(1, user.getUid());
                statement.setString(2, user.getUsername());
                statement.setDouble(3, user.getGeoPoint().getLat());
                statement.setDouble(4, user.getGeoPoint().getLng());
                statement.setString(5, user.getImg_url());
                statement.setString(6, null);
                statement.setString(7, null);
                statement.setInt(8, user.getInfo().getUid());
                int rowsAffected = statement.executeUpdate();
                System.out.println("rows affected: " + rowsAffected);
                return true;
            }
        }catch (SQLIntegrityConstraintViolationException e){
            System.out.println("here we understand that this key already exists in the table");
//            if(e.getMessage().contains("'PRIMARY'")){
//
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getJsonArrayOfUIDs(ArrayList<Integer> uids){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        return builder.create().toJson(uids);
    }

    /**
     * Get Classes.User object by UID. This function communicates with the DB. ++
     * @param uidS
     * Classes.User UID int Array.
     * @return
     * Classes.User objects Array referred by the UIDs provided.
     */
    public static User[] getUsersByUIDs(ArrayList<Integer> uidS){
        User[] users = new User[uidS.size() -1];
        for (int i = 0; i <= uidS.size(); i++){
            users[i] = getUserByUID(uidS.get(i));
        }
        return users;
    }

    /**
     * Get Json array of all users objects.
     * @param users
     * ArrayList of Users.
     * @return
     * Json array containing all User objects provided.
     */
    public static String getJsonStringFromArrayOfUsers(User[] users){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.toJson(users);
    }

    /**
     * Get Classes.User object by UID. This functions communicates with the DB.
     * @param uid
     * Classes.User UID int.
     * @return
     * Classes.User object referred by the UID provided.
     */
    public static User getUserByUID(int uid) {
        User user = null;
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users WHERE uid=? LIMIT 1")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()) {

                        //Get array of favs
                        int[] favsArr = getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream(6)).getAsJsonArray(), int[].class);
                        int[] roomsArr = getGson().fromJson(new JsonParser().parse(resultSet.getCharacterStream(7)).getAsJsonArray(), int[].class);
                        user = new User(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                new GeoPoint(resultSet.getInt(3), resultSet.getInt(4)),
                                resultSet.getString(5),
                                getArrayListFromArray(favsArr),
                                Room.getRoomsFromUIDs(roomsArr),
                                UserInfo.getUserInfoByUID(resultSet.getInt(1)));
                    }
                }catch (FeatureNotAvailableException throwables) {
//                    throwables.printStackTrace();
                    System.out.println(throwables.toString());
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Get Client SmallUser(uid,username,geopoint,img_url) object by UID. This functions communicates with the DB.
     * @param uid
     * User UID int.
     * @return
     * User object referred by the UID provided.
     */
    public static User getSmallUserByUID(int uid) {
        User user = null;
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT uid,username,lat,lng,img_url FROM users WHERE uid=? LIMIT 1")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()) {

                        user = new User(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                new GeoPoint(resultSet.getInt(3), resultSet.getInt(4)),
                                resultSet.getString(5),
                                null,
                                null,
                                null);
                    }
                }catch (FeatureNotAvailableException throwables) {
//                    throwables.printStackTrace();
                    System.out.println(throwables.toString());
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Updates user location. This functions communicates with the DB.
     * @param uid
     * UID for the user to update their location.
     * @param geoPoint
     * Classes.GeoPoint object containing lat & lng.
     */
    public static void updateUserLocation(int uid, GeoPoint geoPoint) {

        try (Connection conn = getConn()) {
            try (PreparedStatement statement = conn.prepareStatement("UPDATE users SET lat=?, lng=? WHERE uid=?")){
                statement.setDouble(1, geoPoint.getLat());
                statement.setDouble(2, geoPoint.getLng());
                statement.setInt(3, uid);
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates user Username OR Img_url. This functions communicates with the DB.
     * @param field
     * Final static Int defined in Class.
     * 100 - USERNAME
     * 200 - IMG_URL
     * @param uid
     * UID for the user to update.
     * @param fieldUpdated
     * String field to update (img-url or username).
     */
    public static void updateUserFields(int field, int uid, String fieldUpdated){
        String fieldString = "";
        switch (field){
            case USERNAME_DBFUNC:
                fieldString = "username";
            case IMGURL_DBFUNC:
                fieldString = "img_url";
        }

        try (Connection conn = getConn()) {
            try (PreparedStatement statement = conn.prepareStatement("UPDATE users SET " + fieldString + "=? WHERE uid=?")){
                switch (field){
                    case USERNAME_DBFUNC:
                        statement.setString(1, fieldUpdated);
                    case IMGURL_DBFUNC:
                        statement.setString(1, fieldUpdated);
                }
                statement.setInt(2, uid);
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds or Removes favourite users to/from list for User. This functions communicates with the DB.
     * @param action
     * final static Int number for action.
     * 1 - ADD
     * 2 - REMOVE
     * @param favOrRoom
     * final static Int number for fav or room.
     * 300 - FAV
     * 400 - ROOM
     * @param currentUID
     * UID for the user.
     * @param otherUID
     * User uid to add to favourites list.
     */
    public static int addOrRemoveFavouriteUsersOrRooms(int action, int favOrRoom, int currentUID, int otherUID){
        String fieldToUpdate = "";
        switch (favOrRoom){
            case FAV_DBFUNC:
                fieldToUpdate = "favs";
            case ROOM_DBFUNC:
                fieldToUpdate = "chatrooms";
        }

        try (Connection conn = getConn()) {
            try (PreparedStatement selectingStatement = conn.prepareStatement("SELECT " + fieldToUpdate + " FROM users WHERE uid=?")){
                selectingStatement.setInt(1, currentUID);
                try (ResultSet resultSet = selectingStatement.executeQuery()){
                    ArrayList<Integer> arrayList = new ArrayList<>();
                    String jsonString = "";
                    if (resultSet.next()){
                        if ((jsonString = resultSet.getString(1)) != null && !jsonString.isEmpty()){
                            GsonBuilder builder = new GsonBuilder();
                            builder.setPrettyPrinting();
                            int[] intUIDs = builder.create().fromJson(jsonString, int[].class);
                            for (int i : intUIDs) {
                                arrayList.add(i);
                            }
                            jsonString = builder.create().toJson(arrayList);
                        }
                    }
                    switch (action){
                        case ADD_DBFUNC:
                            arrayList.add(otherUID);
                        case REMOVE_DBFUNC:
                            for (int i = 0; i <= arrayList.size(); i++) {
                                if (arrayList.get(i) == otherUID){
                                    arrayList.remove(i);
                                    break;
                                }
                            }
                    }
//                    if (favOrRoom == FAV_DBFUNC) {
//
//                    }

                    try (PreparedStatement updatingStatement = conn.prepareStatement("UPDATE users SET " + fieldToUpdate + "=? WHERE uid=?")){
                        updatingStatement.setString(1, jsonString);
                        updatingStatement.setInt(2, currentUID);

                        return ClientThread.OKAY;
                    }catch (SQLException throwables) {
                        throwables.printStackTrace();
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
        return FAILURE;
    }

    private static List<UserDistance> getNewUsersDB(int uid){
        List<UserDistance> users = new ArrayList<>();
        try (Connection conn = getConn()){
            try (CallableStatement statement = conn.prepareCall(
                    "CALL newUsers(?)")){
                statement.setInt(1, uid);
                System.out.println("uid "+uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    while (resultSet.next()){
                        System.out.println("Added a user");
                        users.add(
                                new UserDistance(
                                        new User(resultSet, true),
                                        resultSet.getFloat(10)));
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
        return users;
    }

    public List<UserDistance> getNewUsers(){
        return getNewUsersDB(this.uid);
    }

    public static List<UserDistance> getNewUsers(int uid){
        return getNewUsersDB(uid);
    }

    /**
     * Write Json object to OutputStream.
     * @param outputStream
     * OutputStream to write to.
     */
    public void write(OutputStream outputStream) throws IOException {
        System.out.println("User write: " + this.toString());
        byte[] bytes = this.toString().getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

    /**
     * creates a json object to return to Client with Userinfo UID instead of Object.
     * @return
     * String: json object of Class User.
     */
    public String toClientString() {
        JsonObject object = new JsonObject();
        object.addProperty(UID, this.uid);
        object.addProperty(USERNAME, this.username);
        object.addProperty(LAT, this.geoPoint.getLat());
        object.addProperty(LNG, this.geoPoint.getLng());
        object.addProperty(IMG_URL, this.img_url);
        JsonArray favsArr = new JsonArray(favs.size());
        for (Integer num : favs) {
            favsArr.add(num);
        }
        object.add(FAVS, favsArr);
        JsonArray roomsArr = new JsonArray(chatRooms.size());
        for (Room room : chatRooms) {
            roomsArr.add(room.getUid());
        }
        object.add(ROOMS, roomsArr);
        object.addProperty(INFO, this.info.getUid());

        return object.toString();
    }

    /**
     * Override toString function to create a json object.
     * @return
     * String: json object of Class User.
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public ArrayList<Integer> getFavs() {
        return favs;
    }

    public void setFavs(ArrayList<Integer> favs) {
        this.favs = favs;
    }

    public ArrayList<Room> getChatRooms() {
        return chatRooms;
    }

    public void setChatRooms(ArrayList<Room> chatRooms) {
        this.chatRooms = chatRooms;
    }

    public UserInfo getInfo() {
        return info;
    }

    public void setInfo(UserInfo info) {
        this.info = info;
    }
}
