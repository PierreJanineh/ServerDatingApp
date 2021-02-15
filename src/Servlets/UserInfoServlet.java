package Servlets;

import Classes.HttpConn;
import Classes.UserInfo;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static Classes.HttpConn.*;
import static Classes.HttpConn.SUCCESS;

@WebServlet(name = "UserInfoServlet")
public class UserInfoServlet extends HttpServlet {

    public static final int UPDATE = 100;
    public static final int ADD = 200;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String body = readBody(request);

        // Body's been read, ready to take actions.
        if (body == null || body.isEmpty()) {
            response.getOutputStream().write(INTERNAL_SERVER_ERROR.getBytes());
            response.getOutputStream().write("Nothing has been passed in the body".getBytes());
            return;
        }

        HttpConn.UserInfoPOSTBody userInfoPOSTBody = new HttpConn.UserInfoPOSTBody(body); //Parse body from Json ass Defined class HttpConn.UserPOST

        if (userInfoPOSTBody.getUserInfo() == null){
            response.getOutputStream().write(INTERNAL_SERVER_ERROR.getBytes());
            response.getOutputStream().write("User object has not been passed".getBytes());
            return;
        }

        switch (userInfoPOSTBody.getAction()){ //Check action
            case UPDATE:
                UserInfo.updateUserInfo(userInfoPOSTBody.getUserInfo().getUid(), userInfoPOSTBody.getUserInfo());
                response.getOutputStream().write(SUCCESS.getBytes());
            case ADD:
                boolean bool = UserInfo.addUserInfo(userInfoPOSTBody.getUserInfo());
                if (!bool){
                    response.getOutputStream().write(INTERNAL_SERVER_ERROR.getBytes());
                    response.getOutputStream().write("User uid already exists in DB".getBytes());
                }
                response.getOutputStream().write(SUCCESS.getBytes());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
