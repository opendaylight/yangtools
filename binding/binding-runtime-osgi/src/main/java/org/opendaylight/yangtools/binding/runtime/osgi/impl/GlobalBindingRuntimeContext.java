/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.osgi.OSGiBindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.spi.ForwardingBindingRuntimeContext;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A global {@link BindingRuntimeContext}. It is injected with latest {@link OSGiBindingRuntimeContext} generation.
 */
@Component(service = BindingRuntimeContext.class, immediate = true)
public final class GlobalBindingRuntimeContext extends ForwardingBindingRuntimeContext {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalBindingRuntimeContext.class);

    private final Uint64 generation;
    private BindingRuntimeContext delegate;

    @Activate
    public GlobalBindingRuntimeContext(@Reference final OSGiBindingRuntimeContext osgi) {
        generation = osgi.generation();
        delegate = osgi.service();
        LOG.info("Global BindingRuntimeContext generation {} activated", generation);
    }

    @Override
    protected BindingRuntimeContext delegate() {
        return verifyNotNull(delegate);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Global BindingRuntimeContext generation {} deactivated", generation);
    }
}
