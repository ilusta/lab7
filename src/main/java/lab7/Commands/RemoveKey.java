package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;

public class RemoveKey extends SecurityCollectionCommand
{

    @Override
    public String getName() {
        return "remove_key";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "[key] | Removes element from collection by given key.";
    }

    public static void attach(VehicleCollection collection){
        RemoveKey.collection = collection;
    }

    String removeKey;

    @Override
    public Command build(String[] params) throws InputException, EOFInputException {
        if (params.length < 2) throw new InputException("Argument is missing");
        removeKey = params[1];

        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        return collection.removeKey(removeKey, this.getUser()) + "\n";
    }
}