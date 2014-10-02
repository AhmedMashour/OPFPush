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

package org.onepf.openpush;

import java.util.concurrent.TimeUnit;

/**
 * @author Kirill Rozov
 * @since 05.09.14.
 */
public final class ExponentialBackoff implements Backoff {
    private final int mTryCount;

    public ExponentialBackoff(int tryCount) {
        if (tryCount < 1) {
            throw new IllegalArgumentException("Try count can't less than 1.");
        }
        mTryCount = tryCount;
    }

    public ExponentialBackoff() {
        mTryCount = 4;
    }

    @Override
    public int getTryCount() {
        return mTryCount;
    }

    @Override
    public long getDelay(int tryNumber) {
        if (tryNumber < 1) {
            throw new IllegalArgumentException("Try number can't less than 1.");
        }
        if (tryNumber > mTryCount) {
            throw new IllegalArgumentException("Try number can't more than tryCount().");
        }
        return TimeUnit.SECONDS.toMillis(2 << (tryNumber - 1));
    }
}