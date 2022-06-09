package lab7.Vehicle;

import lab7.Exceptions.NullException;

import java.io.Serializable;

public class Coordinates implements Comparable<Coordinates>, Serializable
{
    private Integer x, y;

    public void setXCoordinate(Integer x) throws NullException {
        if (x == null) throw new NullException("X coordinate can not be NULL");
        this.x = x;
    }

    public void setYCoordinate(Integer y) {
        this.y = y;
    }

    public Integer getXCoordinate() {
        return this.x;
    }

    public Integer getYCoordinate() {
        return this.y;
    }

    @Override
    public String toString() {
        return "Coordinates{x=" + this.x +
                ", y=" + this.y + "};";
    }

    @Override
    public int compareTo(Coordinates O) {
        Integer ty = getYCoordinate();
        if(ty == null) ty = 0;
        Integer oy = O.getYCoordinate();
        if(oy == null) oy = 0;
        return ty.compareTo(oy) + getXCoordinate().compareTo(O.getXCoordinate());
    }
}