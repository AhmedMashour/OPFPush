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

package org.onepf.opfpush.pushsample;

import android.app.Application;

import org.onepf.opfpush.ExponentialBackoff;
import org.onepf.opfpush.OPFPushHelper;
import org.onepf.opfpush.OPFPushLog;
import org.onepf.opfpush.Options;
import org.onepf.opfpush.gcm.GCMProvider;
import org.onepf.opfpush.pushsample.listener.DemoEventListener;

/**
 * @author Roman Savin
 * @since 09.12.14
 */
public class DemoApplication extends Application {

    private static final String GCM_SENDER_ID = "539088697591";

    @Override
    public void onCreate() {
        super.onCreate();
        final Options.Builder optionsBuilder = new Options.Builder()
                .addProviders(new GCMProvider(this, GCM_SENDER_ID))
                .setBackoff(new ExponentialBackoff())
                .setSelectSystemPreferred(true)
                .setEventListener(new DemoEventListener());

        OPFPushLog.setLogEnable(true);
        final OPFPushHelper helper = OPFPushHelper.getInstance(this);
        helper.init(optionsBuilder.build());
        helper.register();
    }
}