package lab7.Server.VehicleCollectionServer;

import lab7.Commands.*;
import lab7.Essentials.Request;
import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.UserInput.UserInput;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VehicleCollectionServer {

    private static final Logger logger = LogManager.getLogger(VehicleCollectionServer.class);

    final ArrayList<Command> commandList = new ArrayList<>();
    final ArrayList<Command> clientCommandList = new ArrayList<>();

    private static String pepper = "J(3kW-.H;xq&[pfj";
    private static Map<String, String> passwords = new HashMap<>();

    static MessageDigest md;

    public void run() {

        System.out.println("Welcome to the Vehicle Collection Server!");
        logger.info("Server initialization");

        String userHome = System.getProperty("user.home");
        File file = new File(userHome, "my.lock");
        try {
            FileChannel fc = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            FileLock lock = fc.tryLock();
            if (lock == null) {
                throw new RuntimeException("another instance is running");
            }

            md = MessageDigest.getInstance("SHA-384");

            UserInput.setDefaultReader(new BufferedReader(new InputStreamReader(System.in)));

            final VehicleCollection collection = new VehicleCollection();

            try {
                final String lockfile = EnvVarReader.getValue("VEHICLE_COLLECTION_PATH");
                collection.setFileName(lockfile);
                collection.open();
            } catch (Exception e) {
                logger.error("Error occurred while reading file: " + e.getMessage());
            }

            commandList.add(new Help());
            commandList.add(new Exit());
            commandList.add(new History());
            commandList.add(new Save());

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
            Save.attach(collection);
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

            CommandExecutor executor = new CommandExecutor();
            CommandBuilder builder = new CommandBuilder();
            CommandBuilder.setCommandList(allCommandList);

            ServerConnectionHandler.startServer();

            logger.info("Server initialization completed");
            while(Exit.getRunFlag() && ServerConnectionHandler.isServerStarted()) {

                if (!ServerConnectionHandler.isConnected()) {
                    ServerConnectionHandler.listenForConnection();

                    if(ServerConnectionHandler.isConnected()) {
                        logger.info("Sending available commands to client");
                        for (Object c : clientCommandList) {
                            ServerConnectionHandler.write(c);
                            ServerConnectionHandler.update();
                        }
                        ServerConnectionHandler.write("End");
                        logger.info("\tDone");
                    }
                } else {
                    try{
                        ServerConnectionHandler.update();

                        Request request = (Request) ServerConnectionHandler.read();
                        if(request != null) {
                            logger.info("Received command from client");

                            if(isUserRegistered(request) || request.getInfo() instanceof RegisterUser) {
                                Command command = (Command) request.getInfo();
                                if (command instanceof Exit)
                                    throw new CommandExecutionException("\tDeprecated command");

                                logger.info("\tExecuting command");
                                ServerConnectionHandler.write(executor.execute(command));
                                logger.info("\tResponse sent to client");
                            }
                            else {
                                ServerConnectionHandler.write("Not registered user\n");
                                logger.info("\tUser is not registered");
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error occurred while executing client`s command: " + e.getMessage());
                    }
                }

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

            try{
                logger.info(collection.save());
            } catch(Exception e){
                logger.error("Error occurred while saving collection: " + e.getMessage());
            }

            ServerConnectionHandler.disconnect();
            UserInput.removeReader();
            logger.info("Goodbye!");

        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error(e);
        }
    }

    public static String registerUser(String user, String password){
        if(user == null || password == null) return "Username and password can not be null\n";
        if(passwords.get(user) != null) return "User with this name already exists\n";

        passwords.put(user, Arrays.toString(md.digest((password + pepper).getBytes(StandardCharsets.UTF_8))));

        return "User registered\n";
    }

    private boolean isUserRegistered(Request request){
        if(request.getUser() == null && request.getPassword() == null) return false;

        try {
            String hash = Arrays.toString(md.digest((request.getPassword() + pepper).getBytes(StandardCharsets.UTF_8)));
            if (passwords.get(request.getUser()).equals(hash)) return true;
        }catch (Exception e){
            return false;
        }
        return false;
    }
}


















