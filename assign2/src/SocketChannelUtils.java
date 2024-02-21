import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SocketChannelUtils {

    public static void sendString(SocketChannel socketChannel, String message) throws IOException {

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageBytes.length);
        // Write the length of the message as the first 4 bytes
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    public static String receiveString(SocketChannel socketChannel) throws IOException {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);

        // Read the length of the incoming message
        while (lengthBuffer.hasRemaining()) {
            if (socketChannel.read(lengthBuffer) == -1) {
                // The channel has been closed
                try {
                    socketChannel.socket().close();
                } catch (IOException e){
                }
                throw new IOException("SocketChannel has been closed");
            }
        }

        lengthBuffer.flip();
        int messageLength = lengthBuffer.getInt();

        ByteBuffer messageBuffer = ByteBuffer.allocate(messageLength);

        // Read the message content if there is data available
        while (messageBuffer.hasRemaining()) {
            if (socketChannel.read(messageBuffer) == -1) {

                throw new IOException("SocketChannel has been closed");
            }
        }

        messageBuffer.flip();
        byte[] messageBytes = new byte[messageLength];
        messageBuffer.get(messageBytes);

        return new String(messageBytes, StandardCharsets.UTF_8);
    }

}