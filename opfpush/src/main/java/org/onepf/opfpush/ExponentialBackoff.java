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

package org.onepf.opfpush;

import android.support.annotation.NonNull;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kirill Rozov
 * @author Roman Savin
 * @since 05.09.14.
 */
public final class ExponentialBackoff implements Backoff {
    public static final int DEFAULT_TRY_COUNT = Integer.MAX_VALUE;
    private final int tryCount;

    @NonNull
    private final AtomicInteger tryNumber = new AtomicInteger(0);

    public ExponentialBackoff(int tryCount) {
        if (tryCount < 1) {
            throw new IllegalArgumentException("Try count can't less than 1.");
        }
        this.tryCount = tryCount;
    }

    public ExponentialBackoff() {
        tryCount = DEFAULT_TRY_COUNT;
    }

    /**
     * Compute summary delay for all tries.
     */
    public int summaryDelay() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getTryDelay() {
        if (tryNumber.get() > tryCount) {
            throw new NoSuchElementException();
        }
        return getTryDelay(tryNumber.get());
    }

    private long getTryDelay(int currentTryNumber) {
        return TimeUnit.SECONDS.toMillis((long) Math.pow(2, currentTryNumber));
    }

    @Override
    public void reset() {
        tryNumber.set(0);
    }

    @Override
    public boolean hasTries() {
        return tryNumber.getAndIncrement() < tryCount;
    }
}
