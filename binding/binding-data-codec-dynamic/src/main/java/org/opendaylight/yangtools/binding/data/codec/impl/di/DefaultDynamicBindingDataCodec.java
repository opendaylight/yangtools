/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl.di;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.yangtools.binding.data.codec.dynamic.DynamicBindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.dynamic.ri.dagger.ReferenceDynamicBindingDataCodecModule;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

/**
 * Default implementation of {@link DynamicBindingDataCodec}.
 *
 * @deprecated Use {@link ReferenceDynamicBindingDataCodecModule#provideDynamicBindingDataCodec(BindingRuntimeContext)}
 *             instead.
 */
@Beta
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
public final class DefaultDynamicBindingDataCodec extends ForwardingObject implements DynamicBindingDataCodec {
    private final DynamicBindingDataCodec delegate;

    @Inject
    public DefaultDynamicBindingDataCodec(final BindingRuntimeContext context) {
        delegate = ReferenceDynamicBindingDataCodecModule.provideDynamicBindingDataCodec(context);
    }

    @Override
    public BindingNormalizedNodeSerializer nodeSerializer() {
        return delegate.nodeSerializer();
    }

    @Override
    public BindingCodecTree tree() {
        return delegate.tree();
    }

    @Override
    public BindingNormalizedNodeWriterFactory writerFactory() {
        return delegate.writerFactory();
    }

    @Override
    public BindingRuntimeContext runtimeContext() {
        return delegate.runtimeContext();
    }

    @Override
    protected BindingDataCodec delegate() {
        return delegate;
    }
}
