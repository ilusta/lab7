package lab7.Exceptions;

public class EOFInputException extends Exception
{
    public EOFInputException(String errorMessage) {
        super(errorMessage);
    }
}