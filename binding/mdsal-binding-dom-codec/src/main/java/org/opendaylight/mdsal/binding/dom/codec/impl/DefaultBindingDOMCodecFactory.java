/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.annotations.Beta;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@NonNullByDefault
@MetaInfServices
@Singleton
@Component(immediate = true)
public final class DefaultBindingDOMCodecFactory implements BindingDOMCodecFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBindingDOMCodecFactory.class);

    @Override
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
