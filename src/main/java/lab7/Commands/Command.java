package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;

import java.io.Serializable;

public abstract class Command implements Serializable
{
    public abstract String getName();
    public abstract CommandType getType();
    public abstract String getHelp();

    public abstract String execute() throws CommandExecutionException;
    public abstract Command build(String[] param) throws InputException, EOFInputException;
}