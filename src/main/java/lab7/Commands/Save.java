package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;


public class Save extends CollectionCommand
{

    @Override
    public String getName() {
        return "save";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "Saves collection to the file.";
    }

    public static void attach(VehicleCollection collection){
        Save.collection = collection;
    }

    @Override
    public Command build(String[] params){
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        String message = "";
        try {
            message = collection.save();
        } catch(Exception e){
            throw new CommandExecutionException(e.getMessage());
        }
        return message + "\n";
    }

}