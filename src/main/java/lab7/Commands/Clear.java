package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;


public class Clear extends CollectionCommand
{

    @Override
    public String getName() {
        return "clear";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "Clears collection.";
    }

    public static void attach(VehicleCollection collection){
        Clear.collection = collection;
    }

    @Override
    public Command build(String[] params){
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        return collection.clear() + "\n";
    }

}