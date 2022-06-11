package lab7.Commands;


import lab7.Server.VehicleCollectionServer.VehicleCollection;

public abstract class SecurityCollectionCommand extends SecurityCommand
{
    protected static VehicleCollection collection;

    protected String user;

    public String getUser(){
        return this.user;
    }

    public void setUser(String user){
        this.user = user;
    }

}