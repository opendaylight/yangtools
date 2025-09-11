/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.IdentityHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.osgi.OSGiModuleInfoSnapshot;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
public final class OSGiBindingRuntime {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingRuntime.class);

    private final IdentityHashMap<OSGiModuleInfoSnapshot, ComponentInstance<OSGiBindingRuntimeContextImpl>> instances =
        new IdentityHashMap<>();
    private final @NonNull ComponentFactory<OSGiBindingRuntimeContextImpl> factory;
    private final @NonNull BindingRuntimeGenerator generator;

    @Activate
    public OSGiBindingRuntime(@Reference final BindingRuntimeGenerator generator,
            @Reference(target = "(component.factory=" + OSGiBindingRuntimeContextImpl.FACTORY_NAME + ")")
            final ComponentFactory<OSGiBindingRuntimeContextImpl> factory) {
        this.generator = requireNonNull(generator);
        this.factory = requireNonNull(factory);
        LOG.info("Binding Runtime activated");
    }

    @Deactivate
    synchronized void deactivate() {
        LOG.info("Binding Runtime deactivating");
        instances.forEach((info, instance) -> instance.dispose());
        instances.clear();
        LOG.info("Binding Runtime deactivated");
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addModuleInfoSnapshot(final OSGiModuleInfoSnapshot snapshot) {
        final var infoSnapshot = snapshot.service();
        final var types = generator.generateTypeMapping(infoSnapshot.modelContext());
        instances.put(snapshot, factory.newInstance(OSGiBindingRuntimeContextImpl.props(snapshot.generation(),
            snapshot.getServiceRanking(), new DefaultBindingRuntimeContext(types, infoSnapshot))));
    }

    synchronized void removeModuleInfoSnapshot(final OSGiModuleInfoSnapshot snapshot) {
        final var instance = instances.remove(snapshot);
        if (instance != null) {
            instance.dispose();
        } else {
            LOG.warn("Instance for generation {} not found", snapshot.generation());
        }
    }
}
