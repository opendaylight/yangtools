/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.spi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Beta
@NonNullByDefault
public final class LazyActionOutputContainerNode extends AbstractLazyActionContainerNode<RpcOutput> {
    public LazyActionOutputContainerNode(final NodeIdentifier identifier, final RpcOutput bindingData,
            final BindingNormalizedNodeSerializer codec, final Class<? extends Action<?, ?, ?>> action) {
        super(identifier, bindingData, codec, action);
    }

    @Override
    protected ContainerNode computeContainerNode(final BindingNormalizedNodeSerializer context) {
        return context.toNormalizedNodeActionOutput(action, getDataObject());
    }
}