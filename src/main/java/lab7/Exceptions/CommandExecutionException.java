package lab7.Exceptions;

public class CommandExecutionException extends Exception
{
    public CommandExecutionException(String errorMessage) {
        super(errorMessage);
    }
}