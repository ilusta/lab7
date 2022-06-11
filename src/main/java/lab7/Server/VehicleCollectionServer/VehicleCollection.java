package lab7.Server.VehicleCollectionServer;

import lab7.Exceptions.CommandExecutionException;

import java.io.*;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import lab7.Exceptions.InputException;
import lab7.Exceptions.NullException;
import lab7.Vehicle.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VehicleCollection {

    private static final Logger logger = LogManager.getLogger(VehicleCollectionServer.class);

    private Connection connection;

    private ArrayList<Vehicle> collection;
    private ZonedDateTime creationDate;


    public VehicleCollection(Connection connection) throws RuntimeException{
        this.connection = connection;
        this.collection = new ArrayList<>();
        Vehicle.setCollection(collection);
        this.creationDate = ZonedDateTime.now();

        load();
    }

    public void load(){
        logger.info("Loading collection from database:");
        collection.clear();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, key, name, x, y, date, enginepower, numberofwheels, capacity, type, \"user\" from collection")) {

            while (resultSet.next()) {
                try {
                    Long id = resultSet.getLong("id");
                    String key = resultSet.getString("key");
                    String name = resultSet.getString("name");
                    Integer x = resultSet.getInt("x");
                    Integer y = resultSet.getInt("y");
                    if(resultSet.wasNull()) y = null;
                    ZonedDateTime date = ZonedDateTime.parse(resultSet.getString("date"), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    Double enginePower = resultSet.getDouble("enginepower");
                    if(resultSet.wasNull()) enginePower = null;
                    Long numberOfWheels = resultSet.getLong("numberofwheels");
                    if(resultSet.wasNull()) numberOfWheels = null;
                    Double capacity = resultSet.getDouble("capacity");
                    VehicleType type = VehicleType.valueOf(resultSet.getString("type"));
                    String user = resultSet.getString("user");

                    collection.add(new Vehicle(key, id, name, x, y, date, enginePower, numberOfWheels, capacity, type, user));

                    if(this.creationDate.compareTo(date) > 0) this.creationDate = date;
                }
                catch(Exception e){
                    logger.error("\tError while loading vehicle from collection: " + e);
                }
            }
            logger.info("\tLoaded " + collection.size() + " vehicles");
        }
        catch (Exception e){
            throw new RuntimeException("Unable to load collection from database: " + e);
        }
    }


    public String show() throws CommandExecutionException {
        StringBuilder message = new StringBuilder("Vehicles in the collection:\n");

        if(collection.isEmpty())
            message.append("\tCollection is empty\n");
        else {
            collection.stream().sorted(Comparator.comparing(vehicle -> vehicle)).forEach(vehicle -> message.append("\t" + vehicle + "\n\n"));
        }
        return message.toString();
    }


    public String insert(Vehicle vehicle, String user) throws CommandExecutionException {
        StringBuilder message = new StringBuilder("Inserting vehicle into collection:\n");

        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " +
                "collection(id, key, name, x, y, date, enginepower, numberofwheels, capacity, type, \"user\") VALUES (nextval('collection_id_seq'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){

            preparedStatement.setString(1, vehicle.getKey());
            preparedStatement.setString(2, vehicle.getName());
            preparedStatement.setInt(3, vehicle.getCoordinates().getXCoordinate());
            Integer y = vehicle.getCoordinates().getYCoordinate();
            if(y != null) preparedStatement.setInt(4, y);
            else preparedStatement.setNull(4, Types.INTEGER);
            preparedStatement.setString(5, ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            Double ep = vehicle.getEnginePower();
            if(ep != null) preparedStatement.setDouble(6, ep);
            else preparedStatement.setNull(6, Types.DOUBLE);
            Long nofw = vehicle.getNumberOfWheels();
            if(nofw != null) preparedStatement.setLong(7, nofw);
            else preparedStatement.setNull(7, Types.BIGINT);
            preparedStatement.setDouble(8, vehicle.getCapacity());
            preparedStatement.setString(9, vehicle.getType().name());
            preparedStatement.setString(10, user);

            preparedStatement.execute();

            load();
        }
        catch (Exception e){
            throw new CommandExecutionException("Unable to add vehicle to collection: " + e);
        }
        message.append("\tVehicle inserted\n");
        return message.toString();
    }


    public String update(Vehicle vehicle, String user) throws CommandExecutionException{
        StringBuilder message = new StringBuilder("Updating vehicle:\n");

        Long givenID = vehicle.getID();
        Optional<Vehicle> foundVehicle = collection.stream().filter(veh -> veh.getID().equals(givenID)).findFirst();

        if(!foundVehicle.isPresent())
            throw new CommandExecutionException("No vehicle with ID=" + givenID +" in collection");
        if(!foundVehicle.get().getUser().equals(user))
            throw new CommandExecutionException("Vehicle with ID=" + givenID +" belongs to another user");

        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE collection" +
                "SET name=?, x=?, y=?, date=?, enginepower=?, numberofwheels=?, capacity=?, type=? " +
                "WHERE (id=? AND \"user\" =?);")){

            preparedStatement.setString(1, vehicle.getName());
            preparedStatement.setInt(2, vehicle.getCoordinates().getXCoordinate());
            Integer y = vehicle.getCoordinates().getYCoordinate();
            if(y != null) preparedStatement.setInt(3, y);
            else preparedStatement.setNull(3, Types.INTEGER);
            preparedStatement.setString(4, ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            Double ep = vehicle.getEnginePower();
            if(ep != null) preparedStatement.setDouble(5, ep);
            else preparedStatement.setNull(5, Types.DOUBLE);
            Long nofw = vehicle.getNumberOfWheels();
            if(nofw != null) preparedStatement.setLong(6, nofw);
            else preparedStatement.setNull(6, Types.BIGINT);
            preparedStatement.setDouble(7, vehicle.getCapacity());
            preparedStatement.setString(8, vehicle.getType().name());
            preparedStatement.setLong(9, givenID);
            preparedStatement.setString(10, user);

            preparedStatement.execute();

            load();
        }
        catch (Exception e){
            throw new CommandExecutionException("Unable to update vehicle: " + e);
        }

        message.append("\tVehicle with ID=" + givenID + " is updated.\n");
        return message.toString();
    }


    public String removeKey(String givenKey, String user) throws CommandExecutionException {
        StringBuilder message = new StringBuilder("Removing vehicle:\n");

        Optional<Vehicle> foundVehicle = collection.stream().filter(veh -> veh.getKey().equals(givenKey)).findFirst();

        if(!foundVehicle.isPresent())
            throw new CommandExecutionException("No vehicle with KEY=" + givenKey +" in collection");
        if(!foundVehicle.get().getUser().equals(user))
            throw new CommandExecutionException("Vehicle with KEY=" + givenKey +" belongs to another user");

        try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM collection WHERE (key=? AND \"user\"=?);")){

            preparedStatement.setString(1, givenKey);
            preparedStatement.setString(2, user);
            preparedStatement.execute();

            load();
        }
        catch (Exception e){
            throw new CommandExecutionException("Unable to remove vehicle: " + e);
        }

        message.append("\tVehicle with KEY=" + givenKey + " deleted.\n");
        return message.toString();
    }


    public String clear(String user) throws CommandExecutionException {
        StringBuilder message = new StringBuilder("Clearing collection:\n");

        try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM collection WHERE \"user\"=?;")){
            preparedStatement.setString(1, user);
            preparedStatement.execute();
            load();
        }
        catch (Exception e){
            throw new CommandExecutionException("Unable to remove vehicles: " + e);
        }

        message.append("\tvehicles owned by " + user + " deleted\n");
        message.append("\t" + collection.size() + " vehicles left in collection\n");
        return message.toString();
    }


    public Integer getSize() {
        return this.collection.size();
    }


    public String getSumOfWheels(){
        Long sumOfWheels = collection.stream().reduce(0L, (sum, vehicle) -> sum + vehicle.getNumberOfWheels(), Long::sum);
        return "Sum of all wheels is " + sumOfWheels;
    }


    public String removeLower(Vehicle givenVehicle, String user) throws CommandExecutionException{
        StringBuilder message = new StringBuilder("Removing smaller vehicles:\n");

        Long[] IDArr = collection.stream().filter(veh -> ((veh.compareTo(givenVehicle) < 0) && veh.getUser().equals(user))).map(Vehicle::getID).toArray(Long[]::new);
        if(IDArr.length == 0) throw new CommandExecutionException("No smaller vehicles owned by " + user + " in collection");

        int counter = 0;
        try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM collection WHERE (\"user\"=? AND id=?)")){

            preparedStatement.setString(1, user);
            for(Long id : IDArr){
                preparedStatement.setLong(2, id);
                preparedStatement.execute();
                counter++;
            }
        }
        catch (Exception e){
            throw new CommandExecutionException("Unable to remove vehicles: " + e);
        }

        load();
        message.append("\tRemoved " + counter + " vehicles owned by " + user + "\n");
        message.append("\t" + collection.size() + " vehicles left in collection\n");
        return message.toString();
    }


    public String removeGreaterKey(String givenKey, String user) throws CommandExecutionException{
        StringBuilder message = new StringBuilder("Removing vehicles with greater keys:\n");

        Long[] IDArr = collection.stream().filter(veh -> ((veh.getKey().compareTo(givenKey) > 0) && veh.getUser().equals(user))).map(Vehicle::getID).toArray(Long[]::new);
        if(IDArr.length == 0) throw new CommandExecutionException("No vehicles with greater keys owned by " + user + " in this collection");

        int counter = 0;
        try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM collection WHERE (\"user\"=? AND id=?)")){

            preparedStatement.setString(1, user);
            for(Long id : IDArr){
                preparedStatement.setLong(2, id);
                preparedStatement.execute();
                counter++;
            }
        }
        catch (Exception e){
            throw new CommandExecutionException("Unable to remove vehicles: " + e);
        }

        load();
        message.append("\tRemoved " + counter + " vehicles owned by " + user + "\n");
        message.append("\t" + collection.size() + " vehicles left in collection\n");
        return message.toString();
    }

    public String maxByCoordinates() throws NullPointerException{
        Optional<Vehicle> opVehicle = collection.stream().max(Comparator.comparing(Vehicle::getCoordinates));

        if(opVehicle.isPresent())
            return "Vehicle with biggest coordinates is:\n\t" + opVehicle.get();
        else
            return "There is no vehicle with biggest coordinates in collection";
    }

    public String filterByType(String givenType) throws NullPointerException, CommandExecutionException{
        StringBuilder message = new StringBuilder("Filtering collection by type " + givenType + ":\n");
        try{
            VehicleType type = VehicleType.valueOf(givenType);
            Object[] arr = collection.stream().filter(vehicle -> vehicle.getType().equals(type)).toArray();

            if (arr.length > 0)
                for(Object obj : arr) message.append("\t" + obj + "\n\n");
            else
                message.append("\tNo given type vehicles in collection\n");

        } catch (Exception e){
            throw new CommandExecutionException("Wrong vehicle type. Select one of the following types:\n" + VehicleType.convertToString());
        }
        return message.toString();
    }

    public ZonedDateTime getCreationDate(){
        return this.creationDate;
    }

    public String info(){
        return  "Information about Vehicle collection:\n" +
                "\tLinked hash map collection\n" +
                "\tConsists of " + this.getSize() + " vehicles\n" +
                "\tCreation date: " + this.getCreationDate().toString();
    }
}