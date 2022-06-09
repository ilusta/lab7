package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.Server.VehicleCollectionServer.VehicleCollection;
import lab7.Vehicle.Vehicle;

import java.util.HashSet;
import java.util.Set;

public class Insert extends CollectionCommand
{

    public Insert(){
    }

    private Insert(Insert cmd){
        this.key = cmd.key;
        this.vehicle = cmd.vehicle;
    }

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

    private String key;
    private Vehicle vehicle;

    public static void attach(VehicleCollection collection){
        Insert.collection = collection;
    }

    @Override
    public Command build(String[] params) throws InputException, EOFInputException{
        if (params.length < 2) throw new InputException("Argument is missing");
        key = params[1];
        Set<Long> IDList = new HashSet<>();
        vehicle = new Vehicle((Set) IDList);
        return new Insert(this);
    }

    @Override
    public String execute() throws CommandExecutionException {
        return collection.insert(key, vehicle);
    }
}