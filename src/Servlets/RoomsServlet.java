package Servlets;

import Classes.Room;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "RoomsServlet")
public class RoomsServlet extends HttpServlet {
    public static final int GET_ALL_ROOMS_FOR_USER = 1;
    public static final int IS_LAST_MSG_TO_ME = 2;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String uid = request.getParameter("uid");

        if (action == null || action.isEmpty())
            return;

        switch (Integer.parseInt(action)){
            case GET_ALL_ROOMS_FOR_USER:
                response.getWriter().write(Room.getJsonStringOfAllRoomsForUser(Integer.parseInt(uid))); //return all room in Json array
            case IS_LAST_MSG_TO_ME:

        }
    }
}
