/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.osgi.impl;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import org.opendaylight.yangtools.binding.data.codec.osgi.OSGiBindingDOMCodecServices;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.runtime.osgi.OSGiBindingRuntimeContext;
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
    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingDOMCodec.class);

    private final IdentityHashMap<OSGiBindingRuntimeContext, ComponentInstance<OSGiBindingDOMCodecServices>> instances =
        new IdentityHashMap<>();
    private final ComponentFactory<OSGiBindingDOMCodecServices> contextFactory;
    private final BindingDOMCodecFactory codecFactory;

    @Activate
    public OSGiBindingDOMCodec(@Reference final BindingDOMCodecFactory codecFactory,
            @Reference(target = "(component.factory=" + OSGiBindingDOMCodecServicesImpl.FACTORY_NAME + ")")
            ComponentFactory<OSGiBindingDOMCodecServices> contextFactory) {
        this.codecFactory = requireNonNull(codecFactory);
        this.contextFactory = requireNonNull(contextFactory);
        LOG.info("Binding/DOM Codec activated");
    }

    @Deactivate
    synchronized void deactivate() {
        LOG.info("Binding/DOM Codec deactivating");
        instances.forEach((ctx, instance) -> instance.dispose());
        instances.clear();
        LOG.info("Binding/DOM Codec deactivated");
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addRuntimeContext(final OSGiBindingRuntimeContext runtimeContext) {
        final var context = runtimeContext.service();

        instances.put(runtimeContext, contextFactory.newInstance(OSGiBindingDOMCodecServicesImpl.props(
            runtimeContext.generation(), runtimeContext.getServiceRanking(),
            codecFactory.createBindingDOMCodec(context))));
    }

    synchronized void removeRuntimeContext(final OSGiBindingRuntimeContext runtimeContext) {
        final var instance = instances.remove(runtimeContext);
        if (instance != null) {
            instance.dispose();
        } else {
            LOG.warn("Instance for generation {} not found", runtimeContext.generation());
        }
    }
}
