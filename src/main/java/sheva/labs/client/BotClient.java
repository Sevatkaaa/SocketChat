package sheva.labs.client;

import sheva.labs.server.ConsoleHelper;
import sheva.labs.constants.BotConstants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class BotClient extends Client implements BotConstants {

    private static Integer id = 0;

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format(NAME, String.valueOf(++id));
    }

    public static void main(String[] args) {
        new BotClient().runClient();
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage(HELLO);
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            if (message != null) {
                ConsoleHelper.writeMessage(message);
                SimpleDateFormat format = null;
                if (message.contains(COLON)) {
                    String[] words = message.split(COLON);
                    if (words.length == 2 && words[1] != null) {
                        String name = words[0];
                        String text = words[1];
                        switch (text) {
                            case DATE:
                                format = new SimpleDateFormat(DATE_FORMAT);
                                break;
                            case DAY:
                                format = new SimpleDateFormat(DAY_FORMAT);
                                break;
                            case MONTH:
                                format = new SimpleDateFormat(MONTH_FORMAT);
                                break;
                            case YEAR:
                                format = new SimpleDateFormat(YEAR_FORMAT);
                                break;
                            case TIME:
                                format = new SimpleDateFormat(TIME_FORMAT);
                                break;
                            case HOUR:
                                format = new SimpleDateFormat(HOUR_FORMAT);
                                break;
                            case MINUTE:
                                format = new SimpleDateFormat(MINUTE_FORMAT);
                                break;
                            case SECOND:
                                format = new SimpleDateFormat(SECOND_FORMAT);
                                break;
                        }
                        if (format != null) {
                            sendTextMessage(String.format(INFORMATION, name, format.format(Calendar.getInstance().getTime())));
                        }
                    }
                }
            }
        }
    }
}
