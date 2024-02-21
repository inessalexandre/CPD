# Distributed Systems Assignment

## Group T03G16
- Bruna Brasil Leão Marques (202007191)
- Francisca Horta Guimarães (202004229)
- Inês Alexandre Queirós Matos Macedo de Oliveira (202103343)

## Credentials

| Username | Password |
| -------- | -------- |
| bru     | 123     |
| kika     | 123     |
| ines     | 123     |


## Instructions

### Run Server

<kbd>java Server 8000</kbd>

### Run Client

<kbd>java Client localhost 8000</kbd>

## Introduction


In this project, we developed a client-server system for an online game using TCP sockets in Java. We developed a text-based guessing numbers game (from 0 to 100) that users can play after authenticating with the server. Our server accepts and authenticates user connections and assembles groups of 3 players for the game. To create and run new game instances, we request threads from a thread pool. The size of the thread pool determines the maximum number of games that can run concurrently on our server. 

    
## Barrier and Matchmaking

The server provides two alternative operating modes when launching: simple and ranked mode.  

- In simple game mode the first 3 users that connect to the system are assigned to the first game instance and so forth for the next batches of 3 users.

- In ranked game mode we keep levels associated with the players (A,B,C,D) based on the points they get from winning or losing the game. We use a matchmaking algorithm to create these teams of players. We use a PriorityQueue to order the players based on their rank. 

### Matchmaking Algorithm

In our ranked mode, we implemented a matchmaking algorithm that aims to create fair and competitive games. Our algorithm attempts to match players with similar skill levels, and it achieves this by comparing the rank levels of players.

- It starts by calculating the differences in ranks between the first and third player in groups of three. This is done in the ***calculateRankDifference()*** method. It returns a list of absolute rank differences among every three players in the queue.
- If the number of players is greater than or equal to three, our algorithm proceeds to match players based on the calculated rank differences.
- The algorithm iterates over the list of calculated rank differences, and checks if there is a perfect match - a situation where the rank difference between players is zero. If a perfect match is found, it means the players are of the same skill level and thus can be matched together.
- In case of a perfect match, our algorithm creates a new game instance for these players. It removes these players from the queue, adds them to a new game, and submits a new game task to the executor for execution.
- If a perfect match is not found, our algorithm then looks at the number of tries it has left since it started looking for a match. If it's been over the maximum number of tries (3), the algorithm decides to relax the matching criteria.
- When the criteria is relaxed, the algorithm finds the group of players with the smallest rank difference and creates a match with them. It removes these players from the queue, adds them to a new game, and submits a new game task to the executor.

In both scenarios, once a game is created, the algorithm restarts the timer to continue the process for the remaining players in the queue.

    
## Fault Tolerance


In terms of fault tolerance, we've developed a protocol between the client and server that safeguards a player's position in the game wait queue even when unexpected disconnections occur.

The protocol is centered around a **TokenWithExpiration** object that each player is assigned upon successful login. This object comprises two fields: a unique *String token* that represents the session identifier, and a *LocalDateTime expirationDate* which specifies the validity period of the token.

When a player first logs in, we create a file named "token_*username*.txt" on the client side where 'username' is the player's username. This file stores their unique token. The presence of this file signals to the system that a user has previously logged in and possesses a token.

In the event of a broken connection, when a user tries to log back in, it first checks whether a token file associated with their username exists. If it does, it means that the user has logged in before and offers the user an option to either resume the previous connection and maintain their place in the queue, or create a new connection which involves generating a new token and a new place on the queue. If they choose the first option and the token is still valid, the user logs back in and resumes the previous connection.

### Queue Handler

Our QueueHandler file is responsible for treating the playersQueue. If a player in the queue is disconnected, it logs them out. But, when the token expires, the player is removed from the queue, losing their place. In this case, the user will need to log in again to receive a new token and re-enter the game queue. In the other hand, if the player is still connected and in the queue, if the token expires, we regenerate a new token and the player does not lose his position in the queue.

This file is also responsible for the matchmaking in the rank mode.
    
## Concurrency

To eliminate race conditions, we have employed synchronization mechanisms in critical sections of our code. For instance, when registering new users, the *registerUser* method in the Registration class is guarded by a **ReentrantLock** named lockDB. This lock ensures that only one thread can access the shared users map at a time, preventing data corruption or inconsistency when multiple threads attempt to register users concurrently. Similarly, in the *authenticatePlayer* method of the Authentication class, we use the lockDB lock to ensure synchronized access to the users map during player authentication. This guarantees that player lookup and comparison operations are thread-safe. **// falar tmb da lockPlayersQueue**


During the game, we have utilized the non-blocking **SocketChannel** provided by the *java.nio.channels* package for the communication between the client (player) and the gameThread. By employing non-blocking I/O and multiplexed channels, we can handle concurrent connections without the need for excessive thread creation and termination.

To prevent slow clients from causing performance issues in the system, we have implemented a timeout mechanism. If the reception of a client's message exceeds a certain duration (in this case, 7 seconds), we terminate the corresponding thread handling the communication, in this case the authentication thread. This ensures that the system is not held up by slow or unresponsive clients, allowing it to efficiently process requests from other clients and maintain overall responsiveness.
        
## Game

In this game, each player can guess any number of times and sends to the game a message with the guess. The game will then respond with a message saying if the guess number is lower or higher than the right number. If a player's guess is right, all game players will recieve a message that the game ended and who is the winner. The winner gets points and the losers lose points. At the end, for each player is asked if they want to play again. If so they are added to the queue and if not they are logged out.
This way, the users can play a sequence of game instances. 

        
## User registration and authentication

During the authentication process, the communication between the AuthenticationThread (server) and each client is done through TCP sockets.

The system supports user registration, allowing new users to provide a username and password. The registration process is handled by the registerUser method in the Registration class. When a user registers, their username and password are stored in a registration file called registration.txt for future reference.

For authentication, users can send their username and password to authenticate themselves. It verifies the user's credentials against the registration data stored in the file and grants access if the credentials are valid. 

Upon successful authentication, the user receives a token generated by the TokenWithExpiration class. This token includes an expiration date and serves as a session identifier.

If the authentication fails, it asks for user data again and this process restarts.


## Architecture Design

### Communication Authentication Thread / Client 
![](https://hackmd.io/_uploads/HkPxPgOS3.png)

### Communication Game Thread / Client 
![](https://hackmd.io/_uploads/rJ6HDx_Hn.png)

### Flow Diagram
![](https://hackmd.io/_uploads/HJc3gZuS3.png)
