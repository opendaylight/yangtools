/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class AugmentationNodeContext<D extends DataObject & Augmentation<?>>
        extends AbstractDataObjectCodecContext<D, AugmentRuntimeType> implements BindingAugmentationCodecTreeNode<D> {
    AugmentationNodeContext(final DataContainerCodecPrototype.Augmentation prototype) {
        super(prototype, new CodecDataObjectAnalysis<>(prototype, CodecItemFactory.of(), null));
    }

    @Override
    public PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        if (!bindingArg().equals(arg)) {
            throw new IllegalArgumentException("Unexpected argument " + arg);
        }
        return null;
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final PathArgument arg) {
        if (arg != null) {
            throw new IllegalArgumentException("Unexpected argument " + arg);
        }
        return bindingArg();
    }

    @Override
    public D filterFrom(final DataContainerNode parentData) {
        for (var childArg : ((DataContainerCodecPrototype.Augmentation) prototype).getChildArgs()) {
            if (parentData.childByArg(childArg) != null) {
                return createProxy(parentData);
            }
        }
        return null;
    }

    private @NonNull D createProxy(final @NonNull DataContainerNode parentData) {
        return createBindingProxy(parentData);
    }

    @Override
    public ImmutableSet<NodeIdentifier> childPathArguments() {
        return byYangKeySet();
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return filterFrom(checkDataArgument(DataContainerNode.class, normalizedNode));
    }

    @Override
    protected NodeIdentifier getDomPathArgument() {
        return null;
    }

    @Override
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
            final DistinctNodeContainer<PathArgument, NormalizedNode> data) {
        return Map.of();
    }
}
