/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.dom.codec.osgi.OSGiBindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.ForwardingBindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.LazyActionInputContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.spi.LazyActionOutputContainerNode;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
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
               BindingDOMCodecServices.class,
               BindingNormalizedNodeWriterFactory.class,
               BindingNormalizedNodeSerializer.class,
               BindingCodecTree.class
           })
public final class GlobalBindingDOMCodecServices extends ForwardingBindingDOMCodecServices {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalBindingDOMCodecServices.class);

    private BindingDOMCodecServices delegate;
    private UnsignedLong generation;

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
        generation = services.getGeneration();
        delegate = services.getService();
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
    protected BindingDOMCodecServices delegate() {
        return verifyNotNull(delegate);
    }
}
