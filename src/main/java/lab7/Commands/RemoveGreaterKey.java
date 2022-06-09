package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;

public class RemoveGreaterKey extends CollectionCommand
{

    public RemoveGreaterKey(){

    }

    private RemoveGreaterKey(RemoveGreaterKey cmd){
        this.removeKey = cmd.removeKey;
    }

    @Override
    public String getName() {
        return "remove_greater_key";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "[key] | Removes element from collection with key greater than given.";
    }

    public static void attach(VehicleCollection collection){
        RemoveGreaterKey.collection = collection;
    }

    private String removeKey;

    @Override
    public Command build(String[] params) throws InputException, EOFInputException {
        if (params.length < 2) throw new InputException("Key is missing");
        removeKey = params[1];

        return new RemoveGreaterKey(this);
    }

    @Override
    public String execute() throws CommandExecutionException {
        return collection.removeGreaterKey(removeKey) + "\n";
    }
}