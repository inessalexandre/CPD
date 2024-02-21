
import java.io.*;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;



public class GameThread extends Thread {

    private List<Socket> sockets;
    String gameId;
    List<Player> gamePlayers;
    private BufferedReader reader;
    private PrintWriter writer;
    private ServerSocketChannel serverSocketChannel;
    private int num = generateRandomNumber();
    private Player winner;

    public GameThread(List <Player> players, String gameId){
        this.gameId = gameId;
        this.gamePlayers = players;
    }

    public void run() {
        boolean endGame = false;
        int counter = 0;
        try {
            Selector selector = Selector.open();
            //start game and register player's socket channels
            registerSocketChannel(selector);

            while (counter  != gamePlayers.size()){
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(!key.isValid()){
                        continue;
                    }
                    if(key.isReadable()){
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        String response = read(key);
                        //se o jogo ainda nao acabou, continuo a ver guesses
                        if(!endGame){
                            response = readGuess(key, response);
                            if (response.startsWith("Correct")) {
                                winner = getPlayerByChannel(clientChannel);
                                //give points to the winner
                                winner.setWinningPoints();

                                for (Player player: gamePlayers){
                                    //remove points from the losers
                                    if(!player.equals(winner)){
                                        player.setLosingPoints();
                                    }

                                    SocketChannelUtils.sendString(player.getChannel(), "game ended - PLAYER " + winner.getUsername() +" guessed the right number (" + num + ")");
                                }
                                endGame = true;
                            }
                            else {
                                SocketChannelUtils.sendString(clientChannel, response);
                            }
                        }
                        //quando o jogo acabar, trato de ver se os jogadores querem jogar de novo ou nao
                        else {
                            if(response.equals("yes")){
                                Player player = getPlayerByChannel(clientChannel);

                                Server.lockPlayersQueue.lock();
                                Server.playersQueue.add(player);
                                Server.lockPlayersQueue.unlock();
                                counter++;
                            }
                            else if(response.equals("no")){
                                Player player = getPlayerByChannel(clientChannel);
                                player.logout();
                                counter++;
                            }
                        }

                    }
                }
            }

        } catch (SocketException e){
            //message to players that a player left
            messagePlayerLeft();

            for (Player player: gamePlayers){
                //search for the player that left and log him out and delete token
                //and the ones who didnt left, ask if they want to play again
                playAgain(player);

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void messagePlayerLeft(){
        for (Player player: gamePlayers){
            SocketChannel socketChannel = player.getChannel();
            if(player.isSocketChannelOpen()){
                try {
                    SocketChannelUtils.sendString(socketChannel, "game ended - a player left the game");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    private String readGuess(SelectionKey key, String guess) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        if (guess != null) {
            int clientNumber = Integer.parseInt(guess);
            String message;

            if (clientNumber == num) {
                message = "Correct guess!";
            } else if (clientNumber > num) {
                message = "Too high!";
            } else if (clientNumber < num) {
                message = "Too low!";
            } else {
                message = "Invalid";
            }
            return message;

        }

        return "";
    }


    private String read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        String messageReceived = SocketChannelUtils.receiveString(clientChannel);

        return messageReceived;
    }

    public void registerSocketChannel(Selector sel) throws IOException{
        for (Player player: gamePlayers){
            SocketChannel socketChannel = player.getChannel();

            socketChannel.configureBlocking(false);
            socketChannel.register(sel, SelectionKey.OP_READ);

            SocketChannelUtils.sendString(socketChannel, "game started");
        }
    }

    // function to generate random number from 0 to 100
    public static int generateRandomNumber() {
        Random rand = new Random();
        return rand.nextInt(100);
    }

    public static void playAgain(Player player) {
        if(!player.isSocketChannelOpen()){
            player.setToken(null);
            player.logout();
        }
        else {
            try {
                while (true){
                    String playAgain = SocketChannelUtils.receiveString(player.getChannel());
                    if(playAgain.equals("yes")){
                        Server.lockPlayersQueue.lock();
                        Server.playersQueue.add(player);
                        Server.lockPlayersQueue.unlock();
                        break;
                    }
                    else if(playAgain.equals("no")){
                        player.logout();
                        break;
                    }
                }


            } catch (IOException ex) {

                ex.printStackTrace();
            }

        }
    }

    public Player getPlayerByChannel(SocketChannel channel) {
        for (Player player : gamePlayers) {
            if (player.getChannel() == channel) {
                return player;
            }
        }
        return null;
    }


}

