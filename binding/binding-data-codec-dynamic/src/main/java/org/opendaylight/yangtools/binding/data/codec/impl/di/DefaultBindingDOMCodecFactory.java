/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl.di;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

/**
 * Default implementation of {@link BindingDOMCodecFactory}.
 */
@Beta
@Singleton
public final class DefaultBindingDOMCodecFactory implements BindingDOMCodecFactory {
    @Inject
    public DefaultBindingDOMCodecFactory() {
        // Exposed for DI
    }

    @Override
    public BindingDOMCodecServices createBindingDOMCodec(final BindingRuntimeContext context) {
        return new BindingCodecContext(context);
    }
}
