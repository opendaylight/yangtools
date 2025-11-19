/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Bridge for initializing generated instance constants during class loading time. This class is public only due to
 * implementation restrictions and can change at any time.
 */
@Beta
public final class ClassGeneratorBridge {
    /**
     * Base class for possible services that are being bridged. This interface, as well as its implementations, must be
     * kept package-private.
     *
     * <p>This marker interface is weird: it allows for {@link GetterGenerator} implementations, but the baseline
     * specializations are kept as interfaces in this class. The reason for this diamond layour is to keep the bridging
     * contract versus code generation implementation separate all the while strictly keeping control over what gets
     * implemented.
     */
    sealed interface BridgeProvider permits GetterGenerator, CodecContextSupplierProvider, LocalNameProvider {
        // Marker interface only
    }

    /**
     * A {@link BridgeProvider} accessed via {@link #resolveCodecContextSupplier(String)}.
     */
    @NonNullByDefault
    sealed interface CodecContextSupplierProvider extends BridgeProvider permits FixedGetterGenerator {
        /**
         * Returns the {@link CodecContextSupplier} for specified method name.
         *
         * @param methodName the method name
         * @return the {@link CodecContextSupplier}
         */
        CodecContextSupplier resolveCodecContextSupplier(String methodName);
    }

    /**
     * A {@link BridgeProvider} accessed via {@link #resolveLocalName(String)}.
     */
    @NonNullByDefault
    sealed interface LocalNameProvider extends BridgeProvider permits ReusableGetterGenerator {
        /**
         * Returns the {@link QName#getLocalName()} for specified method name. Returned value needs to be
         * cross-referenced the the underlying schema to find the correct namespace-bound definition.
         *
         * @param methodName the method name
         * @return the local name
         */
        String resolveLocalName(String methodName);
    }

    // FIXME: Java 25: can we use a ScopedValue instead?
    private static final ThreadLocal<BridgeProvider> CURRENT_CUSTOMIZER = new ThreadLocal<>();

    private ClassGeneratorBridge() {
        // Hidden on purpose
    }

    /**
     * Resolve the {@link CodecContextSupplier} for specified method name.
     *
     * @param methodName the method name
     * @return the {@link CodecContextSupplier}
     */
    @NonNullByDefault
    public static CodecContextSupplier resolveCodecContextSupplier(final String methodName) {
        return current(CodecContextSupplierProvider.class).resolveCodecContextSupplier(methodName);
    }

    /**
     * Resolve the YANG local name for specified method name.
     *
     * @param methodName the method name
     * @return the YANG local name
     */
    @NonNullByDefault
    public static String resolveLocalName(final String methodName) {
        return current(LocalNameProvider.class).resolveLocalName(methodName);
    }

    static @Nullable BridgeProvider setup(final @NonNull BridgeProvider next) {
        final var prev = CURRENT_CUSTOMIZER.get();
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
