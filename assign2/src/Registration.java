

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class Registration {


    public static final String REGISTRATION_FILE = "registration.txt"; // File to store registration data

    // Method to register a new user with a username and password
    public static Player registerUser(String username, String password) {
        //username ja existe
        Server.lockDB.lock();
        if (Server.users.containsKey(username)) {
            Server.lockDB.unlock();
            return null;
        } else {

            try (FileWriter writer = new FileWriter(REGISTRATION_FILE, true)) {
                // Append the username and password to the registration file
                writer.write(username + "," + password + System.lineSeparator());
                writer.flush(); //limpa a stream

                // adicionar ao map
                Player p = new Player(username, password);
                Server.users.put(username, p);
                Server.lockDB.unlock();
                return p;
            } catch (IOException e) {
                System.out.println("Failed to register user");
                Server.lockDB.unlock();
                return null;
            }

        }
    }

    public static Map<String, Player> readUserFile() {
        Map<String, Player> users = new HashMap<>();

        File file = new File(REGISTRATION_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating file");
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(REGISTRATION_FILE))) {
            String line;
            while (( line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    users.put(data[0], new Player(data[0], data[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading data");
        }
        return users;
    }
}