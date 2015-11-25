package de.whs.homebaconcore;

/**
 * Created by Daniel on 17.11.2015.
 */
public class Room {

    private long id;
    private String name;

    public Room(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
