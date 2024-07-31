/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl.di;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeFactory;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

/**
 * Default implementation of {@link BindingCodecTreeFactory}.
 */
@Singleton
@Deprecated(since = "14.0.2", forRemoval = true)
public final class DefaultBindingCodecTreeFactory implements BindingCodecTreeFactory {
    @Inject
    public DefaultBindingCodecTreeFactory() {
        // Exposed for DI
    }

    @Override
    public BindingCodecTree create(final BindingRuntimeContext context) {
        return new BindingCodecContext(context);
    }
}
