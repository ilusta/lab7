package lab7.Client.VehicleCollectionClient;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.ConnectionException;
import lab7.Exceptions.InputException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnectionHandler {

    static boolean connected = false;
    static private ObjectInputStream inputStream = null;
    static private ObjectOutputStream outputStream = null;
    static private Socket socket = null;


    public static void connect(String[] words) throws CommandExecutionException{
        System.out.println("Connecting to server:");
        byte[] ip = new byte[4];
        int port;

        try {
            connected = false;
            if(words.length != 5) throw new InputException("Wrong IP or port.");
            ip[0] = (byte) Integer.parseUnsignedInt(words[0]);
            ip[1] = (byte) Integer.parseUnsignedInt(words[1]);
            ip[2] = (byte) Integer.parseUnsignedInt(words[2]);
            ip[3] = (byte) Integer.parseUnsignedInt(words[3]);
            port = Integer.parseUnsignedInt(words[4]);

            System.out.println("\tConnecting to " + (0xFF & (int)ip[0]) +
                    "." + (0xFF & (int)ip[1]) +
                    "." + (0xFF & (int)ip[2]) +
                    "." + (0xFF & (int)ip[3]) +
                    ":" + port);

            InetAddress inetAddress = InetAddress.getByAddress(ip);
            socket = new Socket(inetAddress, port);
            System.out.println("\tSocket has been created: " + socket);

            socket.setSoTimeout(60000);
            outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            outputStream.flush();
            inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            System.out.println("\tConnected to server");
            connected = true;
        }
        catch (Exception e) {
            throw new CommandExecutionException("Unable to connect: " + e.getMessage());
        }
    }


    public static void disconnect(){
        System.out.println("Disconnecting from server:");
        try {
            socket.close();
            connected = false;
            System.out.println("\tDisconnected");
        }
        catch(Exception e){
            System.out.println("\tError occurred while closing socket: " + e.getMessage());
        }
    }


    public static boolean isConnected(){
        return connected;
    }


    public static void write(Object obj) throws ConnectionException{
        try {
            outputStream.writeObject(obj);
            outputStream.reset();
            outputStream.flush();
        }
        catch(Exception e){
            if(e instanceof IOException || e instanceof NullPointerException){
                throw new ConnectionException("Connection with server is lost.");
            }
            else {
                System.out.println("Error occurred while sending object to server: " + e.getMessage());
            }
        }
    }


    public static Object read() throws ConnectionException{
        Object obj = null;
        try{
            obj = inputStream.readObject();
        }
        catch(Exception e){
            if(e instanceof IOException || e instanceof NullPointerException){
                throw new ConnectionException("Connection with server is lost.");
            }
            else {
                System.out.println("Error occurred while reading object from server: " + e.getMessage());
            }
        }
        return obj;
    }

}
