package com.udacity.catpoint.secservice.application;

import com.udacity.catpoint.secservice.data.AlarmStatus;

/**
 * Identifies a component that should be notified whenever the system status changes
 */
public interface StatusListener {
    void notify(AlarmStatus status);

    void catDetected(boolean catDetected);

    void sensorStatusChanged();
}
