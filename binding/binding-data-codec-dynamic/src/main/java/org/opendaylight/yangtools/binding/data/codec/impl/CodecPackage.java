/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.ClassGenerator;

/**
 * Centralized registry of Java package names used by classes generated by codec components.
 */
enum CodecPackage {
    /**
     * Package holding {@link CodecDataObject}s, {@link CodecOpaqueObject}s and similar.
     */
    CODEC("org.opendaylight.yang.rt.v1.obj"),
    /**
     * Package holding {@link DataObjectStreamer}s.
     */
    STREAMER("org.opendaylight.yang.rt.v1.stream"),
    /**
     * Package holding @link EventInstantAware} specializations of {@code notification} objects.
     */
    EVENT_AWARE("org.opendaylight.yang.rt.v1.eia");

    private static final int PACKAGE_PREFIX_LENGTH = Naming.PACKAGE_PREFIX.length();

    private final String packagePrefix;

    CodecPackage(final String packagePrefix) {
        this.packagePrefix = requireNonNull(packagePrefix);
    }

    <T> @NonNull Class<T> generateClass(final BindingClassLoader loader, final Class<?> bindingInterface,
            final ClassGenerator<T> generator) {
        return loader.generateClass(bindingInterface, createFQCN(bindingInterface), generator);
    }

    @NonNull Class<?> getGeneratedClass(final BindingClassLoader loader, final Class<?> bindingInterface) {
        return loader.getGeneratedClass(bindingInterface, createFQCN(bindingInterface));
    }

    private @NonNull String createFQCN(final Class<?> bindingInterface) {
        final var ifName = bindingInterface.getName();
        checkArgument(ifName.startsWith(Naming.PACKAGE_PREFIX), "Unrecognized interface %s", bindingInterface);
        return packagePrefix + ifName.substring(PACKAGE_PREFIX_LENGTH);
    }
}
