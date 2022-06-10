package lab7.Commands;

import lab7.Exceptions.CommandExecutionException;
import lab7.Exceptions.InputException;
import lab7.Server.VehicleCollectionServer.VehicleCollectionServer;


public class RegisterUser extends SecurityCommand
{

    @Override
    public String getName() {
        return "register";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "[username password] | register at this server";
    }


    @Override
    public Command build(String[] params) throws InputException{
        if(params.length < 3) throw new InputException("Not enough arguments. See 'help' and try again.");

        user = params[1];
        password = params[2];
        if(user == null || password == null) throw new InputException("Username and password can not be null.");

        return this;
    }

    @Override
    public String execute() throws CommandExecutionException {
        return VehicleCollectionServer.registerUser(user, password);
    }

}