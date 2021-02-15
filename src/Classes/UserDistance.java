package Classes;

import com.google.gson.*;
import com.mysql.cj.exceptions.FeatureNotAvailableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static Classes.DBConnection.*;

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

    public UserDistance(InputStream inputStream) throws IOException {
        int jsonLength = inputStream.read();
        if (jsonLength == -1)
            throw new IOException("json hasn't been sent");
        byte[] jsonBytes = new byte[jsonLength];
        int actuallyRead = inputStream.read(jsonBytes);
        if (actuallyRead != jsonLength)
            throw new IOException("");
        UserDistance jsonUserDistance = getUserDistanceFromJson(new String(jsonBytes));
        this.user = jsonUserDistance.getUser();
        this.distance = jsonUserDistance.getDistance();
    }

    public UserDistance(ResultSet resultSet) throws SQLException{
        this.user = new User(resultSet, false);
        this.distance = resultSet.getFloat(10);
    }

    public static UserDistance getUserByUID(int currentUID, int otherUID) throws IOException{
        UserDistance userDistance = null;
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement(GET_USER_DISTANCE_FUNC)){
                statement.setInt(1, currentUID);
                statement.setInt(2, otherUID);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()) {
                        userDistance = new UserDistance(resultSet);
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
        return userDistance;
    }

    public static List<UserDistance> readUsers(InputStream inputStream) throws IOException{
        int jsonLength = inputStream.read();
        if (jsonLength == -1)
            throw new IOException("json hasn't been sent");
        byte[] jsonBytes = new byte[jsonLength];
        int actuallyRead = inputStream.read(jsonBytes);
        if (actuallyRead != jsonLength)
            throw new IOException("");
        return Arrays.asList(getArrayOfUserDistancesFromJson(new String(jsonBytes)));
    }

    public static UserDistance[] getArrayOfUserDistancesFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, UserDistance[].class);
    }

    /**
     * Get UserDistance object from Json String.
     * @param json
     * Json String.
     * @return
     * UserDistance Object.
     */
    public static UserDistance getUserDistanceFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, UserDistance.class);
    }

    /**
     * Write Json object to OutputStream.
     * @param outputStream
     * OutputStream to write to.
     */
    public void write(OutputStream outputStream) throws IOException {
        System.out.println("UserDistance write: " + this.toString());
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
