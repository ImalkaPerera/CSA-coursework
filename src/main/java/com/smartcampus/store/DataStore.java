package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    public static ConcurrentHashMap<String, Sensor> getSensors() {
        return sensors;
    }

    public static ConcurrentHashMap<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
}
