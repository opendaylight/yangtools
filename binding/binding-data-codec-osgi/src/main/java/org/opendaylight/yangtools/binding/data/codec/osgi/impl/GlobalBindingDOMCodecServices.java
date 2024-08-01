/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingLazyContainerNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.yangtools.binding.data.codec.dynamic.DynamicBindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.osgi.OSGiBindingDOMCodecServices;
import org.opendaylight.yangtools.binding.data.codec.spi.LazyActionInputContainerNode;
import org.opendaylight.yangtools.binding.data.codec.spi.LazyActionOutputContainerNode;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A global {@link BindingDOMCodecServices}. It is injected with latest {@link OSGiBindingDOMCodecServices} generation.
 */
@Beta
@Component(immediate = true,
           service = {
               // primary services
               BindingDataCodec.class,
               DynamicBindingDataCodec.class,
               // direct access to components
               BindingCodecTree.class,
               BindingNormalizedNodeSerializer.class,
               BindingNormalizedNodeWriterFactory.class,
           })
public final class GlobalBindingDOMCodecServices implements DynamicBindingDataCodec, BindingCodecTree,
        BindingNormalizedNodeSerializer, BindingNormalizedNodeWriterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalBindingDOMCodecServices.class);

    private DynamicBindingDataCodec delegate;
    private Uint64 generation;

    @Activate
    public GlobalBindingDOMCodecServices(@Reference(updated = "update") final OSGiBindingDOMCodecServices services) {
        updateDelegate(services);
        LOG.info("Global Binding/DOM Codec activated with generation {}", generation);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Global Binding/DOM Codec deactivated");
    }

    void update(final OSGiBindingDOMCodecServices services) {
        updateDelegate(services);
        LOG.info("Global Binding/DOM Codec updated to generation {}", generation);
    }

    private void updateDelegate(final OSGiBindingDOMCodecServices services) {
        generation = services.generation();
        delegate = services.service();
    }

    @Override
    public BindingRuntimeContext runtimeContext() {
        return delegate().runtimeContext();
    }

    @Override
    public BindingNormalizedNodeSerializer nodeSerializer() {
        return delegate().nodeSerializer();
    }

    @Override
    public BindingCodecTree tree() {
        return delegate().tree();
    }

    @Override
    public BindingNormalizedNodeWriterFactory writerFactory() {
        return delegate().writerFactory();
    }

    @Override
    public BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcInput input) {
        return new LazyActionInputContainerNode(identifier, input, this, action);
    }

    @Override
    public BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcOutput output) {
        return new LazyActionOutputContainerNode(identifier, output, this, action);
    }

    @Override
    protected DynamicBindingDataCodec delegate() {
        return verifyNotNull(delegate);
    }
}
