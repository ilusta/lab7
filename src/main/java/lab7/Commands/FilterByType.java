package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;

public class FilterByType extends CollectionCommand
{
    @Override
    public String getName() {
        return "filter_by_type";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "[type] | Prints collection filtered by given vehicle type.";
    }

    public static void attach(VehicleCollection collection){
        FilterByType.collection = collection;
    }

    private String type = null;

    @Override
    public Command build(String[] params) throws InputException, EOFInputException {
        if (params.length < 2) throw new InputException("Vehicle type is missing");
        type = params[1];
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        String message = collection.filterByType(type) + "\n";
        return message;
    }
}