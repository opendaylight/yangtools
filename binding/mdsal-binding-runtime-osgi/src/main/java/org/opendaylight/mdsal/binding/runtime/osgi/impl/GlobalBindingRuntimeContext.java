/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.osgi.OSGiBindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.ForwardingBindingRuntimeContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A global {@link BindingRuntimeContext}. It is injected with latest {@link OSGiBindingRuntimeContext} generation.
 */
@Beta
@Component(service = BindingRuntimeContext.class, immediate = true)
public final class GlobalBindingRuntimeContext extends ForwardingBindingRuntimeContext {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalBindingRuntimeContext.class);

    @Reference
    OSGiBindingRuntimeContext osgi = null;

    private BindingRuntimeContext delegate;
    private UnsignedLong generation;

    @Override
    protected BindingRuntimeContext delegate() {
        return verifyNotNull(delegate);
    }

    @Activate
    void activate() {
        generation = osgi.getGeneration();
        delegate = osgi.getService();
        LOG.info("Global BindingRuntimeContext generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Global BindingRuntimeContext generation {} deactivated", generation);
    }
}
