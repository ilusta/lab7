package lab7.Commands;

import lab7.Server.VehicleCollectionServer.VehicleCollection;


public class Info extends CollectionCommand
{

    @Override
    public String getName() {
        return "info";
    }
    @Override
    public CommandType getType() {
        return CommandType.SERVER;
    }
    @Override
    public String getHelp() {
        return "Prints information about collection.";
    }

    public static void attach(VehicleCollection collection){
        Show.collection = collection;
    }

    @Override
    public Command build(String[] param){
        return this;
    }

    @Override
    public String execute(){
        return collection.info() + "\n";
    }

}
