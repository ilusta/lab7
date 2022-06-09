package lab7.Exceptions;

public class ConnectionException extends Exception{
    public ConnectionException(String errorMessage) {
        super(errorMessage);
    }
}
