/*
 * Copyright 2012-2015 One Platform Foundation
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

package org.onepf.pushchat.ui.fragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.onepf.pushchat.PushChatApplication;
import org.onepf.pushchat.ui.activity.MainActivity;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class BaseFragment extends Fragment {

    @NonNull
    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected void closeDrawer() {
        getMainActivity().closeDrawer();
    }

    protected void setToolbarTitle(@NonNull final String title) {
        getMainActivity().setToolbarTitle(title);
    }

    protected PushChatApplication getPushChatApplication() {
        return getMainActivity().getPushChatApplication();
    }
}
