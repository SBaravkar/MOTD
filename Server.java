/*
 * Server.java
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    public static final int SERVER_PORT = 1002;
    public static HashMap<String, String> userCred;
    public static ArrayList<String> messagesOfTheDay;

    public static int messageIndex = 0;
    public static boolean isLoggedIn = false;

    public static void main(String args[])
    {
        ServerSocket myServerice = null;
        String line;
        BufferedReader is;
        PrintStream os;
        Socket serviceSocket = null;
        String username = "";
        String password = "";
        userCred = new HashMap<>();
        // Initialize the userCred with user IDs and passwords
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
                    if ("QUIT".equals(line)) {
                        os.println("200 OK. The Client is QUIT.");
                        break;
                    } else if (line.startsWith("LOGIN")) {
                        String[] loginInfo = line.split(" ");
                        if (loginInfo.length == 3) {
                            username = loginInfo[1];
                            password = loginInfo[2];
                            if (authenticateUser(username, password)) {
                                os.println("200 OK. Login successful.");
                                isLoggedIn = true;
                            } else {
                                os.println("410 Wrong UserID or Password.");
                            }
                        } if(loginInfo.length != 3) {
                            os.println("400 Bad Request. Invalid arguments.");
                        }
                    } else if (line.startsWith("MSGGET")) {
                        if (messageIndex < messagesOfTheDay.size()) {
                           // os.println("200 OK");
                            String message = messagesOfTheDay.get(messageIndex);
                            os.println("200 OK " +message);
                            messageIndex = (messageIndex + 1) % messagesOfTheDay.size();
                        }
                    } else if (line.startsWith("MSGSTORE")) {
                        if (isLoggedIn)  {
                            // Read and store the new message
                            os.println("200 OK");
                            String newMessage = is.readLine();
                            storeMessage(newMessage);
                            os.println("200 OK");
                        } else {
                            os.println("401 You are not currently logged in, login first.");
                        }
                    } else if (line.startsWith("LOGOUT")) {
                        if (isLoggedIn) {
                            //os.println("200 OK");
                            isLoggedIn = false;
                            os.println("200 OK. Successfully Logout.");
                        }else{
                            os.println("401 You are not currently logged in, login first.");
                        }
                    } else if (line.startsWith("SHUTDOWN")) {
                        if (isLoggedIn == true && username.equals("root")){
                            isLoggedIn = false;
                            os.println("200 OK. Server Shutdown!");
                            System.exit(1);
                        }else {
                            os.println("300 Message Format Error");
                        }

                    } else {
                        System.out.println(line);
                        os.println(line);
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
        ArrayList<String> messages = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                messages.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from file: " + e.getMessage());
        }
        return messages;
    }
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

