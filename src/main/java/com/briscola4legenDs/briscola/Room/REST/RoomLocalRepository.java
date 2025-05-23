package com.briscola4legenDs.briscola.Room.REST;

import com.briscola4legenDs.briscola.Room.Room;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomLocalRepository {
    private final ConcurrentHashMap<Long, Room> rooms;

    public RoomLocalRepository() {
        rooms = new ConcurrentHashMap<>();
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoomById(long id) {
        return rooms.get(id);
    }

    public void add(Room room) {
        rooms.put(room.getId(), room);
    }

    public void remove(long id) {
        rooms.remove(id);
    }

    public boolean existsById(long id) {
        return rooms.containsKey(id);
    }

    @Override
    public String toString() {
        return "RoomLocalRepository{" +
                "rooms=" + rooms +
                '}';
    }
}
