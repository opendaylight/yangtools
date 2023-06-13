/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

final class AugmentationCodecPrototype extends DataContainerCodecPrototype<AugmentRuntimeType> {
    private final @NonNull ImmutableSet<NodeIdentifier> childArgs;

    @SuppressWarnings("unchecked")
    AugmentationCodecPrototype(final Class<?> cls, final QNameModule namespace, final AugmentRuntimeType type,
            final CodecContextFactory factory, final ImmutableSet<NodeIdentifier> childArgs) {
        super(Item.of((Class<? extends DataObject>) cls), namespace, type, factory);
        this.childArgs = requireNonNull(childArgs);
    }

    @Override
    NodeIdentifier getYangArg() {
        throw new UnsupportedOperationException("Augmentation does not have PathArgument address");
    }

    @Override
    AugmentationNodeContext<?> createInstance() {
        return new AugmentationNodeContext<>(this);
    }

    // Guaranteed to be non-empty
    @NonNull ImmutableSet<NodeIdentifier> getChildArgs() {
        return childArgs;
    }
}