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
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader.ClassGenerator;

/**
 * Bridge for initializing generated instance constants during class loading time. This class is public only due to
 * implementation restrictions and can change at any time.
 */
@Beta
public final class ClassGeneratorBridge {
    interface BridgeProvider<T> extends ClassGenerator<T> {
        @Override
        default Class<T> customizeLoading(final @NonNull Supplier<Class<T>> loader) {
            final var prev = ClassGeneratorBridge.setup(this);
            try {
                final var result = loader.get();

                /*
                 * This a bit of magic to support NodeContextSupplier constants. These constants need to be resolved
                 * while we have the information needed to find them -- that information is being held in this instance
                 * and we leak it to a thread-local variable held by CodecDataObjectBridge.
                 *
                 * By default the JVM will defer class initialization to first use, which unfortunately is too late for
                 * us, and hence we need to force class to initialize.
                 */
                try {
                    Class.forName(result.getName(), true, result.getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new LinkageError("Failed to find newly-defined " + result, e);
                }

                return result;
            } finally {
                ClassGeneratorBridge.tearDown(prev);
            }
        }
    }

    interface LocalNameProvider<T> extends BridgeProvider<T> {

        @NonNull String resolveLocalName(@NonNull String methodName);
    }

    interface NodeContextSupplierProvider<T> extends BridgeProvider<T> {

        @NonNull NodeContextSupplier resolveNodeContextSupplier(@NonNull String methodName);
    }

    private static final ThreadLocal<BridgeProvider<?>> CURRENT_CUSTOMIZER = new ThreadLocal<>();

    private ClassGeneratorBridge() {
        // Hidden on purpose
    }

    public static @NonNull NodeContextSupplier resolveNodeContextSupplier(final @NonNull String methodName) {
        return current(NodeContextSupplierProvider.class).resolveNodeContextSupplier(methodName);
    }

    public static @NonNull String resolveLocalName(final @NonNull String methodName) {
        return current(LocalNameProvider.class).resolveLocalName(methodName);
    }

    static @Nullable BridgeProvider<?> setup(final @NonNull BridgeProvider<?> next) {
        final var prev = CURRENT_CUSTOMIZER.get();
        CURRENT_CUSTOMIZER.set(verifyNotNull(next));
        return prev;
    }

    static void tearDown(final @Nullable BridgeProvider<?> prev) {
        if (prev == null) {
            CURRENT_CUSTOMIZER.remove();
        } else {
            CURRENT_CUSTOMIZER.set(prev);
        }
    }

    private static <T extends BridgeProvider<?>> @NonNull T current(final Class<T> requested) {
        return requested.cast(verifyNotNull(CURRENT_CUSTOMIZER.get(), "No customizer attached"));
    }
}
