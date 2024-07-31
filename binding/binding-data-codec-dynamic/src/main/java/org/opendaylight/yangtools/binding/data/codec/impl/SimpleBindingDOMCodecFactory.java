/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.dynamic.BindingDataCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@MetaInfServices(value = { BindingDataCodecFactory.class, BindingDOMCodecFactory.class })
@Component(immediate = true, service = { BindingDataCodecFactory.class, BindingDOMCodecFactory.class })
public final class SimpleBindingDOMCodecFactory implements BindingDataCodecFactory, BindingDOMCodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleBindingDOMCodecFactory.class);

    @Override
    public BindingDataCodec newBindingDataCodec(final BindingRuntimeContext runtimeContext) {
        return new BindingCodecContext(runtimeContext);
    }

    @Override
    @Deprecated(since = "14.0.2", forRemoval = true)
    public BindingDOMCodecServices createBindingDOMCodec(final BindingRuntimeContext context) {
        return new BindingCodecContext(context);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Binding/DOM Codec enabled");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Binding/DOM Codec disabled");
    }
}
