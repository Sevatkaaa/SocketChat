package sheva.labs.client;

import sheva.labs.server.Connection;
import sheva.labs.server.ConsoleHelper;
import sheva.labs.message.Message;
import sheva.labs.message.MessageType;
import sheva.labs.constants.ClientConstants;

import java.io.IOException;
import java.net.Socket;


public class Client implements ClientConstants {

    protected Connection connection;
    protected volatile boolean clientConnected = false;

    public static void main(String[] args) {
        new Client().runClient();
    }

    protected boolean isConsole() {
        return true;
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage(ENTER_PORT);
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage(ENTER_NAME);
        return ConsoleHelper.readString();
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.MESSAGE, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage(SEND_EXCEPTION);
            clientConnected = false;
        }
    }

    public void runClient() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage(RUNNABLE_EXCEPTION);
            System.exit(1);
        }
        if (clientConnected) {
            if (isConsole())
                ConsoleHelper.writeMessage(CONNECTION_OK);
            while (clientConnected) {
                String message = ConsoleHelper.readString();
                if (message.equalsIgnoreCase(EXIT)) {
                    break;
                } else {
                    if (shouldSendTextFromConsole()) {
                        sendTextMessage(message);
                    }
                }
            }
        } else {
            ConsoleHelper.writeMessage(CLIENT_EXCEPTION);
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format(USER_CONNECTED, userName));
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format(USER_LEFT, userName));
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException {
            Message message;

            while (!clientConnected) {
                try {
                    message = connection.receive();
                } catch (ClassNotFoundException e) {
                    throw new IOException(UNEXPECTED_TYPE);
                }
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else {
                    if (message.getType() == MessageType.NAME_ACCEPTED) {
                        notifyConnectionStatusChanged(true);
                    }
                    else {
                        throw new IOException(UNEXPECTED_TYPE);
                    }
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message message;

            while (true) {

                try {
                    message = connection.receive();
                } catch (Exception e) {
                    break;
                }
                if (message.getType() == MessageType.MESSAGE) processIncomingMessage(message.getData());
                else {
                    if (message.getType() == MessageType.USER_ADDED) informAboutAddingNewUser(message.getData());
                    else {
                        if (message.getType() == MessageType.USER_REMOVED) informAboutDeletingNewUser(message.getData());
                        else break;
                    }
                }

            }
            throw new IOException(UNEXPECTED_TYPE);
        }

        public void run()
        {
            int serverPort = getServerPort();
            try {
                Socket socket = new Socket((String)null, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException  | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
