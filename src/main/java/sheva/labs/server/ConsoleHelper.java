package sheva.labs.server;

import sheva.labs.constants.ConsoleHelperConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ConsoleHelper implements ConsoleHelperConstants {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        while (true) {
            try {
                return reader.readLine();
            }
            catch (IOException e) {writeMessage(String.format(EXC, TEXT));}
        }
    }

    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {writeMessage(String.format(EXC, NUMBER));}
        }
    }
}
