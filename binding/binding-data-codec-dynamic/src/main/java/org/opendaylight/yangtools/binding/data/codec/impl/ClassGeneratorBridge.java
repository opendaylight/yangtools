/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private static final ScopedValue<@NonNull BridgeProvider> CURRENT_PROVIDER = ScopedValue.newInstance();

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
        return ((CodecContextSupplierProvider) CURRENT_PROVIDER.get()).resolveCodecContextSupplier(methodName);
    }

    /**
     * Resolve the YANG local name for specified method name.
     *
     * @param methodName the method name
     * @return the YANG local name
     */
    @NonNullByDefault
    public static String resolveLocalName(final String methodName) {
        return ((LocalNameProvider) CURRENT_PROVIDER.get()).resolveLocalName(methodName);
    }

    /**
     * Load and initialize a class from a loader using specified {@link BridgeProvider}.
     *
     * @param <T> the class being loaded
     * @param provider the {@link BridgeProvider}
     * @param loader the class loader
     * @return the class
     */
    static <T> Class<T> loadWithProvider(final @NonNull BridgeProvider provider,
            final @NonNull Supplier<Class<T>> loader) {
        return ScopedValue.getWhere(CURRENT_PROVIDER, requireNonNull(provider), () -> {
            final var result = loader.get();

            /*
             * This a bit of magic to support NodeContextSupplier constants. These constants need to be resolved while
             * we have the information needed to find them -- that information is being held in this instance and we
             * leak it to a thread-local variable held by CodecDataObjectBridge.
             *
             * By default the JVM will defer class initialization to first use, which unfortunately is too late for us,
             * and hence we need to force class to initialize.
             */
            try {
                Class.forName(result.getName(), true, result.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new LinkageError("Failed to find newly-defined " + result, e);
            }

            return result;
        });
    }
}
