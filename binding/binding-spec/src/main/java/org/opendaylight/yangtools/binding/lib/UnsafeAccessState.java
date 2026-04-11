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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State shared between {@link DefaultSTORegistrar} and {@link DefaultUnsafeAccess}.
 */
// TODO: value class when we have JEP-401 available
@NonNullByDefault
final class UnsafeAccessState {
    private static final Logger LOG = LoggerFactory.getLogger(UnsafeAccessState.class);

    private final ConcurrentHashMap<Class<?>, UnsafeScalarTypeObjectFactory<?, ?>> unsafeFactories =
        new ConcurrentHashMap<>();
    private final String rootPackageName;
    private final Module definingModule;

    UnsafeAccessState(final String rootPackageName, final Module definingModule) {
        this.rootPackageName = requireNonNull(rootPackageName);
        this.definingModule = requireNonNull(definingModule);
        if (!rootPackageName.startsWith(Naming.PACKAGE_PREFIX + ".")) {
            throw new IllegalArgumentException("Invalid root package " + rootPackageName);
        }
    }

    @Nullable UnsafeScalarTypeObjectFactory<?, ?> lookupSTO(final Class<?> clazz) {
        checkClassMembership(clazz);
        final var ret = unsafeFactories.get(clazz);
        if (ret != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Acquired unsafe factory for {}", clazz.getCanonicalName(), new Throwable());
            } else {
                LOG.debug("Acquired unsafe factory for {}", clazz.getCanonicalName());
            }
        }
        return ret;
    }

    void putSTO(final UnsafeScalarTypeObjectFactory<?, ?> factory) {
        final var target = factory.target();
        checkClassMembership(target);

        final var prev = unsafeFactories.putIfAbsent(target, factory);
        if (prev != null) {
            throw new IllegalArgumentException(target + " already registered as " + prev);
        }

        LOG.debug("Registered {} for unsafe instantiation", target.getCanonicalName());
    }

    String computeToString(final Class<?> clazz) {
        return MoreObjects.toStringHelper(clazz)
            .add("rootPackage", rootPackageName)
            .add("module", definingModule)
            .toString();
    }

    @Override
    public String toString() {
        return computeToString(UnsafeAccessState.class);
    }

    private void checkClassMembership(final Class<?> typeClass) {
        final var typePackageName = typeClass.getPackageName();
        if (!typePackageName.startsWith(rootPackageName)) {
            throw new IllegalArgumentException(typePackageName + " does not match " + rootPackageName);
        }
        final var typeModule = typeClass.getModule();
        if (!typeModule.equals(definingModule)) {
            throw new IllegalArgumentException(typeModule + " does not match " + definingModule);
        }
    }
}
