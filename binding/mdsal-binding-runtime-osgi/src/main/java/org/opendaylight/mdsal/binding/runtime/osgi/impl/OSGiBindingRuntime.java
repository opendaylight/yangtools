/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiModuleInfoSnapshot;
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
    // TODO: can we get rid of this complexity?
    private abstract static class AbstractInstances {

        abstract void add(OSGiModuleInfoSnapshot snapshot);

        abstract void remove(OSGiModuleInfoSnapshot snapshot);

        abstract @NonNull AbstractInstances toActive(BindingRuntimeGenerator generator,
            ComponentFactory<OSGiBindingRuntimeContextImpl> factory);

        abstract @NonNull AbstractInstances toInactive();
    }

    private static final class InactiveInstances extends AbstractInstances {
        private final Set<OSGiModuleInfoSnapshot> instances = Collections.newSetFromMap(new IdentityHashMap<>());

        InactiveInstances() {

        }

        InactiveInstances(final Set<OSGiModuleInfoSnapshot> keySet) {
            instances.addAll(keySet);
        }

        @Override
        void add(final OSGiModuleInfoSnapshot snapshot) {
            verify(instances.add(snapshot), "Duplicate instance %s?!", snapshot);
        }

        @Override
        void remove(final OSGiModuleInfoSnapshot snapshot) {
            instances.remove(snapshot);
        }

        @Override
        AbstractInstances toActive(final BindingRuntimeGenerator generator,
                final ComponentFactory<OSGiBindingRuntimeContextImpl> factory) {
            final ActiveInstances active = new ActiveInstances(generator, factory);
            instances.stream()
                .sorted(Comparator.comparing(OSGiModuleInfoSnapshot::getGeneration).reversed())
                .forEach(active::add);
            return active;
        }

        @Override
        AbstractInstances toInactive() {
            throw new IllegalStateException("Attempted to deactivate inactive instances");
        }
    }

    private static final class ActiveInstances extends AbstractInstances {
        private final Map<OSGiModuleInfoSnapshot, ComponentInstance<OSGiBindingRuntimeContextImpl>> instances =
            new IdentityHashMap<>();
        private final BindingRuntimeGenerator generator;
        private final ComponentFactory<OSGiBindingRuntimeContextImpl> factory;

        ActiveInstances(final BindingRuntimeGenerator generator,
                final ComponentFactory<OSGiBindingRuntimeContextImpl> factory) {
            this.generator = requireNonNull(generator);
            this.factory = requireNonNull(factory);
        }

        @Override
        void add(final OSGiModuleInfoSnapshot snapshot) {
            final var infoSnapshot = snapshot.getService();
            final var types = generator.generateTypeMapping(infoSnapshot.modelContext());

            instances.put(snapshot, factory.newInstance(OSGiBindingRuntimeContextImpl.props(
                snapshot.getGeneration(), snapshot.getServiceRanking(),
                new DefaultBindingRuntimeContext(types, infoSnapshot))));
        }

        @Override
        void remove(final OSGiModuleInfoSnapshot snapshot) {
            final var instance = instances.remove(snapshot);
            if (instance != null) {
                instance.dispose();
            } else {
                LOG.warn("Instance for generation {} not found", snapshot.getGeneration());
            }
        }

        @Override
        AbstractInstances toActive(final BindingRuntimeGenerator ignoreGenerator,
                final ComponentFactory<OSGiBindingRuntimeContextImpl> ignoreFactory) {
            throw new IllegalStateException("Attempted to activate active instances");
        }

        @Override
        AbstractInstances toInactive() {
            instances.values().forEach(ComponentInstance::dispose);
            return new InactiveInstances(instances.keySet());
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingRuntime.class);

    @Reference
    BindingRuntimeGenerator generator = null;

    @Reference(target = "(component.factory=" + OSGiBindingRuntimeContextImpl.FACTORY_NAME + ")")
    ComponentFactory<OSGiBindingRuntimeContextImpl> contextFactory = null;

    @GuardedBy("this")
    private AbstractInstances instances = new InactiveInstances();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addModuleInfoSnapshot(final OSGiModuleInfoSnapshot snapshot) {
        instances.add(snapshot);
    }

    synchronized void removeModuleInfoSnapshot(final OSGiModuleInfoSnapshot snapshot) {
        instances.remove(snapshot);
    }

    @Activate
    synchronized void activate() {
        LOG.info("Binding Runtime activating");
        instances = instances.toActive(generator, contextFactory);
        LOG.info("Binding Runtime activated");
    }

    @Deactivate
    synchronized void deactivate() {
        LOG.info("Binding Runtime deactivating");
        instances = instances.toInactive();
        LOG.info("Binding Runtime deactivated");
    }
}
