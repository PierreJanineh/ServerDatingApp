package Clients;

import Classes.Message;
import Classes.User;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadedMessaging {

    private static final int PORT = 3000;


    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        List<Message> messagesList = new ArrayList<>();
        Map<Integer, User> usersMap = new HashMap<>();

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                socket = serverSocket.accept();

                System.out.println(socket.getLocalSocketAddress() + "address");
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            System.out.println("new thread");
            new ClientThread(socket, messagesList, usersMap).start();
        }
    }
}
