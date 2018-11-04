package sheva.labs.server;


import sheva.labs.constants.ServerConstants;
import sheva.labs.message.Message;
import sheva.labs.message.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Server implements ServerConstants {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Server.runServer();
    }

    private static void runServer() {
        ConsoleHelper.writeMessage(INPUT_PORT);
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage(STARTED);
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage(EXCEPTION);
        }
    }

    public static void sendMessageToEveryone(Message message) {
        ConsoleHelper.writeMessage(message.getType() + COLON + message.getData());
        for (Connection connection : connectionMap.values()) {
            try {
                sendMessage(connection, message, false);
            } catch (IOException e) {}
        }
    }

    private static void sendMessage(Connection connection, Message message, boolean shouldWriteToServer) throws IOException {
        if (shouldWriteToServer) {
            ConsoleHelper.writeMessage(message.getType() + COLON + message.getData());
        }
        connection.send(message);
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            if (socket != null && socket.getRemoteSocketAddress() != null) {
                ConsoleHelper.writeMessage(NEW_CONNECTION + socket.getRemoteSocketAddress());
            }
            String userName = null;

            try (Connection connection = new Connection(socket)) {

                userName = serverHandshake(connection);
                sendMessageToEveryone(new Message(MessageType.USER_ADDED, userName));
                sendListOfUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                sendMessageToEveryone(new Message(MessageType.USER_REMOVED, userName));
            } finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                }
                ConsoleHelper.writeMessage(CLOSED_CONNECTION + socket.getRemoteSocketAddress());
            }
        }


        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                sendMessage(connection, new Message(MessageType.NAME_REQUEST), false);
                Message answer = connection.receive();
                ConsoleHelper.writeMessage(MessageType.NAME_REQUEST + COLON + answer.getData());
                if (answer.getType() == MessageType.USER_NAME) {
                    if (!answer.getData().isEmpty()) {
                        if (!connectionMap.containsKey(answer.getData())) {
                            connectionMap.put(answer.getData(), connection);
                            sendMessage(connection, new Message(MessageType.NAME_ACCEPTED, answer.getData()), true);
                            return answer.getData();
                        } else {
                            ConsoleHelper.writeMessage(MessageType.NAME_DENIED + COLON + answer.getData());
                        }
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (!entry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message != null && message.getType() == MessageType.MESSAGE) {
                    sendMessageToEveryone(new Message(MessageType.MESSAGE, userName + COLON + message.getData()));
                } else {
                    ConsoleHelper.writeMessage(EXCEPTION);
                }
            }
        }
    }
}
