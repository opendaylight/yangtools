/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated singleton type for YANG's {@code type empty} value.
 */
@NonNullByDefault
public final class Empty implements Immutable, Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Empty VALUE = new Empty();
    private static final CompletionStage<Empty> COMPLETED_FUTURE = CompletableFuture.completedFuture(VALUE);
    private static final ListenableFuture<Empty> IMMEDIATE_FUTURE = Futures.immediateFuture(VALUE);

    private Empty() {
        // Hidden on purpose
    }

    /**
     * Return the singleton {@link Empty} value.
     *
     * @return Empty value.
     */
    public static Empty value()  {
        return VALUE;
    }

    /**
     * Return a {@link CompletionStage} completed with {@link #value()}.
     *
     * @return A completed CompletionStage
     */
    public static CompletionStage<Empty> completedFuture() {
        return COMPLETED_FUTURE;
    }

    /**
     * Return a {@link ListenableFuture} completed with {@link #value()}.
     *
     * @return A completed ListenableFuture
     */
    public static ListenableFuture<Empty> immediateFuture() {
        return IMMEDIATE_FUTURE;
    }

    @Override
    public int hashCode() {
        return 1337;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return "empty";
    }

    @java.io.Serial
    @SuppressWarnings("static-method")
    private Object readResolve() {
        return VALUE;
    }
}
