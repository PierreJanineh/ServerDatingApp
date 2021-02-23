package com.company.Classes;

import com.company.Clients.ClientThread;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.company.Classes.DBConnection.*;

public class UserDistance {

    /*
    UserDistance class in Android project contains:
                  1. wholeUser      WholeUser
                  2. smallUser      SmallUser
                  3. distance       float
                  4. isWhole        boolean
     */

    public static final String DISTANCE = "distance";
    private User user;
    private float distance;

    public UserDistance(User user, float distance) {
        this.user = user;
        this.distance = distance;
    }

    /**
     * Get UserDistance object from Json String.
     * @param jsonObject
     * Json String.
     * @return
     * UserDistance Object.
     */
    public UserDistance(String jsonObject){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        UserDistance userDistance = gson.fromJson(jsonObject, UserDistance.class);
        this.user = userDistance.getUser();
        this.distance = userDistance.getDistance();
    }

    public UserDistance(InputStream inputStream) throws IOException {
        UserDistance jsonUserDistance = new UserDistance(ClientThread.readStringFromInptStrm(inputStream));
        this.user = jsonUserDistance.getUser();
        this.distance = jsonUserDistance.getDistance();
    }

    public UserDistance(ResultSet resultSet) throws SQLException{
        this.user = new User(resultSet, false);
        this.distance = resultSet.getFloat(10);
    }

    @Nullable
    public static List<UserDistance> getDistanceFromFavUsers(@NotNull User user){
        if (user.getFavs().size() == 0){
            return null;
        }
        ArrayList<UserDistance> distances = new ArrayList<>();
        for (int i = 0; i < user.getFavs().size(); i++) {
            distances.add(getUserByUID(user.getUid(), user.getFavs().get(i)));
        }
        return distances;
    }

    @Nullable
    public static UserDistance getUserByUID(@NotNull int currentUID,@NotNull int otherUID){
        UserDistance userDistance = null;
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement(GET_USER_DISTANCE_FUNC)){
                statement.setInt(1, currentUID);
                statement.setInt(2, otherUID);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()) {
                        userDistance = new UserDistance(resultSet);
                    }
                }catch (Exception throwables) {
                    throwables.printStackTrace();
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return userDistance;
    }

    public static List<UserDistance> readUsers(InputStream inputStream) throws IOException{
        String s = ClientThread.readStringFromInptStrm(inputStream);
        return Arrays.asList(getArrayOfUserDistancesFromJson(s));
    }

    public static UserDistance[] getArrayOfUserDistancesFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, UserDistance[].class);
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

    public static List<UserDistance> getNewUsers(int uid){
        List<UserDistance> users = new ArrayList<>();
        try (Connection conn = getConn()){
            try (CallableStatement statement = conn.prepareCall(
                    "CALL newUsers(?)")){
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
     * Get Json array of all userDistances objects.
     * @param users
     * ArrayList of UserDistances.
     * @return
     * Json array containing all UserDistance objects provided.
     */
    public static String getJsonStringFromListOfUserDistances(List<UserDistance> users){
        JsonArray array = new JsonArray(users.size());
        for (UserDistance user : users) {
            JsonObject object = new JsonObject();
            object.addProperty(User.UID, user.getUser().getUid());
            object.addProperty(User.USERNAME, user.getUser().getUsername());
            object.addProperty(User.LAT, user.getUser().getGeoPoint().getLat());
            object.addProperty(User.LNG, user.getUser().getGeoPoint().getLng());
            object.addProperty(User.IMG_URL, user.getUser().getImg_url());
//            object.addProperty(User.INFO, user.getUser().getInfo().getUid());
            object.addProperty(DISTANCE, user.getDistance());
            array.add(object);
        }
        return array.toString();
    }

    /**
     * Override toString function to create a json object.
     * @return
     * String: json object of Class UserDistance.
     */
    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        return builder.create().toJson(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
