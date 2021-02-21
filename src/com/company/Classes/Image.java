package com.company.Classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static com.company.Classes.DBConnection.getConn;

public class Image {

    private int uid, userUid;
    private String imgUrl;

    public Image(int uid, int userUid, String imgUrl) {
        this.uid = uid;
        this.userUid = userUid;
        this.imgUrl = imgUrl;
    }

    /**
     * Get Images by UID. This function communicates with the DB.
     * @param uid
     * User UID.
     * @return
     * Array of String(url) of images..
     */
    public static ArrayList<Image> getImages(int uid){
        ArrayList<Image> images = new ArrayList<>();
        try (Connection conn = getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM images WHERE user_uid=?")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    while (resultSet.next()) {
                        images.add(new Image(resultSet.getInt(1),
                                resultSet.getInt(2),
                                resultSet.getString(3)));
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
        return images;
    }

    public static String getJsonStringFromArray(ArrayList<Image> images){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.toJson(images);
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getUserUid() {
        return userUid;
    }

    public void setUserUid(int userUid) {
        this.userUid = userUid;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
