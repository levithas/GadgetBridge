/*  Copyright (C) 2020 Andreas Shimokawa, Taavi Eomäe

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.pinetime;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventMusicControl;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.pinetime.PineTimeJFConstants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.IntentListener;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.alertnotification.AlertCategory;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.alertnotification.AlertNotificationProfile;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.alertnotification.NewAlert;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.alertnotification.OverflowStrategy;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfoProfile;

public class PineTimeJFSupport extends AbstractBTLEDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PineTimeJFSupport.class);
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
    private final DeviceInfoProfile<PineTimeJFSupport> deviceInfoProfile;

    /**
     * These are used to keep track when long strings haven't changed,
     * thus avoiding unnecessary transfers that are (potentially) very slow.
     * <p>
     * Makes the device's UI more responsive.
     */
    String lastAlbum;
    String lastTrack;

    private void setInitialized(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }

    private void requestDeviceInfo(TransactionBuilder builder) {
        LOG.debug("Requesting Device Info!");
        deviceInfoProfile.requestDeviceInfo(builder);
    }

    private void handleDeviceInfo(nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo info) {
        LOG.warn("Device info: " + info);
        versionCmd.hwVersion = info.getHardwareRevision();
        versionCmd.fwVersion = info.getFirmwareRevision();
        handleGBDeviceEvent(versionCmd);
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        TransactionBuilder builder = new TransactionBuilder("notification");
        NewAlert alert = new NewAlert(AlertCategory.CustomHuami, 1, notificationSpec.body + " "); // HACK: no idea why the last byte is swallowed
        AlertNotificationProfile<?> profile = new AlertNotificationProfile<>(this);
        profile.newAlert(builder, alert, OverflowStrategy.TRUNCATE);
        builder.queue(getQueue());
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetTime() {
        // since this is a standard we should generalize this in Gadgetbridge (properly)
        GregorianCalendar now = BLETypeConversions.createCalendar();
        byte[] bytes = BLETypeConversions.calendarToRawBytes(now);
        byte[] tail = new byte[]{0, BLETypeConversions.mapTimeZone(now.getTimeZone(), BLETypeConversions.TZ_FLAG_INCLUDE_DST_IN_TZ)};
        byte[] all = BLETypeConversions.join(bytes, tail);

        TransactionBuilder builder = new TransactionBuilder("set time");
        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_CURRENT_TIME), all);
        builder.queue(getQueue());
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {

    }

    @Override
    public void onSetCallState(CallSpec callSpec) {

    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {

    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {

    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppInfoReq() {

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {

    }

    @Override
    public void onAppDelete(UUID uuid) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onAppReorder(UUID[] uuids) {

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {

    }

    @Override
    public void onReset(int flags) {

    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {

    }

    @Override
    public void onFindDevice(boolean start) {
        onSetConstantVibration(start ? 0xff : 0x00);
    }

    @Override
    public void onSetConstantVibration(int intensity) {
    }

    @Override
    public void onScreenshotReq() {

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {

    }

    String lastArtist;

    public PineTimeJFSupport() {
        super(LOG);
        addSupportedService(GattService.UUID_SERVICE_ALERT_NOTIFICATION);
        addSupportedService(GattService.UUID_SERVICE_CURRENT_TIME);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(PineTimeJFConstants.UUID_SERVICE_MUSIC_CONTROL);
        deviceInfoProfile = new DeviceInfoProfile<>(this);
        IntentListener mListener = new IntentListener() {
            @Override
            public void notify(Intent intent) {
                String action = intent.getAction();
                if (DeviceInfoProfile.ACTION_DEVICE_INFO.equals(action)) {
                    handleDeviceInfo((nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo) intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
                }
            }
        };
        deviceInfoProfile.addListener(mListener);
        AlertNotificationProfile<PineTimeJFSupport> alertNotificationProfile = new AlertNotificationProfile<>(this);
        addSupportedProfile(alertNotificationProfile);
        addSupportedProfile(deviceInfoProfile);
    }

    /**
     * Helper function that ust converts an integer into a byte array
     */
    private static byte[] intToBytes(int source) {
        return ByteBuffer.allocate(4).putInt(source).array();
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));
        requestDeviceInfo(builder);
        onSetTime();
        builder.notify(getCharacteristic(PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_EVENT), true);
        setInitialized(builder);
        return builder;
    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
        try {
            TransactionBuilder builder = performInitialized("send playback info");

            if (musicSpec.album != null && !musicSpec.album.equals(lastAlbum)) {
                lastAlbum = musicSpec.album;
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_ALBUM, musicSpec.album.getBytes());
            }
            if (musicSpec.track != null && !musicSpec.track.equals(lastTrack)) {
                lastTrack = musicSpec.track;
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_TRACK, musicSpec.track.getBytes());
            }
            if (musicSpec.artist != null && !musicSpec.artist.equals(lastArtist)) {
                lastArtist = musicSpec.artist;
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_ARTIST, musicSpec.artist.getBytes());
            }

            if (musicSpec.duration != MusicSpec.MUSIC_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_LENGTH_TOTAL, intToBytes(musicSpec.duration));
            }
            if (musicSpec.trackNr != MusicSpec.MUSIC_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_TRACK_NUMBER, intToBytes(musicSpec.trackNr));
            }
            if (musicSpec.trackCount != MusicSpec.MUSIC_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_TRACK_TOTAL, intToBytes(musicSpec.trackCount));
            }

            builder.queue(getQueue());
        } catch (Exception e) {
            LOG.error("error sending music info", e);
        }
    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
        try {
            TransactionBuilder builder = performInitialized("send playback state");

            if (stateSpec.state != MusicStateSpec.STATE_UNKNOWN) {
                byte[] state = new byte[1];
                if (stateSpec.state == MusicStateSpec.STATE_PLAYING) {
                    state[0] = 0x01;
                }
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_STATUS, state);
            }

            if (stateSpec.playRate != MusicStateSpec.STATE_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_PLAYBACK_SPEED, intToBytes(stateSpec.playRate));
            }

            if (stateSpec.position != MusicStateSpec.STATE_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_POSITION, intToBytes(stateSpec.position));
            }

            if (stateSpec.repeat != MusicStateSpec.STATE_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_REPEAT, intToBytes(stateSpec.repeat));
            }

            if (stateSpec.shuffle != MusicStateSpec.STATE_UNKNOWN) {
                safeWriteToCharacteristic(builder, PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_SHUFFLE, intToBytes(stateSpec.repeat));
            }

            builder.queue(getQueue());

        } catch (Exception e) {
            LOG.error("error sending music state", e);
        }

    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic, int status) {
        if (super.onCharacteristicRead(gatt, characteristic, status)) {
            return true;
        }
        UUID characteristicUUID = characteristic.getUuid();

        LOG.info("Unhandled characteristic read: " + characteristicUUID);
        return false;
    }

    @Override
    public void onSendConfiguration(String config) {

    }

    @Override
    public void onReadConfiguration(String config) {

    }

    @Override
    public void onTestNewFunction() {

    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }

        UUID characteristicUUID = characteristic.getUuid();
        if (characteristicUUID.equals(PineTimeJFConstants.UUID_CHARACTERISTICS_MUSIC_EVENT)) {
            byte[] value = characteristic.getValue();
            GBDeviceEventMusicControl deviceEventMusicControl = new GBDeviceEventMusicControl();

            switch (value[0]) {
                case 0:
                    deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.PLAY;
                    break;
                case 1:
                    deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.PAUSE;
                    break;
                case 3:
                    deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.NEXT;
                    break;
                case 4:
                    deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.PREVIOUS;
                    break;
                case 5:
                    deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.VOLUMEUP;
                    break;
                case 6:
                    deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.VOLUMEDOWN;
                    break;
                default:
                    return false;
            }
            evaluateGBDeviceEvent(deviceEventMusicControl);
            return true;
        }
        LOG.info("Unhandled characteristic changed: " + characteristicUUID);
        return false;
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {

    }

    /**
     * This will check if the characteristic exists and can be written
     * <p>
     * Keeps backwards compatibility with firmware that can't take all the information
     */
    private void safeWriteToCharacteristic(TransactionBuilder builder, UUID uuid, byte[] data) {
        BluetoothGattCharacteristic characteristic = getCharacteristic(uuid);
        if (characteristic != null &&
                (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            builder.write(characteristic, data);
        }
    }
}
