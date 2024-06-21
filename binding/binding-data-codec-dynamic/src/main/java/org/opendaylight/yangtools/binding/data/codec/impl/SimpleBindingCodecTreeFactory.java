/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices
@Component(immediate = true)
public final class SimpleBindingCodecTreeFactory implements BindingCodecTreeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleBindingCodecTreeFactory.class);

    @Override
    public BindingCodecTree create(final BindingRuntimeContext context) {
        return new BindingCodecContext(context);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Binding-DOM Codec enabled");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Binding-DOM Codec disabled");
    }
}
