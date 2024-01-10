/*  Copyright (C) 2022-2024 Daniel Dakhno, José Rebelo, thermatk

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgtr3;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.Huami2021Coordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.AbstractHuami2021FWInstallHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.amazfitgtr3.AmazfitGTR3Support;

public class AmazfitGTR3Coordinator extends Huami2021Coordinator {
    private static final Logger LOG = LoggerFactory.getLogger(AmazfitGTR3Coordinator.class);

    @NonNull
    @Override
    public boolean supports(final GBDeviceCandidate candidate) {
        try {
            final String name = candidate.getName();
            if (name != null && name.startsWith(HuamiConst.AMAZFIT_GTR3_NAME) && !name.contains("Pro")) {
                return true;
            }
        } catch (final Exception e) {
            LOG.error("unable to check device support", e);
        }

        return false;
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return AmazfitGTR3Support.class;
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_amazfit_gtr3;
    }

    @Override
    public AbstractHuami2021FWInstallHandler createFwInstallHandler(final Uri uri, final Context context) {
        return new AmazfitGTR3FWInstallHandler(uri, context);
    }

    @Override
    public boolean sendAgpsAsFileTransfer() {
        return false;
    }

    @Override
    public boolean supportsBluetoothPhoneCalls(final GBDevice device) {
        return false;
    }
}
