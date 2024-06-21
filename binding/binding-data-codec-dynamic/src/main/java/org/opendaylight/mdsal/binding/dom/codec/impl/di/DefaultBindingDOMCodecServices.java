/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl.di;

import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.binding.data.codec.spi.ForwardingBindingDOMCodecServices;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

/**
 * Default implementation of {@link BindingDOMCodecServices}.
 */
@Beta
@Singleton
public final class DefaultBindingDOMCodecServices extends ForwardingBindingDOMCodecServices {
    private final @NonNull BindingDOMCodecServices delegate;

    @Inject
    public DefaultBindingDOMCodecServices(final BindingRuntimeContext context) {
        delegate = new BindingCodecContext(context);
    }

    @Override
    protected BindingDOMCodecServices delegate() {
        return delegate;
    }
}
