package lab7.Commands;

import lab7.Client.VehicleCollectionClient.VehicleCollectionClient;
import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;

import java.util.ArrayList;


public class Disconnect extends Command
{
    private static ArrayList<Command> commandList;

    @Override
    public String getName() {
        return "disconnect";
    }
    @Override
    public CommandType getType() {
        return CommandType.CLIENT;
    }
    @Override
    public String getHelp() {
        return "Disconnects from server";
    }

    @Override
    public Command build(String[] params) throws InputException, EOFInputException {
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        VehicleCollectionClient.disconnect();
        return "";
    }
}