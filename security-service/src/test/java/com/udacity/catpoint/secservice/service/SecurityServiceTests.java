package com.udacity.catpoint.secservice.service;

import com.udacity.catpoint.imageservice.ImageService;
import com.udacity.catpoint.secservice.data.*;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTests {
    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    private static Stream<Arguments> getArmedSenors() {
        return Stream.of(
                Arguments.of(ArmingStatus.ARMED_AWAY, SensorType.DOOR),
                Arguments.of(ArmingStatus.ARMED_AWAY, SensorType.MOTION),
                Arguments.of(ArmingStatus.ARMED_AWAY, SensorType.WINDOW),
                Arguments.of(ArmingStatus.ARMED_HOME, SensorType.DOOR),
                Arguments.of(ArmingStatus.ARMED_HOME, SensorType.MOTION),
                Arguments.of(ArmingStatus.ARMED_HOME, SensorType.WINDOW)
        );
    }

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @ParameterizedTest
    @DisplayName("Verify that System is in Pending State when Alarm is Armed and Sensor is Activated")
    @MethodSource("getArmedSenors")
    public void verifyThatSystemIsInPendingState_when_AlarmIsArmed_and_SensorIsActivated(
            ArmingStatus status, SensorType type) {
        // Arrange
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(status);
        var sensor = new Sensor(new RandomString().nextString(), type);

        // Act
        securityService.changeSensorActivationStatus(sensor, true);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @DisplayName("Verify that Alarm is Turned On when System in Active Pending State is Notified by a Sensor")
    @MethodSource("getArmedSenors")
    public void verifyThatAlarmIsTurnedOn_when_SystemInActivePendingStateIsNotifiedByASensor(
            ArmingStatus status, SensorType type) {
        // Arrange
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(status);
        var sensor = new Sensor(new RandomString().nextString(), type);

        // Act
        securityService.changeSensorActivationStatus(sensor, true);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @DisplayName("Verify that Alarm is Turned Off when System in Active Pending State has no Senors Enabled")
    @MethodSource("getArmedSenors")
    public void verifyThatAlarmIsTurnedOff_when_SystemInActivePendingStateHasNoSenorsEnabled(
            ArmingStatus status, SensorType type) {
        // Arrange
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        var sensor = new Sensor(new RandomString().nextString(), type);
        sensor.setActive(true);

        // Act
        securityService.changeSensorActivationStatus(sensor, false);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
}
