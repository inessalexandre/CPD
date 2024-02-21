import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerListener implements Runnable {
    private SocketChannel socketChannel;
    protected volatile String message;

    public ServerListener(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        try {
            listenToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToServer() throws IOException {
        while (true) {
            message = SocketChannelUtils.receiveString(socketChannel);
            if(message == null){
                message = "aaa";
            }
            if (message.startsWith("game ended")) {
                return;
            }


        }
    }

    public String getMessage() {
        return message;
    }
}
