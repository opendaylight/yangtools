/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link UnsafeAccess}. The operation of this implementation can be adjusted on a JVM-global
 * basis using the following properties:
 * <ul>
 *   <li>when {@value #VERIFY_STO_PROP} is set to {@code "true"}, all attempts to unsafely instantiate
 *       {@link ScalarTypeObject}s will use the usual safe instantiation</li>
 * </ul>
 *
 * @since 15.0.3
 */
@NonNullByDefault
public final class DefaultUnsafeAccess implements UnsafeAccess {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUnsafeAccess.class);
    private static final String VERIFY_STO_PROP = "odl.binding.spec.unsafe.verify-sto";
    private static final boolean VERIFY_STO;

    static {
        final var verifySTO = System.getProperty(VERIFY_STO_PROP);
        VERIFY_STO = switch (verifySTO) {
            case null -> {
                LOG.debug("Unsafe ScalarTypeObject instantiation enabled");
                yield false;
            }
            case "false" -> {
                LOG.info("Unsafe ScalarTypeObject instantiation enabled");
                yield false;
            }
            case "true" -> {
                LOG.info("Unsafe ScalarTypeObject instantiation disabled");
                yield true;
            }
            default -> {
                LOG.warn("Unrecognized {} property value '{}', disabling unsafe ScalarTypeObject instantiation",
                    VERIFY_STO_PROP, verifySTO);
                yield true;
            }
        };
    }

    private final ConcurrentHashMap<Class<?>, UnsafeScalarTypeObjectFactory<?, ?>> unsafeFactories =
        new ConcurrentHashMap<>();
    private final String rootPackageName;
    private final Module definingModule;

    /**
     * Construct a new instance tied to a {@link Module} and a root package name of the generated code.
     *
     * @param rootPackageName the root package
     * @param definingModule the defining module
     */
    public DefaultUnsafeAccess(final String rootPackageName, final Module definingModule) {
        this.rootPackageName = requireNonNull(rootPackageName);
        this.definingModule = requireNonNull(definingModule);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ScalarTypeObject<V>, V>
            @Nullable UnsafeScalarTypeObjectFactory<T, V> lookupUnsafeScalarTypeObjectFactory(
                final Class<T> typeClass) {
        return (UnsafeScalarTypeObjectFactory<T, V>) unsafeFactories.get(typeClass.asSubclass(ScalarTypeObject.class));
    }

    /**
     * Register a {@link ScalarTypeObject} capable of unsafe construction. There is no facility for unregistration, as
     * this method is meant to be invoked during a ScalarTypeObject's class initialization.
     *
     * @param <T> the {@link ScalarTypeObject} type
     * @param <V> the value type
     * @param typeClass the {@link ScalarTypeObject} class
     * @param safeCtor the safe constructor
     * @param unsafeCtor the unsafe constructor
     */
    public <T extends ScalarTypeObject<V>, V> void registerScalarTypeObject(final Class<T> typeClass,
            final Function<V, T> safeCtor, final BiFunction<UnsafeSecret, V, T> unsafeCtor) {
        if (ScalarTypeObject.class.isAssignableFrom(typeClass)) {
            throw new IllegalArgumentException(typeClass + " is not a ScalarTypeObject");
        }
        final var typePackageName = typeClass.asSubclass(ScalarTypeObject.class).getPackageName();
        if (!typePackageName.startsWith(rootPackageName)) {
            throw new IllegalArgumentException(typePackageName + " does not match " + rootPackageName);
        }
        final var typeModule = typeClass.getModule();
        if (!typeModule.equals(definingModule)) {
            throw new IllegalArgumentException(typeModule + " does not match " + definingModule);
        }

        final var safe = requireNonNull(safeCtor);
        final var unsafe = requireNonNull(unsafeCtor);
        final var factory = VERIFY_STO ? new VerifyingUnsafeScalarTypeObjectFactory<>(typeClass, safe)
            : new DefaultUnsafeScalarTypeObjectFactory<>(typeClass, unsafe);

        final var prev = unsafeFactories.putIfAbsent(typeClass, factory);
        if (prev != null) {
            throw new IllegalArgumentException(typeClass + " already registered as " + prev);
        }

        LOG.debug("Registered {} for unsafe instantiation", typeClass.getCanonicalName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("rootPackage", rootPackageName)
            .add("module", definingModule)
            .toString();
    }
}
