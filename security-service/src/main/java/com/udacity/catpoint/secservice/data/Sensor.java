package com.udacity.catpoint.secservice.data;


import com.google.common.collect.ComparisonChain;

import java.util.Objects;
import java.util.UUID;

/**
 * Sensor POJO. Needs to know how to sort itself for display purposes.
 */
public class Sensor implements Comparable<Sensor> {
    private final UUID sensorId;
    private final String name;
    private boolean active;
    private final SensorType sensorType;

    // Gson : Unable to invoke no-args constructor for class
    public Sensor() {
        this.name = UUID.randomUUID().toString();
        this.sensorType = SensorType.WINDOW;
        this.sensorId = UUID.randomUUID();
        this.active = Boolean.FALSE;
    }

    public Sensor(String name, SensorType sensorType) {
        this.name = name;
        this.sensorType = sensorType;
        this.sensorId = UUID.randomUUID();
        this.active = Boolean.FALSE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sensor sensor = (Sensor) o;
        return sensorId.equals(sensor.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId);
    }

    public String getName() {
        return name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    @Override
    public int compareTo(Sensor o) {
        return ComparisonChain.start()
                .compare(this.name, o.name)
                .compare(this.sensorType.toString(), o.sensorType.toString())
                .compare(this.sensorId, o.sensorId)
                .result();
    }
}
