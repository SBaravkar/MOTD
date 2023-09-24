/*
 * Server.java
 * This file contains code to process commands received from clients.
 * The server will handle MSGGET, MSGSTORE, LOGIN, LOGOUT, SHUTDOWN and QUIT requests from client.
 */


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    public static final int SERVER_PORT = 1002;
    // Initialize hashmap to store user credentials.
    public static HashMap<String, String> userCred;
    // Initialize message list to store messages for MSGGET and MSGSTORE.
    public static ArrayList<String> messagesOfTheDay;
    // Initialize message index for MSGGET.
    public static int messageIndex = 0;
    // Initialize session flag to track logged-In users.
    public static boolean isLoggedIn = false;

    public static int loggedInUser = 0;

    public static void main(String[] args)
    {
        ServerSocket myServerice = null;
        String line;
        BufferedReader is;
        PrintStream os;
        Socket serviceSocket = null;
        // Initialize login credentials variables.
        String username = "";
        String password = "";
        // Store user credentials.
        userCred = new HashMap<>();
        userCred.put("root", "root01");
        userCred.put("john", "john01");
        userCred.put("david", "david01");
        userCred.put("mary", "mary01");

        messagesOfTheDay = readMessagesFromFile("messages.txt");

        // Try to open a server socket
        try {
            myServerice = new ServerSocket(SERVER_PORT);
        }
        catch (IOException e) {
            System.out.println(e);
        }

        // Create a socket object from the ServerSocket to listen and accept connections.
        // Open input and output streams
        while (true)
        {
            try
            {
                serviceSocket = myServerice.accept();
                is = new BufferedReader (new InputStreamReader(serviceSocket.getInputStream()));
                os = new PrintStream(serviceSocket.getOutputStream());

                // As long as we receive data, echo that data back to the client.
                while ((line = is.readLine()) != null)
                {
                    // The server will confirm and terminate the client upon QUIT request.
                    if ("QUIT".equals(line)) {
                        os.println("200 OK. The Client is QUIT.");
                        break;
                    }
                    // Server verifies user validity during LOGIN authentication.
                    else if (line.startsWith("LOGIN")) {
                        String[] loginInfo = line.split(" ");
                        if (loginInfo.length == 3) {
                            username = loginInfo[1];
                            password = loginInfo[2];
                            if (authenticateUser(username, password)) {
                                // Respond with 200 OK upon the successful login.
                                if (loggedInUser ==0) {
                                    os.println("200 OK. Login successful.");
                                    isLoggedIn = true;
                                    loggedInUser = 1;
                                }
                                else {
                                    os.println("Logout previous User");
                                }
                            } else {
                                // Respond with 410 Wrong upon the invalid credentials.
                                os.println("410 Wrong UserID or Password.");
                            }
                        } if(loginInfo.length != 3) {
                            // Respond with 400 Bad Request upon username/password missing.
                            os.println("400 Bad Request. Invalid arguments.");
                        }
                    }
                    // Server fetches and forwards client's daily messages upon receiving MSGGET.
                    else if (line.equals("MSGGET")) {

                        if (messageIndex < messagesOfTheDay.size()) {
                           // Message at the current index is retrieved.
                            String message = messagesOfTheDay.get(messageIndex);
                            // Respond with 200 OK upon the message retrieved.
                            os.println("200 OK " +message);
                            // Increment the Index count for sequential message printing.
                            messageIndex = (messageIndex + 1) % messagesOfTheDay.size();
                        }
                    }
                    // The MSGSTORE command, which allows logged-in users to store a new message.
                    else if (line.equals("MSGSTORE")) {
                        if (isLoggedIn)  {
                            os.println("200 OK");
                            // Read the new message sent by the user.
                            String newMessage = is.readLine();
                            // Store the new message sent by the user in the text file.
                            storeMessage(newMessage);
                            // Respond with 200 OK after storing the message.
                            os.println("200 OK");
                        } else {
                            os.println("401 You are not currently logged in, login first.");
                        }
                    }
                    // The server will log out the client upon receiving LOGOUT from client.
                    else if (line.equals("LOGOUT")) {
                        if (isLoggedIn) {
                            // Set session flag to false upon LOGOUT.
                            isLoggedIn = false;
                            loggedInUser = 0;
                            os.println("200 OK. Successfully Logout.");
                        }else{
                            os.println("401 You are not currently logged in, login first.");
                        }
                    }
                    // The server and client will terminate upon receiving SHUTDOWN from client.
                    else if (line.equals("SHUTDOWN")) {
                        // check if user is logged in and username is root user.
                        if (isLoggedIn && username.equals("root")){
                            isLoggedIn = false;
                            os.println("200 OK.");
                            System.exit(1);
                            os.println("Server SHUTDOWN");
                        }else {
                            os.println("402 User not allowed to execute this command.");
                            System.out.println(line);
                        }

                    } else {
                        System.out.println(line);
                        os.println("300 Message Format Error");
                    }
                }
                //close input and output stream and socket
                is.close();
                os.close();
                serviceSocket.close();
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        }
    }
    // Function to authenticate a user based on their username and password
    private static boolean authenticateUser(String username, String password) {
        String storedPassword = userCred.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    // Function to read messages from a text file
    private static ArrayList<String> readMessagesFromFile(String fileName) {
         // ArrayList to store messages.
        ArrayList<String> messages = new ArrayList<>();
        // To read file, open it as a FileReader and wrap it in a BufferedReader.
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            //store each line read from the file.
            String line;
            while ((line = br.readLine()) != null) {
                messages.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from file: " + e.getMessage());
        }
        return messages;
    }
    // Function to store a new message in a text file
    private static void storeMessage(String message) {
        try (FileWriter fw = new FileWriter("messages.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);

        } catch (IOException e) {
            System.err.println("Error storing message: " + e.getMessage());
        }
    }
}
