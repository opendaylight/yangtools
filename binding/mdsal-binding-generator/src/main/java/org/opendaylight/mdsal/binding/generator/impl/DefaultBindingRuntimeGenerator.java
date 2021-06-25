/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link BindingRuntimeGenerator}.
 */
@Beta
@MetaInfServices
@Singleton
@Component(immediate = true)
public final class DefaultBindingRuntimeGenerator implements BindingRuntimeGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBindingRuntimeGenerator.class);

    @Inject
    public DefaultBindingRuntimeGenerator() {
        // exposed for DI
    }

    @Override
    public BindingRuntimeTypes generateTypeMapping(final EffectiveModelContext context) {
        return BindingRuntimeTypesFactory.createTypes(context);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Binding/YANG type support activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Binding/YANG type support deactivated");
    }
}
