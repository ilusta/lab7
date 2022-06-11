package lab7.Server.VehicleCollectionServer;

import lab7.Commands.*;
import lab7.Essentials.ConfigurationFileReader.ConfigurationFileReader;
import lab7.Essentials.Request;
import lab7.Essentials.SignedRequest;
import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Server.Database.Database;
import lab7.UserInput.UserInput;

import java.io.*;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VehicleCollectionServer {

    private static final Logger logger = LogManager.getLogger(VehicleCollectionServer.class);

    final ArrayList<Command> commandList = new ArrayList<>();
    final ArrayList<Command> clientCommandList = new ArrayList<>();

    private static String pepper = "J(3kW-.H;xq&[pfj";
    //private static Map<String, String> passwords = new HashMap<>();

    private static Connection connection;

    static MessageDigest md;

    public void run() {

        System.out.println("Welcome to the Vehicle Collection Server!");
        logger.info("Server initialization");

        try {
            //Check for other server already running
            String userHome = System.getProperty("user.home");
            File file = new File(userHome, "my.lock");
            FileChannel fc = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            FileLock lock = fc.tryLock();
            if (lock == null) {
                throw new RuntimeException("another instance is running");
            }

            //algorithm for encrypting passwords
            md = MessageDigest.getInstance("SHA-384");

            //User`s inputs reader
            UserInput.setDefaultReader(new BufferedReader(new InputStreamReader(System.in)));

            //Connect to database
            ConfigurationFileReader confReader = new ConfigurationFileReader("./conf.txt");
            String dbUser = confReader.readParameter("DB_USER");
            String dbPassword = confReader.readParameter("DB_PASSWORD");
            Database database = new Database("jdbc:postgresql://localhost:5432/collection", dbUser, dbPassword);
            connection = database.getConnection();

            //Create vehicle collection and load values from database
            final VehicleCollection collection = new VehicleCollection(connection);

            //Server commands
            commandList.add(new Help());
            commandList.add(new Exit());
            commandList.add(new History());
            //Commands available to clients
            clientCommandList.add(new Info());
            clientCommandList.add(new Show());
            clientCommandList.add(new Insert());
            clientCommandList.add(new Update());
            clientCommandList.add(new RemoveKey());
            clientCommandList.add(new Clear());
            clientCommandList.add(new SumOfNumberOfWheels());
            clientCommandList.add(new MaxByCoordinates());
            clientCommandList.add(new FilterByType());
            clientCommandList.add(new RemoveGreaterKey());
            clientCommandList.add(new RemoveLower());
            clientCommandList.add(new RegisterUser());
            clientCommandList.add(new LogIn());

            ArrayList<Command> allCommandList = new ArrayList<>();
            allCommandList.addAll(commandList);
            allCommandList.addAll(clientCommandList);

            Help.attachCommandList(allCommandList);
            Info.attach(collection);
            Show.attach(collection);
            Insert.attach(collection);
            Update.attach(collection);
            RemoveKey.attach(collection);
            Clear.attach(collection);
            SumOfNumberOfWheels.attach(collection);
            MaxByCoordinates.attach(collection);
            FilterByType.attach(collection);
            RemoveGreaterKey.attach(collection);
            RemoveLower.attach(collection);

            //Command builder and executor
            CommandExecutor executor = new CommandExecutor();
            CommandBuilder builder = new CommandBuilder();
            CommandBuilder.setCommandList(allCommandList);

            //Starting server
            ServerConnectionHandler.startServer();

            logger.info("Server initialization completed");
            while(Exit.getRunFlag() && ServerConnectionHandler.isServerStarted()) {

                //Reconnect to database if connection is lost
                if (connection.isClosed()) {
                    try {
                        logger.error("Connection with database lost. Reconnecting...");
                        database.close();
                        database = new Database("jdbc:postgresql://localhost:5432/collection", dbUser, dbPassword);
                        connection = database.getConnection();
                    } catch (Exception e) {
                        logger.error("Error occurred: " + e);
                    }
                }

                //Listen for new connection
                SocketAddress clientID = ServerConnectionHandler.acceptConnection();
                if (clientID != null) {

                    logger.info("Sending available commands to client " + clientID);
                    for (Object c : clientCommandList) {
                        ServerConnectionHandler.communicators.get(clientID).write(c);
                    }
                    ServerConnectionHandler.communicators.get(clientID).write("End");
                    logger.info("\tDone");
                }

                ServerConnectionHandler.update();

                //Execute client`s commands
                while (ServerConnectionHandler.requestsQueue.size() > 0) {
                    new Thread(new executeClientsCommand(ServerConnectionHandler.requestsQueue.poll(), executor)).start();
                }

                //Execute server`s commands
                try {
                    if (UserInput.available()) {
                        logger.info("Reading local command");
                        System.out.println(executor.execute(builder.build()));
                        logger.info("Local command executed");
                    }
                } catch (Exception e) {
                    if (e instanceof EOFInputException) break;
                    logger.error("Error while executing command: " + e.toString());
                }
            }


            //free resources
            database.close();
            ServerConnectionHandler.close();
            UserInput.removeReader();
            logger.info("Goodbye!");

        } catch (Exception e) {
            logger.error(e);
        }
    }



    private class executeClientsCommand implements Runnable{
        SignedRequest signedRequest;
        CommandExecutor executor;

        public executeClientsCommand(SignedRequest signedRequest, CommandExecutor executor){
            this.signedRequest = signedRequest;
            this.executor = executor;
        }

        public void run(){
            try{
                SocketAddress clientID = signedRequest.getClientID();
                Request request = (Request) signedRequest.getRequest();

                if(request != null) {
                    logger.info("Received command from client " + clientID);

                    if(isUserRegistered(request) || request.getInfo() instanceof RegisterUser) {
                        Command command = (Command) request.getInfo();
                        if (command instanceof Exit)
                            throw new CommandExecutionException("\tDeprecated command");

                        if(command instanceof  SecurityCollectionCommand)
                            ((SecurityCollectionCommand) command).setUser(request.getUser());

                        logger.info("\tExecuting command");
                        ServerConnectionHandler.communicators.get(clientID).write(executor.execute(command));
                        logger.info("\tResponse sent to client " + clientID);
                    }
                    else {
                        ServerConnectionHandler.communicators.get(clientID).write("Error: Not registered user\n");
                        logger.info("\tUser is not registered");
                    }
                }
            } catch (Exception e) {
                //ServerConnectionHandler.write("Error occurred while executing command\n");
                logger.error("Error occurred while executing client`s command: " + e.getMessage());
            }
        }
    }


    public static String registerUser(String user, String password) throws CommandExecutionException{
        if(user == null || password == null) return "Username and password can not be null\n";

        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT username, password from users")){

            while (resultSet.next()) {
                String s = resultSet.getString("username");
                if (user.equals(s)) return "User with this name already exists\n";
            }

            String hash = new String(md.digest((password + pepper).getBytes(StandardCharsets.UTF_8)));
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)");
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, hash);
            preparedStatement.execute();

        }
        catch(Exception e){
            throw new CommandExecutionException("Unable to register: " + e);
        }
        return "User registered\n";
    }

    private boolean isUserRegistered(Request request) throws RuntimeException{
        String user = request.getUser();
        String password = request.getPassword();
        if(user == null && password == null) return false;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT username, password from users")){

            String hash = new String(md.digest((password + pepper).getBytes(StandardCharsets.UTF_8)));
            while (resultSet.next()) {
                String u = resultSet.getString("username");
                String p = resultSet.getString("password");
                if (user.equals(u) && hash.equals(p)) return true;
            }
        }
        catch(Exception e){
            throw new RuntimeException("Unable to check user: " + e);
        }
        return false;
    }
}


















