import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Player {
    private String username;
    private String password;
    private boolean isLoggedIn;
    private TokenWithExpiration token;
    private SocketChannel channel;
    private Socket socket;
    private int points;

    public Player(String username, String password) {
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
        this.points = 0;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Socket getSocket() {
        return socket;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setToken(TokenWithExpiration token) {
        this.token = token;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setChannel(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public TokenWithExpiration getToken() {
        return token;
    }

    public void login() {
        this.isLoggedIn = true;
    }

    public void logout() {
        this.isLoggedIn = false;
    }

    public String getRank(){
        if (points >= 50) {
            return "A";
        } else if (points >= 30) {
            return "B";
        } else if (points >= 20) {
            return "C";
        } else {
            return  "D";
        }
    }

    public void setWinningPoints(){
        this.points += 10;
    }

    public void setLosingPoints(){
        if(this.points - 3 < 0){
            this.points = 0;
        }
        else {
            this.points -= 3;
        }

    }

    public boolean isSocketChannelOpen() {
        try {
            // Attempt a non-blocking read operation
            this.channel.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1);
            int bytesRead = this.channel.read(buffer);

            return bytesRead != -1; // If bytesRead is -1, the client has disconnected
        } catch (IOException e) {
            return false;
        }
    }
}