/*  Copyright (C) 2024 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.devices.banglejs;

import androidx.annotation.NonNull;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractTimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSSleepStageSample;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSSleepStageSampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class BangleJSSleepStageSampleProvider extends AbstractTimeSampleProvider<BangleJSSleepStageSample> {
    public BangleJSSleepStageSampleProvider(final GBDevice device, final DaoSession session) {
        super(device, session);
    }

    @NonNull
    @Override
    public AbstractDao<BangleJSSleepStageSample, ?> getSampleDao() {
        return getSession().getBangleJSSleepStageSampleDao();
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return BangleJSSleepStageSampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return BangleJSSleepStageSampleDao.Properties.DeviceId;
    }

    @Override
    public BangleJSSleepStageSample createSample() {
        return new BangleJSSleepStageSample();
    }
}
