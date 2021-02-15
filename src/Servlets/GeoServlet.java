package Servlets;

import Classes.GeoPoint;
import Classes.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Servlets.GeoServlet")
public class GeoServlet extends HttpServlet {

    public static final int UPDATE_USER_LOCATION = 1;
    public static final int GET_USER_LOCATION = 2;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String uid = request.getParameter("uid");
        String lat = request.getParameter("lat");
        String lng = request.getParameter("lng");

        if (action == null || action.isEmpty())
            return;

        switch (Integer.parseInt(action)){
            case UPDATE_USER_LOCATION:
                GeoPoint geoPoint = new GeoPoint(Float.parseFloat(lat), Float.parseFloat(lng));
                User.updateUserLocation((Integer.parseInt(uid)), geoPoint); //update location for user
            case GET_USER_LOCATION:
                return;
        }
    }
}
