/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.spi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.lib.Action;
import org.opendaylight.yangtools.binding.lib.RpcInput;
import org.opendaylight.yangtools.binding.lib.RpcOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

@Beta
public abstract class AbstractBindingNormalizedNodeSerializer implements BindingNormalizedNodeSerializer {
    @Override
    public final BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcInput input) {
        return new LazyActionInputContainerNode(identifier, input, this, action);
    }

    @Override
    public final BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final RpcInput input) {
        return toLazyNormalizedNodeActionInput(action, actionInputName(action), input);
    }

    protected abstract @NonNull NodeIdentifier actionInputName(@NonNull Class<? extends Action<?, ?, ?>> action);

    @Override
    public final BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcOutput output) {
        return new LazyActionOutputContainerNode(identifier, output, this, action);
    }

    @Override
    public final BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final RpcOutput output) {
        return toLazyNormalizedNodeActionOutput(action, actionOutputName(action), output);
    }

    protected abstract @NonNull NodeIdentifier actionOutputName(@NonNull Class<? extends Action<?, ?, ?>> action);
}
