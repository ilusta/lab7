package lab7.Server.Database;

import java.sql.*;


public class Database {

    private Connection connection;

    public Database(String url, String user, String password){
        System.out.println("Connecting to database");
        try {
            DriverManager.setLoginTimeout(5);
            connection = DriverManager.getConnection(url, user, password);
            if(!connection.isValid(5)) throw new RuntimeException("Unable to connect to database");
            System.out.println("\tConnected");
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to connect to database: " + e);
        }
    }

    public Statement getStatement() throws SQLException {
        return connection.createStatement();
    }

    public Connection getConnection(){
        return connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

}
