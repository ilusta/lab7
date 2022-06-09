package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;


public class Show extends CollectionCommand
{

    @Override
    public String getName() {
        return "show";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "Prints all elements in collection.";
    }

    public static void attach(VehicleCollection collection){
        Show.collection = collection;
    }

    @Override
    public Command build(String[] params){
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        return collection.show() + "\n";
    }

}