
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Authentication {

    public Authentication() {

    }

    public static TokenWithExpiration generateToken(String playerName, int expirationDays) {
        String token = generateRandomToken(); // Generate a random token
        LocalDateTime expirationDate = LocalDateTime.now().plus(30, ChronoUnit.SECONDS);
        TokenWithExpiration tokenWithExpiration = new TokenWithExpiration(token, expirationDate);

        return tokenWithExpiration;
    }

    // Method to authenticate a player with a username and password
    public static boolean authenticatePlayer(String username, String password) {
        Server.lockDB.lock();
        if(Server.users.containsKey(username)){
            Player player = Server.users.get(username);
            //conferir se username e passe estao certas e (FALTA) que o player nao esta ja logado
            if(player.getUsername().equals(username) && player.getPassword().equals(password)) {
                Server.lockDB.unlock();
                return true;
            }
        }

        Server.lockDB.unlock();
        return false;
    }

    // Method to generate a random token (example implementation)
    private static String generateRandomToken() {

        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int TOKEN_LENGTH = 10;

        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char c = CHARACTERS.charAt(index);
            sb.append(c);
        }

        return sb.toString();
    }

    public static void writeTokenToFile(String username, String token){
        try (FileWriter fileWriter = new FileWriter("token_" + username + ".txt")) {
            fileWriter.write(token);
        } catch (IOException e) {
            // Handle the exception appropriately
        }
    }

}