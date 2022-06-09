package lab7.Vehicle;

import java.io.Serializable;

public enum VehicleType implements Serializable
{
    HELICOPTER,
    DRONE,
    BOAT,
    BICYCLE,
    HOVERBOARD;

    public static String convertToString() {
        return VehicleType.HELICOPTER.toString() + ", " +
                VehicleType.DRONE.toString() + ", " +
                VehicleType.BOAT.toString() + ", " +
                VehicleType.BICYCLE.toString() + ", " +
                VehicleType.HOVERBOARD.toString();
    }
}