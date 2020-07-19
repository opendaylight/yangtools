/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.osgi.OSGiBindingRuntimeContext;
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

@Component(immediate = true)
public final class OSGiBindingDOMCodec {
    // TODO: can we get rid of this complexity?
    private abstract static class AbstractInstances {

        abstract void add(OSGiBindingRuntimeContext runtimeContext);

        abstract void remove(OSGiBindingRuntimeContext runtimeContext);

        abstract @NonNull AbstractInstances toActive(BindingDOMCodecFactory codecFactory, ComponentFactory factory);

        abstract @NonNull AbstractInstances toInactive();
    }

    private static final class InactiveInstances extends AbstractInstances {
        private final Set<OSGiBindingRuntimeContext> instances = Collections.newSetFromMap(new IdentityHashMap<>());

        InactiveInstances() {

        }

        InactiveInstances(final Set<OSGiBindingRuntimeContext> keySet) {
            instances.addAll(keySet);
        }

        @Override
        void add(final OSGiBindingRuntimeContext runtimeContext) {
            verify(instances.add(runtimeContext), "Duplicate instance %s?!", runtimeContext);
        }

        @Override
        void remove(final OSGiBindingRuntimeContext runtimeContext) {
            instances.remove(runtimeContext);
        }

        @Override
        AbstractInstances toActive(final BindingDOMCodecFactory codecFactory, final ComponentFactory factory) {
            final ActiveInstances active = new ActiveInstances(codecFactory, factory);
            instances.stream()
                .sorted(Comparator.comparing(OSGiBindingRuntimeContext::getGeneration).reversed())
                .forEach(active::add);
            return active;
        }

        @Override
        AbstractInstances toInactive() {
            throw new IllegalStateException("Attempted to deactivate inactive instances");
        }
    }

    private static final class ActiveInstances extends AbstractInstances {
        private final Map<OSGiBindingRuntimeContext, ComponentInstance> instances = new IdentityHashMap<>();
        private final BindingDOMCodecFactory codecFactory;
        private final ComponentFactory factory;

        ActiveInstances(final BindingDOMCodecFactory codecFactory, final ComponentFactory factory) {
            this.codecFactory = requireNonNull(codecFactory);
            this.factory = requireNonNull(factory);
        }

        @Override
        void add(final OSGiBindingRuntimeContext runtimeContext) {
            final BindingRuntimeContext context = runtimeContext.getService();

            instances.put(runtimeContext, factory.newInstance(OSGiBindingDOMCodecServicesImpl.props(
                runtimeContext.getGeneration(), runtimeContext.getServiceRanking(),
                codecFactory.createBindingDOMCodec(context))));
        }

        @Override
        void remove(final OSGiBindingRuntimeContext runtimeContext) {
            final ComponentInstance instance = instances.remove(runtimeContext);
            if (instance != null) {
                instance.dispose();
            } else {
                LOG.warn("Instance for generation {} not found", runtimeContext.getGeneration());
            }
        }

        @Override
        AbstractInstances toActive(final BindingDOMCodecFactory ignoreCodecFactory,
                final ComponentFactory ignoreFactory) {
            throw new IllegalStateException("Attempted to activate active instances");
        }

        @Override
        AbstractInstances toInactive() {
            instances.values().forEach(ComponentInstance::dispose);
            return new InactiveInstances(instances.keySet());
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingDOMCodec.class);

    @Reference
    BindingDOMCodecFactory codecFactory = null;

    @Reference(target = "(component.factory=" + OSGiBindingDOMCodecServicesImpl.FACTORY_NAME + ")")
    ComponentFactory contextFactory = null;

    @GuardedBy("this")
    private AbstractInstances instances = new InactiveInstances();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addModuleInfoSnapshot(final OSGiBindingRuntimeContext runtimeContext) {
        instances.add(runtimeContext);
    }

    synchronized void removeModuleInfoSnapshot(final OSGiBindingRuntimeContext runtimeContext) {
        instances.remove(runtimeContext);
    }

    @Activate
    synchronized void activate() {
        LOG.info("Binding/DOM Codec activating");
        instances = instances.toActive(codecFactory, contextFactory);
        LOG.info("Binding/DOM Codec activated");
    }

    @Deactivate
    synchronized void deactivate() {
        LOG.info("Binding/DOM Codec deactivating");
        instances = instances.toInactive();
        LOG.info("Binding/DOM Codec deactivated");
    }
}
