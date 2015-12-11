/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nullable;

/**
 * Interface corresponding to {@link com.google.common.util.concurrent.SettableFuture}. Unlike the Guava version,
 * which is a final class, this interface provides the ability to override actions the future performs.
 *
 * @param <V> The result type returned by this Future's <tt>get</tt> method
 */
@Beta
public interface SettableFuture<V> extends ListenableFuture<V> {
    /**
     * Sets the value of this future.  This method will return {@code true} if
     * the value was successfully set, or {@code false} if the future has already
     * been set or cancelled.
     *
     * @param value the value the future should hold.
     * @return true if the value was successfully set.
     */
    public boolean set(@Nullable V value);

    /**
     * Sets the future to having failed with the given exception. This exception
     * will be wrapped in an {@code ExecutionException} and thrown from the {@code
     * get} methods. This method will return {@code true} if the exception was
     * successfully set, or {@code false} if the future has already been set or
     * cancelled.
     *
     * @param throwable the exception the future should hold.
     * @return true if the exception was successfully set.
     */
    public boolean setException(Throwable throwable);
}
