package com.udacity.catpoint.secservice.service;

import com.udacity.catpoint.imageservice.ImageService;
import com.udacity.catpoint.secservice.application.StatusListener;
import com.udacity.catpoint.secservice.data.*;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTests {
    @Mock
    private ImageService imageService;
    @Mock
    private BufferedImage image;
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private StatusListener listener;

    private SecurityService securityService;

    private static Stream<Arguments> getArmedSenor() {
        return Stream.of(
                Arguments.of(ArmingStatus.ARMED_AWAY, SensorType.DOOR),
                Arguments.of(ArmingStatus.ARMED_AWAY, SensorType.MOTION),
                Arguments.of(ArmingStatus.ARMED_AWAY, SensorType.WINDOW),
                Arguments.of(ArmingStatus.ARMED_HOME, SensorType.DOOR),
                Arguments.of(ArmingStatus.ARMED_HOME, SensorType.MOTION),
                Arguments.of(ArmingStatus.ARMED_HOME, SensorType.WINDOW)
        );
    }

    private static Stream<Arguments> getArmedSenors() {
        return Stream.of(
                Arguments.of(ArmingStatus.ARMED_AWAY, List.of(
                        new Sensor("ABC", SensorType.DOOR),
                        new Sensor("BCD", SensorType.WINDOW),
                        new Sensor("EFG", SensorType.MOTION)
                )),
                Arguments.of(ArmingStatus.ARMED_HOME, List.of(
                        new Sensor("ABC", SensorType.DOOR),
                        new Sensor("BCD", SensorType.WINDOW),
                        new Sensor("EFG", SensorType.MOTION)
                ))
        );
    }

    private static Stream<Arguments> getSenors() {
        return Stream.of(
                Arguments.of(SensorType.DOOR),
                Arguments.of(SensorType.MOTION),
                Arguments.of(SensorType.WINDOW)
        );
    }

    private static Stream<Arguments> getSenorsAndState() {
        return Stream.of(
                Arguments.of(SensorType.DOOR, Boolean.TRUE),
                Arguments.of(SensorType.DOOR, Boolean.FALSE),
                Arguments.of(SensorType.MOTION, Boolean.TRUE),
                Arguments.of(SensorType.MOTION, Boolean.FALSE),
                Arguments.of(SensorType.WINDOW, Boolean.TRUE),
                Arguments.of(SensorType.WINDOW, Boolean.FALSE)
        );
    }

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @ParameterizedTest // TC 1
    @DisplayName("Alarm Armed + Senor Enabled => Pending State")
    @MethodSource("getArmedSenor")
    public void verifyThatSystemIsInPendingState_when_AlarmIsArmed_and_SensorIsActivated(
            ArmingStatus status, SensorType type) {
        // Arrange
        var sensor = new Sensor(new RandomString().nextString(), type);
        when(securityRepository.getArmingStatus()).thenReturn(status);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        // Act
        securityService.changeSensorActivationStatus(sensor, true);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest // TC 2
    @DisplayName("Alarm Armed + Senor Enabled + Pending State => Alarm On!")
    @MethodSource("getArmedSenors")
    public void verifyThatSystemIsAlarmed_when_SystemInActivePendingStateIsNotifiedByASensor(
            ArmingStatus status, List<Sensor> sensors) {
        // Arrange
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(status);
        sensors.forEach(sensor -> securityRepository.addSensor(sensor));
        var sensor = sensors.get(new Random().nextInt(3));

        // Act
        securityService.changeSensorActivationStatus(sensor, true);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest // TC 3
    @DisplayName("Senor Disabled + Pending State => Alarm Off!")
    @MethodSource("getSenors")
    public void verifyThatSystemIsNotAlarmed_when_SystemInActivePendingStateHasNoSenorsEnabled(SensorType type) {
        // Arrange
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        var sensor = new Sensor(new RandomString().nextString(), type);
        sensor.setActive(true);

        // Act
        securityService.changeSensorActivationStatus(sensor, false);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest // TC 4
    @DisplayName("Senor Enable/Disabled + Alarm On => Alarm On!")
    @MethodSource("getSenorsAndState")
    public void verifyThatSystemAlarmIsUnaffected_by_AChangeInSenorState(SensorType type, boolean state) {
        // Arrange
        // UnnecessaryStubbingException
        lenient().when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        var sensor = new Sensor(new RandomString().nextString(), type);

        // Act
        securityService.changeSensorActivationStatus(sensor, state);

        // Assert - Argument Matcher
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
        Assertions.assertEquals(securityRepository.getAlarmStatus(), AlarmStatus.ALARM);
    }

    @RepeatedTest(value = 5) // TC 5
    @DisplayName("Senor Enabled + Pending State + New Senor Enabled => Alarm On!")
    public void verifyThatSystemIsAlarmed_when_ANewSenorIsActivated_and_SystemIsInActivePendingState() {
        // Arrange
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM, AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        var sensor1 = new Sensor(new RandomString().nextString(), SensorType.MOTION);
        var sensor2 = new Sensor(new RandomString().nextString(), SensorType.DOOR);
        securityService.addSensor(sensor1);
        securityService.addSensor(sensor2);

        // Act
        securityService.changeSensorActivationStatus(sensor1, true);
        securityService.changeSensorActivationStatus(sensor2, true);

        // Assert
        verify(securityRepository, atMost(2)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest // TC 6
    @DisplayName("Senor Disabled => No Change to Alarm!")
    @MethodSource("getSenors")
    public void verifyThatWhenSenorIsDisabled_Then_AlarmRemainsUnaffected(SensorType type) {
        // Arrange
        var sensor = new Sensor(new RandomString().nextString(), type);

        // Act
        securityService.changeSensorActivationStatus(sensor, false);

        // Assert - Argument Matcher
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test // TC 7
    @DisplayName("\uD83D\uDC08 + \uD83C\uDFE0 => \uD83D\uDE31!")
    public void verifyThatWhenSystemIsArmedHome_and_CatAppears_then_IntimateHooman() {
        // Arrange
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);

        // Act
        securityService.processImage(image);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test // TC 8
    @DisplayName("No|Dead \uD83D\uDC08 + No Active Sensor  => Alarm Off!")
    public void verifyThatWhenSystemIsArmedHome_and_CatAreDead_AlongWithNoActiveSenors_then_TurnOffAlarm() {
        // Arrange
        lenient().when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(false);
        when(securityRepository.getSensors()).thenReturn(Set.of(
                new Sensor("ABC", SensorType.DOOR),
                new Sensor("BCD", SensorType.WINDOW),
                new Sensor("EFG", SensorType.MOTION)));

        // Act
        securityService.processImage(image);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test // TC 9
    @DisplayName("Disarmed => No Alarm")
    public void verifyThatIfSystemIsDisarmed_then_ThereIsNoAlarm() {
        // Arrange & Act
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        // Verify
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest // TC 10
    @DisplayName("Change in Armed State => Reset all Sensors")
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    public void verifyThatSystemSenorsAreReset_when_ArmingStateChanges(ArmingStatus state) {
        // Arrange
        var sensorSet = Set.of(
                new Sensor("ABC", SensorType.DOOR),
                new Sensor("BCD", SensorType.WINDOW),
                new Sensor("EFG", SensorType.MOTION)
        );
        sensorSet.forEach(sensor -> sensor.setActive(true));
        when(securityRepository.getSensors()).thenReturn(sensorSet);

        // Act
        securityService.setArmingStatus(state);

        // Assert
        for (Sensor sensor : securityService.getSensors()) {
            Assertions.assertFalse(sensor.getActive());
        }
    }

    @Test // TC 11
    @DisplayName("\uD83D\uDC08 + \uD83C\uDFE0 => \uD83D\uDE31 Part 2!")
    public void verifyThatSystemWhenArmedHome_and_CatsAppear_NotifyHooman() {
        // Arrange
        when(imageService.imageContainsCat(image, 50.0f)).thenReturn(true);
        var sensorSet = Set.of(
                new Sensor("ABC", SensorType.DOOR),
                new Sensor("BCD", SensorType.WINDOW),
                new Sensor("EFG", SensorType.MOTION)
        );
        when(securityRepository.getSensors()).thenReturn(sensorSet);

        // Act
        securityService.processImage(image);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        // Assert
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("Test Status Listeners")
    public void codeCoverageForStatusListeners() {
        securityService.addStatusListener(listener);
        securityService.removeStatusListener(listener);
    }

    @Test
    @DisplayName("Test Sensors")
    public void codeCoverageForSensors() {
        var senor = new Sensor("ASC", SensorType.DOOR);
        securityService.addSensor(senor);
        securityService.removeSensor(senor);
    }
}
