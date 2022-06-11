package lab7.Server.VehicleCollectionServer;

import lab7.Client.VehicleCollectionClient.ClientConnectionHandler;
import lab7.Essentials.SignedRequest;
import lab7.Exceptions.EOFInputException;
import lab7.UserInput.BufferedFileReader;
import lab7.UserInput.UserInput;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Thread.sleep;

public class ServerConnectionHandler {

    private static final Logger logger = LogManager.getLogger(ServerConnectionHandler.class);

    static boolean serverStarted = false;
    static private SocketChannel socketChannel = null;
    static private ServerSocketChannel serverSocketChannel = null;
    static private ExecutorService executor = null;
    static Map<SocketAddress, CommunicationWithClient> communicators = new HashMap<>();
    static Queue<SignedRequest> requestsQueue = new LinkedList<>();

    public static void startServer(){
        while (true) {
            try {
                System.out.println("Enter port to start server:");
                System.out.print("->");
                String[] words = UserInput.readLine().split(" +");
                int port = Integer.parseInt(words[0]);

                InetSocketAddress inetAddress = new InetSocketAddress(port);
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(inetAddress);
                serverSocketChannel.configureBlocking(false);

                logger.info("Server is started at " + inetAddress.getAddress() + ":" + serverSocketChannel.socket().getLocalPort());
                serverStarted = true;

                communicators.clear();

                executor = Executors.newCachedThreadPool();

                break;
            } catch (Exception e) {
                if (e.getClass() == EOFInputException.class){
                    break;
                }
                logger.error("Unable to start server: " + e.getMessage());
                logger.info("Please, try again.");
            }
        }
    }


    public static boolean isServerStarted(){
        return serverStarted;
    }


    public static SocketAddress acceptConnection(){
        try {
            socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                logger.info("Client connecting...");
                socketChannel.configureBlocking(false);
                SocketAddress clientID = socketChannel.getRemoteAddress();

                CommunicationWithClient communication;
                communication = new CommunicationWithClient(socketChannel);

                communicators.put(clientID, communication);
                executor.execute(communication);

                return clientID;
            }
        }
        catch(Exception e){
            if (e instanceof SocketTimeoutException);
            else if(e instanceof IOException){
                logger.error("Error occurred while connecting to client: " + e);
            }else{
                logger.error("Server error: " + e);
                e.printStackTrace();
                //serverStarted = false;
            }
        }
        return null;
    }

    public static void close() {
        try {
            serverSocketChannel.close();
            Collection<CommunicationWithClient> cList = communicators.values();
            for(CommunicationWithClient c : cList){
                c.disconnect();
            }
        }
        catch (Exception e){}
    }

    public static void update(){
        Collection<CommunicationWithClient> cList = communicators.values();
        for(CommunicationWithClient c : cList){
            c.read();
        }
    }
}



class CommunicationWithClient implements Runnable{

    private static final Logger logger = LogManager.getLogger(CommunicationWithClient.class);

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ByteArrayOutputStream baos;
    private ByteArrayInputStream bais;
    private ByteBuffer rxBuffer;
    private ByteBuffer txBuffer;
    private SocketChannel socketChannel;
    private SocketAddress clientID;


    public CommunicationWithClient(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        clientID = socketChannel.getRemoteAddress();

        logger.info("\tChannel has been created: " + socketChannel);

        txBuffer = ByteBuffer.allocate(2000);
        rxBuffer = ByteBuffer.allocate(2000);
        txBuffer.clear();
        rxBuffer.clear();


        baos = new ByteArrayOutputStream();
        outputStream = new ObjectOutputStream(baos);
        outputStream.flush();
        txBuffer.put(baos.toByteArray());
        baos.reset();

        bais = new ByteArrayInputStream(rxBuffer.array());
        try {
            Thread.sleep(500);
        }catch (Exception e){}
        //inputStream = null;
        //do {
            update();
        //} while(rxBuffer.get(0) != -84 && rxBuffer.get(1) != -19 && rxBuffer.get(3) != 0 && rxBuffer.get(3) != 5);

        try {
            inputStream = new ObjectInputStream(bais);
            rxBuffer.position(rxBuffer.limit());
            rxBuffer.compact();
        }catch (Exception e) {
            disconnect();
            throw new RuntimeException("Unable to connect");
        }

        logger.info("\tClient connected");
    }

    public void disconnect(){
        logger.info("Disconnecting from client");
        try {
            ServerConnectionHandler.communicators.remove(clientID);
            inputStream.close();
            outputStream.close();
            socketChannel.close();
            txBuffer.clear();
            rxBuffer.clear();
            baos.close();
            bais.close();
            logger.info("\tDisconnected");
        }
        catch(Exception e){
            if(e instanceof NullPointerException);
            else logger.error("\tError occurred while closing socket: " + e.getMessage());
        }
    }

    public void write(Object obj){
        try {
            outputStream.writeObject(obj);
            outputStream.reset();
            outputStream.flush();
            int written = 0;
            while(baos.size() - written > 0){
                synchronized (this.txBuffer) {
                    int s = baos.size();
                    int r = txBuffer.remaining();
                    int a = Math.min(s - written, r);
                    txBuffer.put(baos.toByteArray(), written, a);
                    written += a;
                }
            }
            baos.reset();
        }
        catch(Exception e){
            logger.error("Error occurred while serializing object: ");
        }
    }


    public Object read(){
        Object obj = null;
        try {
            synchronized (this.rxBuffer) {
                rxBuffer.flip();
                if (rxBuffer.remaining() != 0) {
                    byte[] bytes = new byte[rxBuffer.remaining() + 6];
                    for (int i = rxBuffer.position(); i < rxBuffer.limit(); i++)
                        bytes[i + 4] = rxBuffer.get(i);

                    bytes[0] = -84;
                    bytes[1] = -19;
                    bytes[2] = 0;
                    bytes[3] = 5;
                    obj = SerializationUtils.deserialize(bytes);
                    ServerConnectionHandler.requestsQueue.add(new SignedRequest(clientID, obj));
                }
                rxBuffer.position(rxBuffer.limit());
                rxBuffer.compact();
            }
        }
        catch(Exception e){
            logger.error("Error occurred while deserializing object: " + e.getMessage());
        }
        return obj;
    }

    public void run(){
        while(true) {
            try {
                int n;
                synchronized (this.rxBuffer) {
                    n = socketChannel.read(rxBuffer);
                    if (n == -1) throw new IOException("Unable to read");
                }
                synchronized (this.txBuffer) {
                    txBuffer.flip();
                    n = socketChannel.write(txBuffer);
                    if (n == -1) throw new IOException("Unable to send");
                    txBuffer.compact();
                }

            } catch (Exception e) {
                if (e instanceof IOException || e instanceof NullPointerException) {
                    logger.error("Connection with client is lost: " + e.getMessage());
                    disconnect();
                    break;
                } else {
                    logger.error("Error occurred while communicating with client: " + e.getMessage());
                }
            }
        }
    }

    public void update(){
            try {
                int n;
                synchronized (this.rxBuffer) {
                    n = socketChannel.read(rxBuffer);
                    if (n == -1) throw new IOException("Unable to read");
                }
                synchronized (this.txBuffer) {
                    txBuffer.flip();
                    n = socketChannel.write(txBuffer);
                    if (n == -1) throw new IOException("Unable to send");
                    txBuffer.compact();
                }

            } catch (Exception e) {
                if (e instanceof IOException || e instanceof NullPointerException) {
                    logger.error("Connection with client is lost: " + e.getMessage());
                    disconnect();
                    //break;
                } else {
                    logger.error("Error occurred while communicating with client: " + e.getMessage());
                }
            }
    }
}
