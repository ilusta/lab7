package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;
import lab7.Vehicle.Vehicle;

import java.util.HashSet;
import java.util.Set;

public class Insert extends SecurityCollectionCommand
{

    @Override
    public String getName() {
        return "insert";
    }
    @Override
    public CommandType getType() {
        return CommandType.ALL;
    }
    @Override
    public String getHelp() {
        return "[key] {vehicle} | Inserts new element to collection with given key.";
    }

    private Vehicle vehicle;

    public static void attach(VehicleCollection collection){
        Insert.collection = collection;
    }

    @Override
    public Command build(String[] params) throws InputException, EOFInputException{
        if (params.length < 2) throw new InputException("Argument is missing");
        String key = params[1];
        vehicle = new Vehicle(key);
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        return collection.insert(vehicle, this.getUser());
    }
}