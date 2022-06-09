package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;

import java.util.ArrayList;


public class Help extends Command
{
    private static ArrayList<Command> commandList;

    @Override
    public String getName() {
        return "help";
    }
    @Override
    public CommandType getType() {
        return CommandType.ALL;
    }
    @Override
    public String getHelp() {
        return "Prints all supported commands.";
    }

    public static void attachCommandList(ArrayList<Command> selfCommandList){
        Help.commandList = selfCommandList;
    }

    @Override
    public Command build(String[] params) throws InputException, EOFInputException {
        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        StringBuilder message = new StringBuilder("Available commands:\n");
        for (Command c : commandList)
            message.append("\t" + c.getName() + " - " + c.getHelp() + "\n");

        return message.toString();
    }
}