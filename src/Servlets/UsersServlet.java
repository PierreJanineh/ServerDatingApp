package Servlets;

import Classes.HttpConn;
import Classes.User;
import Classes.UserInfo;
import java.io.IOException;
import static Classes.HttpConn.*;
import static Classes.User.*;

public class UsersServlet extends javax.servlet.http.HttpServlet {

    public static final int GET_USER_BY_UID = 1;
    public static final int GET_USERINFO_BY_UID = 2;
    public static final int GET_USERS_BY_UIDS = 3;
    public static final int ADD_FAV_USER = 4;
    public static final int REMOVE_FAV_USER = 5;
    public static final int ADD_ROOM_FOR_USER = 6;
    public static final int REMOVE_ROOM_FOR_USER = 7;

    public static final int UPDATE_USER = 1;
    public static final int ADD_USER = 2;


    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        /*  Post starts for 1: Updating User
                            2: Adding User
         */
        String body = readBody(request);

        // Body's been read, ready to take actions.
        if (body == null || body.isEmpty()) {
            response.getOutputStream().write(INTERNAL_SERVER_ERROR.getBytes());
            response.getOutputStream().write("Nothing has been passed in the body".getBytes());
            return;
        }

        HttpConn.UserPOSTBody userPOSTBody = new HttpConn.UserPOSTBody(body); //Parse body from Json ass Defined class HttpConn.UserPOST

        if (userPOSTBody.getUser() == null){
            response.getOutputStream().write(INTERNAL_SERVER_ERROR.getBytes());
            response.getOutputStream().write("User object has not been passed".getBytes());
            return;
        }

        switch (userPOSTBody.getAction()){ //Check action
            case UPDATE_USER:
                switch (userPOSTBody.getField()){
                    case USERNAME_DBFUNC:
                        User.updateUserFields(USERNAME_DBFUNC, userPOSTBody.getUser().getUid(), userPOSTBody.getUser().getUsername());
                    case IMGURL_DBFUNC:
                        User.updateUserFields(IMGURL_DBFUNC, userPOSTBody.getUser().getUid(), userPOSTBody.getUser().getImg_url());
                }
                response.getOutputStream().write(SUCCESS.getBytes());
            case ADD_USER:
                boolean bool = User.addUser(userPOSTBody.getUser());
                if (!bool){
                    response.getOutputStream().write(INTERNAL_SERVER_ERROR.getBytes());
                    response.getOutputStream().write("User uid already exists in DB".getBytes());
                }
                response.getOutputStream().write(SUCCESS.getBytes());
        }
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String action = request.getParameter("action");
        String uidString = request.getParameter("uid");
        String otherUID = request.getParameter("other_uid");

        if (action == null ||
                action.isEmpty() ||
                uidString == null ||
                uidString.isEmpty())
            return;

        int uid = Integer.parseInt(uidString);

        switch (Integer.parseInt(action)) {
            case GET_USER_BY_UID:
                response.getWriter().write(User.getUserByUID(uid).toString()); //returns Json object of User
            case GET_USERINFO_BY_UID:
                response.getWriter().write(UserInfo.getUserInfoByUID(uid).toString()); //returns Json object of UserInfo
            case GET_USERS_BY_UIDS:
                String body = readBody(request);
                // Body's been read, ready to take actions.
                if (body == null || body.isEmpty())
                    return;

                HttpConn.UserGETBody userGETBody = new HttpConn.UserGETBody(body); //Parse body from Json ass Defined class HttpConn.UserPOST
                User[] users = User.getUsersByUIDs(userGETBody.getUids()); //Get all requested Users
                String usersJson = User.getJsonStringFromArrayOfUsers(users); //Convert to Json
                response.getOutputStream().write(usersJson.getBytes()); //Write back to API
            case ADD_FAV_USER:
                if (otherUID == null || otherUID.isEmpty())
                    return;
                User.addOrRemoveFavouriteUsersOrRooms(ADD_DBFUNC, FAV_DBFUNC, uid, Integer.parseInt(otherUID));
            case REMOVE_FAV_USER:
                if (otherUID == null || otherUID.isEmpty())
                    return;
                User.addOrRemoveFavouriteUsersOrRooms(REMOVE_DBFUNC, FAV_DBFUNC, uid, Integer.parseInt(otherUID));
            case ADD_ROOM_FOR_USER:
                if (otherUID == null || otherUID.isEmpty())
                    return;
                User.addOrRemoveFavouriteUsersOrRooms(ADD_DBFUNC, ROOM_DBFUNC, uid, Integer.parseInt(otherUID));
            case REMOVE_ROOM_FOR_USER:
                if (otherUID == null || otherUID.isEmpty())
                    return;
                User.addOrRemoveFavouriteUsersOrRooms(REMOVE_DBFUNC, FAV_DBFUNC, ROOM_DBFUNC, Integer.parseInt(otherUID));
        }
    }
}
