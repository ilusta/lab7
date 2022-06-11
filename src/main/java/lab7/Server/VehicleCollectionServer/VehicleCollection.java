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
            collection.stream().sorted(Comparator.comparing(vehicle -> vehicle)).forEach(vehicle ->
                    message.append("\t" + vehicle + "\n\n"));
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
        message.append("\tDone\n");
        return message.toString();
    }


    public String update(Vehicle vehicle, String user) throws CommandExecutionException{
       /* Set<String> keys = this.collection.keySet();

        if(ID == null) throw new CommandExecutionException("ID can not be NULL. ");

        String key = null;
        for (String k : keys) {
            if (ID.equals(this.collection.get(k).getID())) {
                key = k;
            }
        }

        if (key == null) throw new CommandExecutionException("Vehicle with id " + ID + " does not exist. ");

        this.collection.get(key).update(vehicle);*/
        return "Vehicle with ID " + " is updated.\n";
    }

    public String removeKey(String removeKey) throws CommandExecutionException {
        /*Set<String> keys = this.collection.keySet();

        String key = null;
        for (String k : keys) {
            if (removeKey.equals(k)) {
                key = k;
            }
        }

        if (key == null) throw new CommandExecutionException("Element with key " + removeKey + " does not exist. ");
        this.collection.remove(key);*/
        return "Element with key " + removeKey + " deleted. ";
    }

    public String clear() {
        this.collection.clear();
        return "Collection is cleared";
    }

    public Integer getSize() {
        return this.collection.size();
    }

    public String getSumOfWheels(){
        /*Collection<Vehicle> list = collection.values();
        Long sumOfWheels = list.stream().reduce(0L, (sum, vehicle) -> sum + vehicle.getNumberOfWheels(), Long::sum);

        return "Sum of all wheels is " + sumOfWheels;*/
        return "";
    }

    public String removeLower(Vehicle givenVehicle) throws CommandExecutionException{
        /*StringBuilder message = new StringBuilder("Removing:\n");

        Collection<Vehicle> vehicles = collection.values();
        Object[] arr = vehicles.stream().filter(key -> (key.compareTo(givenVehicle) < 0)).toArray();

        if (arr.length > 0) {
            for (String key : collection.keySet())
                for (Object veh : arr)
                    if(collection.get(key).equals(veh)) collection.remove(key);

            message.append("\t" + arr.length + " vehicles deleted\n");
        }else
            message.append("\tNo smaller vehicles in collection\n");
        return message.toString();*/
        return "";
    }

    public String removeGreaterKey(String givenKey) throws CommandExecutionException{
        /*StringBuilder message = new StringBuilder("Removing:\n");

        Collection<String> keys = collection.keySet();
        Object[] arr = keys.stream().filter(key -> (key.compareTo(givenKey) < 0)).toArray();

        if (arr.length > 0) {
            for (Object key : arr) collection.remove(key);
            message.append("\t" + arr.length + " vehicles deleted\n");
        }else
            message.append("\tNo vehicles in collection with key greater than given\n");
        return message.toString();*/
        return "";
    }

    public String maxByCoordinates() throws NullPointerException{
        /*Collection<Vehicle> list = collection.values();
        Optional<Vehicle> opVehicle = list.stream().max(Comparator.comparing(Vehicle::getCoordinates));

        Vehicle vehicle = null;
        if(opVehicle.isPresent())
            vehicle = opVehicle.get();

        return "Vehicle with biggest coordinates is " + vehicle;*/
        return "";
    }

    public String filterByType(String givenType) throws NullPointerException, CommandExecutionException{
        /*StringBuilder message = new StringBuilder("Filtering collection by given vehicle type:\n");
        try{
            VehicleType type = VehicleType.valueOf(givenType);
            Collection<Vehicle> list = collection.values();
            Object[] arr = list.stream().filter(vehicle -> vehicle.getType().equals(type)).toArray();

            if (arr.length > 0)
                for(Object obj : arr) message.append("\t" + obj + "\n\n");
            else
                message.append("\tNo given type vehicles in collection\n");

        } catch (Exception e){
            throw new CommandExecutionException("Wrong vehicle type. Select one of the following types:\n" + VehicleType.convertToString());
        }
        return message.toString();*/
        return "";
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