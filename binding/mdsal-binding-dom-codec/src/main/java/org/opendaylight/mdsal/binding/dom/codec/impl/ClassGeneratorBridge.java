/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Bridge for initializing generated instance constants during class loading time. This class is public only due to
 * implementation restrictions and can change at any time.
 */
@Beta
public final class ClassGeneratorBridge {
    interface BridgeProvider {

    }

    interface LocalNameProvider extends BridgeProvider {

        @NonNull String resolveLocalName(@NonNull String methodName);
    }

    interface NodeContextSupplierProvider extends BridgeProvider {

        @NonNull NodeContextSupplier resolveNodeContextSupplier(@NonNull String methodName);
    }

    private static final ThreadLocal<BridgeProvider> CURRENT_CUSTOMIZER = new ThreadLocal<>();

    private ClassGeneratorBridge() {

    }

    public static @NonNull NodeContextSupplier resolveNodeContextSupplier(final @NonNull String methodName) {
        return current(NodeContextSupplierProvider.class).resolveNodeContextSupplier(methodName);
    }

    public static @NonNull String resolveLocalName(final @NonNull String methodName) {
        return current(LocalNameProvider.class).resolveLocalName(methodName);
    }

    static @Nullable BridgeProvider setup(final @NonNull BridgeProvider next) {
        final BridgeProvider prev = CURRENT_CUSTOMIZER.get();
        CURRENT_CUSTOMIZER.set(verifyNotNull(next));
        return prev;
    }

    static void tearDown(final @Nullable BridgeProvider prev) {
        if (prev == null) {
            CURRENT_CUSTOMIZER.remove();
        } else {
            CURRENT_CUSTOMIZER.set(prev);
        }
    }

    private static <T extends BridgeProvider> @NonNull T current(final Class<T> requested) {
        return requested.cast(verifyNotNull(CURRENT_CUSTOMIZER.get(), "No customizer attached"));
    }
}
