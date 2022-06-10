package lab7.Server.VehicleCollectionServer;

import lab7.Server.VehicleCollectionServer.CSVParser.CSVParser;
import lab7.Exceptions.CommandExecutionException;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import lab7.Exceptions.InputException;
import lab7.Exceptions.NullException;
import lab7.Vehicle.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;


public class VehicleCollection {

    private static final Logger logger = LogManager.getLogger(VehicleCollectionServer.class);


    //Connection connection = DriverManager.getConnection("helios.se.ifmo.ru");




    protected class vehicleWithKey{
        protected String key;
        protected Vehicle vehicle;
        protected vehicleWithKey(String key, Vehicle vehicle){
            this.key = key;
            this.vehicle = vehicle;
        }
    }

    LinkedHashMap<String, Vehicle> collection;
    String fileName = null;
    private ZonedDateTime creationDate;

    public VehicleCollection() {
        this.collection = new LinkedHashMap<>();
        this.creationDate = ZonedDateTime.now();
    }

    public void setFileName(String fileName) throws NullException {
        if (fileName == null) throw new NullException("File name is NULL.");
        this.fileName = fileName;
    }

    public void open() throws SecurityException, IOException, InputException, DateTimeParseException {
        logger.info("Loading collection from " + fileName);

        InputStreamReader input = new InputStreamReader(new FileInputStream(fileName));
        Set<Long> IDList = new HashSet<>();

        if(input.ready())
        try {
            ArrayList<String> params = CSVParser.readLine(input);
            if (params.size() < 1) throw new InputException("Argument missing.");
            this.creationDate = ZonedDateTime.parse(params.get(0), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        catch (Exception e) {
            logger.error("Loading error. " + e.getMessage());
        }

        while(input.ready()) {
            try {
                ArrayList<String> params = CSVParser.readLine(input);
                if(params.size() < 10) throw new InputException("Argument missing.");

                Double p6;
                Long p7;
                Integer p4;
                if (params.get(4).equals("")) p4 = null;
                else p4 = Integer.parseInt(params.get(4));
                if (params.get(6).equals("")) p6 = null;
                else p6 = Double.parseDouble(params.get(6));
                if (params.get(7).equals("")) p7 = null;
                else p7 = Long.parseLong(params.get(7));

                collection.put(params.get(0), new Vehicle(Long.parseLong(params.get(1)),
                        params.get(2),
                        Integer.parseInt(params.get(3)),
                        p4,
                        params.get(5),
                        p6,
                        p7,
                        Double.parseDouble(params.get(8)),
                        params.get(9),
                        IDList));
                IDList.add(Long.parseLong(params.get(1)));
            }
            catch (Exception e)
            {
                logger.error("Loading error. " + e.getMessage());
            }
        }

        input.close();
        logger.info("Load completed: " + collection.size() + " vehicles loaded.");
    }

    public String save() throws IOException {
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(fileName));
        StringBuilder data = new StringBuilder("\"" + this.getCreationDate().toString() + "\";\n");

        Set<String> keys = this.collection.keySet();
        for (String key : keys) {
            String line[] = {key,
                    String.valueOf(collection.get(key).getID()),
                    String.valueOf(collection.get(key).getName()),
                    String.valueOf(collection.get(key).getCoordinates().getXCoordinate()),
                    String.valueOf(collection.get(key).getCoordinates().getYCoordinate()),
                    String.valueOf(collection.get(key).getCreationDate()),
                    String.valueOf(collection.get(key).getEnginePower()),
                    String.valueOf(collection.get(key).getNumberOfWheels()),
                    String.valueOf(collection.get(key).getCapacity()),
                    String.valueOf(collection.get(key).getType())};
            data.append(CSVParser.convertToLine(line));
        }

        output.write(data.toString().getBytes());
        output.close();

        return "Collection saved to " + fileName;
    }

    public String show() throws CommandExecutionException {
        StringBuilder message = new StringBuilder("Vehicles in the collection:\n");

        if(collection.isEmpty())
            message.append("\tCollection is empty\n");
        else {
            Collection<String> keys = collection.keySet();
            Collection<vehicleWithKey> list = new ArrayList<>();
            for (String k : keys) list.add(new vehicleWithKey(k, collection.get(k)));

            list.stream().sorted(Comparator.comparing(veh -> veh.vehicle)).forEach(veh ->
                    message.append("\tKey=" + veh.key + ": " + veh.vehicle.toString() + "\n\n"));
        }
        return message.toString();
    }

    public String insert(String newKey, Vehicle vehicle) throws CommandExecutionException {
        Set<String> keys = this.collection.keySet();
        Set<Long> IDList = new HashSet<>();

        boolean flag = true;
        for (String key : keys) {
            IDList.add(this.collection.get(key).getID());
            if (key.equals(newKey)) {
                flag = false;
            }
        }

        if (!flag) throw new CommandExecutionException("This key is already exists. ");

        vehicle.serverVehicleUpdate(IDList);
        this.collection.put(newKey, vehicle);

        return "Vehicle inserted.";
    }

    public String update(Long ID, Vehicle vehicle) throws CommandExecutionException{
        Set<String> keys = this.collection.keySet();

        if(ID == null) throw new CommandExecutionException("ID can not be NULL. ");

        String key = null;
        for (String k : keys) {
            if (ID.equals(this.collection.get(k).getID())) {
                key = k;
            }
        }

        if (key == null) throw new CommandExecutionException("Vehicle with id " + ID + " does not exist. ");

        this.collection.get(key).update(vehicle);
        return "Vehicle with ID " + ID + " is updated.\n";
    }

    public String removeKey(String removeKey) throws CommandExecutionException {
        Set<String> keys = this.collection.keySet();

        String key = null;
        for (String k : keys) {
            if (removeKey.equals(k)) {
                key = k;
            }
        }

        if (key == null) throw new CommandExecutionException("Element with key " + removeKey + " does not exist. ");
        this.collection.remove(key);
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
        Collection<Vehicle> list = collection.values();
        Long sumOfWheels = list.stream().reduce(0L, (sum, vehicle) -> sum + vehicle.getNumberOfWheels(), Long::sum);

        return "Sum of all wheels is " + sumOfWheels;
    }

    public String removeLower(Vehicle givenVehicle) throws CommandExecutionException{
        StringBuilder message = new StringBuilder("Removing:\n");

        Collection<Vehicle> vehicles = collection.values();
        Object[] arr = vehicles.stream().filter(key -> (key.compareTo(givenVehicle) < 0)).toArray();

        if (arr.length > 0) {
            for (String key : collection.keySet())
                for (Object veh : arr)
                    if(collection.get(key).equals(veh)) collection.remove(key);

            message.append("\t" + arr.length + " vehicles deleted\n");
        }else
            message.append("\tNo smaller vehicles in collection\n");
        return message.toString();
    }

    public String removeGreaterKey(String givenKey) throws CommandExecutionException{
        StringBuilder message = new StringBuilder("Removing:\n");

        Collection<String> keys = collection.keySet();
        Object[] arr = keys.stream().filter(key -> (key.compareTo(givenKey) < 0)).toArray();

        if (arr.length > 0) {
            for (Object key : arr) collection.remove(key);
            message.append("\t" + arr.length + " vehicles deleted\n");
        }else
            message.append("\tNo vehicles in collection with key greater than given\n");
        return message.toString();
    }

    public String maxByCoordinates() throws NullPointerException{
        Collection<Vehicle> list = collection.values();
        Optional<Vehicle> opVehicle = list.stream().max(Comparator.comparing(Vehicle::getCoordinates));

        Vehicle vehicle = null;
        if(opVehicle.isPresent())
            vehicle = opVehicle.get();

        return "Vehicle with biggest coordinates is " + vehicle;
    }

    public String filterByType(String givenType) throws NullPointerException, CommandExecutionException{
        StringBuilder message = new StringBuilder("Filtering collection by given vehicle type:\n");
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