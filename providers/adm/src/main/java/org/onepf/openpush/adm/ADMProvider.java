/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.openpush.adm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;

import org.onepf.openpush.BasePushProvider;

/**
 * Created by Kirill Rozov on 06.09.14.
 */
public class ADMProvider extends BasePushProvider {

    public static final String NAME = "Amazon Device Messaging";
    private static final String KINDLE_STORE_APP_PACKAGE = "com.amazon.venezia";

    @NonNull
    private final ADM mAdm;

    public ADMProvider(@NonNull Context context) {
        super(context, NAME, KINDLE_STORE_APP_PACKAGE);
        mAdm = new ADM(context);
    }

    @Override
    public void register() {
        mAdm.startRegister();
    }

    @Override
    public boolean checkManifest() {
        if (super.checkManifest() && ADMUtils.isKindleFire()) {
            Context ctx = getContext();
            return super.checkManifest()
                    && checkPermission(ctx, android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
                    && checkPermission(ctx, ADMManifest.PERMISSION_RECEIVE_MESSAGES)
                    && checkPermission(ctx, ctx.getPackageName() + ".permission.RECEIVE_ADM_MESSAGE");
        } else {
            return false;
        }
    }

    @Override
    public void unregister() {
        mAdm.startUnregister();
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && mAdm.isSupported();
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        return mAdm.getRegistrationId();
    }
}
