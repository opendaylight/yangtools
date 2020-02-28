/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import com.google.common.primitives.UnsignedLong;
import org.opendaylight.binding.runtime.api.AbstractBindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.dom.schema.osgi.OSGiModuleInfoSnapshot;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Factory Component which implements {@link BindingRuntimeContext}.
 */
@Component(service = BindingRuntimeContext.class, immediate = true)
public final class BindingRuntimeContextImpl extends AbstractBindingRuntimeContext {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeContextImpl.class);

    @Reference
    OSGiModuleInfoSnapshot effectiveModel = null;
    @Reference
    BindingRuntimeGenerator generator = null;

    private BindingRuntimeContext delegate;
    private UnsignedLong generation;

    @Override
    public ClassLoadingStrategy getStrategy() {
        return delegate.getStrategy();
    }

    @Override
    public BindingRuntimeTypes getTypes() {
        return delegate.getTypes();
    }

    @Activate
    void activate() {
        generation = effectiveModel.getGeneration();
        delegate = DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(effectiveModel.getEffectiveModelContext()), effectiveModel);

        LOG.debug("BindingRuntimeContext generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.debug("BindingRuntimeContext generation {} deactivated", generation);
    }
}
