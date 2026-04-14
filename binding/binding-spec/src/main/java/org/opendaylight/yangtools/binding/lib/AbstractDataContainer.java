/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link DataContainer} implementations. It implements {@link #hashCode()} caching and common
 * handling of {@link #equals(Object)} and {@link #toString()}.
 *
 * @param <T> the {@link DataContainer} type
 * @since 15.1.0
 */
public abstract class AbstractDataContainer<T extends DataContainer & JavaDataContainer<T>>
        implements JavaDataContainer<T> {
    private static final class InconsistentHashCode {
        static final @NonNull Cache<Class<?>, Empty> DAMPENED =
            CacheBuilder.newBuilder().weakKeys().expireAfterWrite(DAMPEN_DURATION).build();
    }

    private static final class ZeroHashCode {
        static final @NonNull Cache<Class<?>, Empty> DAMPENED =
            CacheBuilder.newBuilder().weakKeys().expireAfterWrite(DAMPEN_DURATION).build();
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataContainer.class);
    private static final Duration DAMPEN_DURATION = Duration.ofHours(1);
    private static final VarHandle VH;

    static {
        try {
            VH = MethodHandles.lookup().findVarHandle(AbstractDataContainer.class, "hashCode", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    private volatile int hashCode;

    @Override
    public final int hashCode() {
        final var local = (int) VH.getAcquire(this);
        return local != 0 ? local : loadHashCode();
    }

    private int loadHashCode() {
        final var result = bindingHashCode();
        if (result == 0) {
            warnZeroHashCode();
        }
        final var witness = (int) VH.compareAndExchangeRelease(this, 0, result);
        return witness == 0 ? result : reconcileHashCode(witness, result);
    }

    private int reconcileHashCode(final int stored, final int computed) {
        return stored == computed ? computed : warnInconsistentHashCode(stored, computed);
    }

    private int warnInconsistentHashCode(final int stored, final int computed) {
        final var clazz = getClass();
        if (recordWarning(InconsistentHashCode.DAMPENED, clazz)) {
            LOG.warn("{} produces inconsistent bindingHashCode(): stored {} computed {}",
                clazz.getCanonicalName(), Integer.toHexString(stored), Integer.toHexString(computed), new Throwable());
        }
        return stored;
    }

    private void warnZeroHashCode() {
        final var clazz = getClass();
        if (recordWarning(ZeroHashCode.DAMPENED, clazz)) {
            LOG.warn("{} produces indingHashCode() == 0, violating the contract",
                clazz.getCanonicalName(), new Throwable());
        }
    }

    private static boolean recordWarning(final @NonNull Cache<Class<?>, Empty> cache, final @NonNull Class<?> clazz) {
        if (cache.getIfPresent(clazz) != null) {
            return false;
        }
        cache.put(clazz, Empty.value());
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean equals(final Object obj) {
        // The unchecked cast here is safe, but we cannot quite express that. We are seeing implementedInterface()
        // as Class<? extends DataContainer> and we should be sharpening the return type to Class<T>. If we were to do
        // that, though, we would override the default method in the generated interface -- effectively forcing
        // subclasses to restore that wiring by:
        //
        //   public Class<Foo> implementedInterface() {
        //     return Foo.super.implementedInterface();
        //   }
        //
        // Which is exactly the bit wiring we want to side-step through the use of ImplementedInterface.
        //
        // So we rely on subclass to not try anything weird: it is bound by the class it returns from
        // implementedInterface(), so it better be giving us the correct contract :)
        return this == obj || implementedInterface().isInstance(obj) && bindingEquals((T) obj);
    }

    @Override
    public final String toString() {
        return bindingToString();
    }
}
