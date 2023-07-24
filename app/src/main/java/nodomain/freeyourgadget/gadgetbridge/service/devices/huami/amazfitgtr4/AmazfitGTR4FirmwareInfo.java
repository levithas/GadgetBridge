/*  Copyright (C) 2022 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.amazfitgtr4;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.Huami2021FirmwareInfo;

public class AmazfitGTR4FirmwareInfo extends Huami2021FirmwareInfo {
    private static final Map<Integer, String> crcToVersion = new HashMap<Integer, String>() {{
        // firmware
        put(1699, "3.17.0.2");
        put(20712, "3.18.1.1 (diff from 3.17.0.2)");
        put(49685, "3.23.3.1 (diff from 3.21.0.1)");
    }};

    public AmazfitGTR4FirmwareInfo(final byte[] bytes) {
        super(bytes);
    }

    @Override
    public String deviceName() {
        return HuamiConst.AMAZFIT_GTR4_NAME;
    }

    @Override
    public Set<Integer> deviceSources() {
        return new HashSet<>(Arrays.asList(7930112, 7930113));
    }

    @Override
    public boolean isGenerallyCompatibleWith(final GBDevice device) {
        return isHeaderValid() && device.getType() == DeviceType.AMAZFITGTR4;
    }

    @Override
    protected Map<Integer, String> getCrcMap() {
        return crcToVersion;
    }
}
