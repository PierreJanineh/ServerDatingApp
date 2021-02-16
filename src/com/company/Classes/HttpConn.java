package com.company.Classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HttpConn {

    private static GsonBuilder builder = new GsonBuilder();


    public static final String INTERNAL_SERVER_ERROR = "500";
    public static final String SUCCESS = "100";

    private static void createGson(){
        builder.setPrettyPrinting();
    }

    public static class UserGETBody {
        private int action;
        private ArrayList<Integer> uids; // For getting Users from UIDs

        public UserGETBody(int action, ArrayList<Integer> uids) {
            this.action = action;
            this.uids = uids;
        }

        /**
         * Parse body and create a new UserGET class from Json object passed as body in GET requests to UsersServlet.
         * <h2>This is for Action: <Strong>GET_USERS_BY_UIDS</Strong></h2>
         * @param body
         * GET body: Json object passed as body in GET requests to UsersServlet.
         */
        public UserGETBody(String body){
            createGson();

            Gson gson = builder.create();
            this.action = gson.fromJson(body, UserGETBody.class).action;
            this.uids = gson.fromJson(body, UserGETBody.class).uids;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public ArrayList<Integer> getUids() {
            return uids;
        }

        public void setUids(ArrayList<Integer> uids) {
            this.uids = uids;
        }
    }

    public static class UserPOSTBody {
        private int action;
        private User user; // For adding a User
        private int field; // For updating User

        public UserPOSTBody(int action, User user, int field) {
            this.action = action;
            this.user = user;
            this.field = field;
        }

        public UserPOSTBody(String body){
            createGson();

            Gson gson = builder.create();
            this.action = gson.fromJson(body, UserPOSTBody.class).action;
            this.user = gson.fromJson(body, UserPOSTBody.class).user;
            this.field = gson.fromJson(body, UserPOSTBody.class).field;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public int getField() {
            return field;
        }

        public void setField(int field) {
            this.field = field;
        }
    }

    public static class UserInfoPOSTBody {
        private int action;
        private UserInfo userInfo;

        public UserInfoPOSTBody(int action, UserInfo userInfo) {
            this.action = action;
            this.userInfo = userInfo;
        }

        public UserInfoPOSTBody(String body) {
            createGson();

            Gson gson = builder.create();
            this.action = gson.fromJson(body, UserInfoPOSTBody.class).action;
            this.userInfo = gson.fromJson(body, UserInfoPOSTBody.class).userInfo;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }
    }
}
