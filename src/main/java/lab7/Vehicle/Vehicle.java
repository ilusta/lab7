package lab7.Vehicle;

import lab7.Exceptions.EOFInputException;
import lab7.Exceptions.InputException;
import lab7.Exceptions.NegativeValueException;
import lab7.Exceptions.NullException;
import lab7.UserInput.UserInput;

import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.time.ZonedDateTime;

public class Vehicle implements Comparable<Vehicle>, Serializable
{
    static ArrayList<Vehicle> collection;

    static public void setCollection(ArrayList<Vehicle> collection) {Vehicle.collection = collection;}


    public Vehicle() {

    }

    private String key;
    private Long id;
    private String name;
    private Coordinates coordinates;
    private ZonedDateTime creationDate;
    private Double enginePower;
    private Long numberOfWheels;
    private Double capacity;
    private VehicleType type;
    private String user;

    public Vehicle(String key,
                   Long id,
                   String name,
                   Integer x,
                   Integer y,
                   ZonedDateTime creationDate,
                   Double enginePower,
                   Long numberOfWheels,
                   Double capacity,
                   VehicleType type,
                   String user) throws InputException, DateTimeParseException
    {
        setKey(key);
        setID(id);
        setName(name);
        Coordinates coordinates = new Coordinates();
        coordinates.setXCoordinate(x);
        coordinates.setYCoordinate(y);
        setCoordinates(coordinates);
        setCreationTime(creationDate);
        setEnginePower(enginePower);
        setNumberOfWheels(numberOfWheels);
        setCapacity(capacity);
        setVehicleType(type);
        setUser(user);
    }

    public Vehicle(String key) throws EOFInputException, NullException {
        setVehicleParams();
        setKey(key);
    }

    public Vehicle(Long ID) throws EOFInputException, InputException {
        setVehicleParams();
        setID(ID);
    }

    public void update() throws EOFInputException {
        setVehicleParams();
    }

    public void update(Vehicle vehicle){
        this.name = vehicle.name;
        this.coordinates = vehicle.coordinates;
        this.enginePower = vehicle.enginePower;
        this.numberOfWheels = vehicle.numberOfWheels;
        this.capacity = vehicle.capacity;
        this.type = vehicle.type;
    }

    private void setVehicleParams() throws EOFInputException {

        System.out.println("Enter vehicle parameters:");

        boolean flag = false;
        while (!flag) {
            try {
                this.setName(UserInput.getString("vehicle name"));
                flag = true;
            }
            catch (IOException | NullException e) {
                System.out.println(e.getMessage() + ". Impossible name. Please, enter valid value.");
            }
        }

        flag = false;
        final Coordinates coordinates = new Coordinates();
        while (!flag) {
            try {
                coordinates.setXCoordinate(UserInput.getInteger("x coordinate"));
                flag = true;
            }
            catch (IOException | InputException e) {
                System.out.println(e.getMessage() + ". Impossible x. Please, enter valid value.");
            }
        }

        flag = false;
        while (!flag) {
            try {
                coordinates.setYCoordinate(UserInput.getInteger("y coordinate"));
                flag = true;
            }
            catch (IOException | InputException e) {
                System.out.println(e.getMessage() + ". Impossible y. Please, enter valid value.");
            }
        }

        this.setCoordinates(coordinates);
        flag = false;
        while (!flag) {
            try {
                setEnginePower(UserInput.getDouble("engine power"));
                flag = true;
            }
            catch (IOException | InputException e) {
                System.out.println(e.getMessage() + ". Impossible engine power. Please, enter valid value.");
            }
        }

        flag = false;
        while (!flag) {
            try {
                setNumberOfWheels(UserInput.getLong("number of wheels"));
                flag = true;
            }
            catch (IOException | InputException e) {
                System.out.println(e.getMessage() + ". Impossible number of wheels. Please, enter valid value.");
            }
        }

        flag = false;
        while (!flag) {
            try {
                setCapacity(UserInput.getDouble("capacity"));
                flag = true;
            }
            catch (IOException | InputException e) {
                System.out.println(e.getMessage() + ". Impossible capacity. Please, enter valid value.");
            }
        }

        flag = false;
        while (!flag) {
            try {
                System.out.println("Possible vehicle types:");
                System.out.println(VehicleType.convertToString());
                setVehicleType(VehicleType.valueOf(UserInput.getString("type")));
                flag = true;
            }
            catch (IOException | NullException | IllegalArgumentException e) {
                System.out.println(e.getMessage() + ". Wrong type. Please, enter valid value.");
            }
        }

        creationDate = ZonedDateTime.now();

        System.out.println("Done!");
    }

    private void setKey(String key) throws NullException {
        if(key == null || key.equals("")) throw new NullException("Key can not be NULL or empty");

        if(collection != null) {
            Object[] arr = collection.stream().filter(vehicle -> vehicle.getKey().equals(key)).toArray();
            if (arr.length > 0) throw new NullException("Vehicle with this key is already in collection");
        }

        this.key = key;
    }

    private void setID(Long ID) throws InputException {
        if(ID == null) throw new NullException("ID can not be NULL");

        if(collection != null) {
            Object[] arr = collection.stream().filter(vehicle -> vehicle.getID().equals(ID)).toArray();
            if (arr.length > 0) throw new InputException("Vehicle with this ID is already in collection");
        }

        this.id = ID;
    }

    private void setName(String name) throws NullException {
        if (name == null || name.equals("")) throw new NullException("Name can not be NULL or empty");
        this.name = name;
    }

    private void setCreationTime(ZonedDateTime dateTime) {
        this.creationDate = dateTime;
    }

    private void setCreationTime(String input) throws DateTimeParseException {
        this.creationDate = ZonedDateTime.parse(input, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    private void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    private void setEnginePower(Double enginePower) throws NegativeValueException {
        if (enginePower != null && enginePower <= 0.0) throw new NegativeValueException("Power can not be negative");
        this.enginePower = enginePower;
    }

    private void setNumberOfWheels(Long numberOfWheels) throws NegativeValueException {
        if (numberOfWheels != null && numberOfWheels <= 0) throw new NegativeValueException("Number of wheels can not be negative");
        this.numberOfWheels = numberOfWheels;
    }

    private void setCapacity(Double capacity) throws NegativeValueException, NullException {
        if (capacity == null) throw new NullException("Capacity can not be NULL");
        if (capacity <= 0.0) throw new NegativeValueException("Capacity can not be negative");
        this.capacity = capacity;
    }

    private void setVehicleType(VehicleType type) throws NullException, IllegalArgumentException {
        if (type == null) throw new NullException("Vehicle type can not be NULL");
        this.type = type;
    }

    private void setUser(String user) {
        this.user = user;
    }

    public String getKey() {
        return this.key;
    }

    public Long getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Coordinates getCoordinates() {
        return this.coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return this.creationDate;
    }

    public Double getEnginePower() {
        return this.enginePower;
    }

    public Double getCapacity() {
        return this.capacity;
    }

    public Long getNumberOfWheels() {
        return this.numberOfWheels;
    }

    public VehicleType getType() {
        return this.type;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Vehicle{user=" + this.user +
                ", key=" + this.key +
                ", id=" + this.id +
                ", name=" + this.name +
                ", coordinates=" + this.coordinates.toString() +
                ", creation=" + this.creationDate.toString() +
                ", enginePower=" + this.enginePower +
                ", numberOfWheels=" + this.numberOfWheels +
                ", capacity=" + this.capacity +
                ", type=" + this.type.toString() + "};";
    }

    @Override
    public int hashCode() {
        int result = (int)(this.id ^ this.id >>> 32);
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.coordinates.hashCode();
        result = 31 * result + this.enginePower.hashCode();
        result = 31 * result + this.numberOfWheels.hashCode();
        result = 31 * result + this.capacity.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Vehicle other = (Vehicle)obj;
        return this.id == other.id &&
                this.name.equals(other.name) &&
                this.coordinates.equals(other.coordinates) &&
                this.enginePower.equals(other.enginePower) &&
                this.numberOfWheels.equals(other.numberOfWheels) &&
                this.capacity.equals(other.capacity) &&
                this.type.equals(other.type);
    }

    @Override
    public int compareTo(Vehicle O) {
        Double eP = this.getEnginePower();
        if(eP == null)eP = (double)0;
        Long nW = this.getNumberOfWheels();
        if(nW == null)nW = (long)0;
        Double OeP = O.getEnginePower();
        if(OeP == null)OeP = (double)0;
        Long OnW = O.getNumberOfWheels();
        if(OnW == null)OnW = (long)0;
        return this.getName().compareTo(O.getName()) +
                this.getCoordinates().compareTo(O.getCoordinates()) +
                eP.compareTo(OeP) +
                nW.compareTo(OnW) +
                this.getCapacity().compareTo(O.getCapacity()) +
                this.getType().compareTo(O.getType());
    }
}