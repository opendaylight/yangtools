/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.meta.YangFeatureProvider;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.binding.runtime.spi.ModuleInfoSnapshotResolver;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class RegularYangModuleInfoRegistry extends YangModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(RegularYangModuleInfoRegistry.class);

    private final ComponentFactory<OSGiModuleInfoSnapshotImpl> contextFactory;
    private final ModuleInfoSnapshotResolver resolver;

    private @GuardedBy("this") ComponentInstance<OSGiModuleInfoSnapshotImpl> currentInstance;
    private @GuardedBy("this") ModuleInfoSnapshot currentSnapshot;
    private @GuardedBy("this") int generation;

    private volatile boolean ignoreScanner = true;

    RegularYangModuleInfoRegistry(final ComponentFactory<OSGiModuleInfoSnapshotImpl> contextFactory,
            final YangParserFactory factory) {
        this.contextFactory = requireNonNull(contextFactory);
        resolver = new ModuleInfoSnapshotResolver("dom-schema-osgi", factory);
    }

    // Invocation from scanner, we may want to ignore this in order to not process partial updates
    @Override
    void scannerUpdate() {
        if (!ignoreScanner) {
            synchronized (this) {
                updateService();
            }
        }
    }

    @Override
    synchronized void scannerShutdown() {
        ignoreScanner = true;
    }

    @Override
    synchronized void enableScannerAndUpdate() {
        ignoreScanner = false;
        updateService();
    }

    @Override
    synchronized void close() {
        ignoreScanner = true;
        if (currentInstance != null) {
            currentInstance.dispose();
            currentInstance = null;
        }
    }

    @Override
    Registration registerBundle(final List<YangModuleInfo> moduleInfos,
            final List<YangFeatureProvider<?>> featureProviders) {
        final var infoRegs = resolver.registerModuleInfos(moduleInfos);
        final var featureRegs = featureProviders.stream().map(this::register).collect(Collectors.toList());

        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                featureRegs.forEach(Registration::close);
                infoRegs.forEach(Registration::close);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <R extends @NonNull DataRoot<R>> Registration register(final YangFeatureProvider<?> provider) {
        @SuppressWarnings("rawtypes")
        final YangFeatureProvider cast = provider;
        return resolver.registerModuleFeatures(cast.boundModule(), cast.supportedFeatures());
    }

    @Holding("this")
    private void updateService() {
        final ModuleInfoSnapshot newSnapshot;
        try {
            newSnapshot = resolver.takeSnapshot();
        } catch (NoSuchElementException e) {
            LOG.debug("No snapshot available", e);
            return;
        }
        if (newSnapshot.equals(currentSnapshot)) {
            LOG.debug("No update to snapshot");
            return;
        }

        final ComponentInstance<OSGiModuleInfoSnapshotImpl> newInstance = contextFactory.newInstance(
            OSGiModuleInfoSnapshotImpl.props(nextGeneration(), newSnapshot));
        if (currentInstance != null) {
            currentInstance.dispose();
        }
        currentInstance = newInstance;
        currentSnapshot = newSnapshot;
    }

    @Holding("this")
    private long nextGeneration() {
        return generation == -1 ? -1 : ++generation;
    }
}
