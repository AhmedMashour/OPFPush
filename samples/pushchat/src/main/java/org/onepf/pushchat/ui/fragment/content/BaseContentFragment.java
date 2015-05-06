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

package org.onepf.pushchat.ui.fragment.content;

import org.onepf.pushchat.ui.fragment.BaseFragment;

/**
 * @author Roman Savin
 * @since 29.04.2015
 */
public class BaseContentFragment extends BaseFragment {

    protected void showProgressBar() {
        getMainActivity().showProgressbar();
    }

    protected void hideProgressBar() {
        getMainActivity().hideProgressBar();
    }

    protected void showShareButton() {
        getMainActivity().showShareButton();
    }

    protected void hideShareButton() {
        getMainActivity().hideShareButton();
    }

    protected void showClearButton() {
        getMainActivity().showClearButton();
    }

    protected void hideClearButton() {
        getMainActivity().hideClearButton();
    }
}
