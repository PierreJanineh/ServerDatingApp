package com.company.Classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBConnection {

    public static final String GET_USER_DISTANCE_FUNC = "CALL getdist(?,?)";

    /**
     * Create connection with DB with Username and Password.
     * @return
     * Connection object.
     */
    public static Connection getConn() throws SQLException {
        String connectionString = "jdbc:mysql://localhost:3306/felpr?useSSL=false";
        String user = "root";
        String password = "Owe42)spoons";
        return DriverManager.getConnection(connectionString, user, password);
    }

    public static Gson getGson(){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        return builder.create();
    }

    public static ArrayList<Integer> getArrayListFromArray(int[] arr){
        if (arr == null){
            return null;
        }
        ArrayList<Integer> array =  new ArrayList<>();
        for (int num : arr) {
            array.add(num);
        }
        return array;
    }

    public static String createJsonArrayOf(ArrayList<Integer> ints){
        JsonArray jsonArray = new JsonArray();
        for (int num : ints) {
            jsonArray.add(num);
        }
        return jsonArray.toString();
    }

}
