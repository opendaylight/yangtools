/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.util.Dictionary;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.osgi.OSGiBindingRuntimeContext;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Factory Component which implements {@link OSGiBindingRuntimeContext}.
 */
@Beta
@Component(factory = OSGiBindingRuntimeContextImpl.FACTORY_NAME, service = OSGiBindingRuntimeContext.class)
public final class OSGiBindingRuntimeContextImpl implements OSGiBindingRuntimeContext {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.runtime.osgi.impl.OSGiBindingRuntimeContextImpl";

    // Keys to for activation properties
    @VisibleForTesting
    static final String GENERATION = "org.opendaylight.mdsal.binding.runtime.osgi.impl.generation";
    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.binding.runtime.osgi.impl.BindingRuntimeContext";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingRuntimeContextImpl.class);

    private final BindingRuntimeContext delegate;
    private final Uint64 generation;

    @Activate
    public OSGiBindingRuntimeContextImpl(final Map<String, ?> properties) {
        generation = (Uint64) verifyNotNull(properties.get(GENERATION));
        delegate = (BindingRuntimeContext) verifyNotNull(properties.get(DELEGATE));
        LOG.info("BindingRuntimeContext generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        LOG.info("BindingRuntimeContext generation {} deactivated", generation);
    }

    @Override
    public Uint64 generation() {
        return generation;
    }

    @Override
    public BindingRuntimeContext service() {
        return delegate;
    }

    static Dictionary<String, ?> props(final @NonNull Uint64 generation, final @NonNull Integer ranking,
            final @NonNull BindingRuntimeContext delegate) {
        return FrameworkUtil.asDictionary(Map.of(
            Constants.SERVICE_RANKING, ranking,
            GENERATION, generation,
            DELEGATE, delegate));
    }
}
