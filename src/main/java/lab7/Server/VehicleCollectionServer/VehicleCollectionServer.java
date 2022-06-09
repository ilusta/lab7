package lab7.Server.VehicleCollectionServer;

import lab7.Commands.*;
import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.UserInput.UserInput;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VehicleCollectionServer {

    private static final Logger logger = LogManager.getLogger(VehicleCollectionServer.class);

    final ArrayList<Command> commandList = new ArrayList<>();
    final ArrayList<Command> clientCommandList = new ArrayList<>();

    public void run() {

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

            logger.info("Welcome to the Vehicle Collection Server!");
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

            logger.info("Server started");
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

                        Command command = (Command) ServerConnectionHandler.read();
                        if(command != null) {
                            logger.info("Received command from client");
                            if (command instanceof Exit)
                                throw new CommandExecutionException("\tDeprecated command");

                            logger.info("\tExecuting command");
                            ServerConnectionHandler.write(executor.execute(command));
                            logger.info("\tResponse send to client");
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

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}


















