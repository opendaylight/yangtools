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
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;

/**
 * Default implementation of {@link BindingCodecTreeFactory}.
 */
@Beta
@Singleton
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
