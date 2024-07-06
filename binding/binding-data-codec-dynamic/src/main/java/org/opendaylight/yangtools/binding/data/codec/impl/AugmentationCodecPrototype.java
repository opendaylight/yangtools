/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

final class AugmentationCodecPrototype<T extends Augmentation<?>>
        extends CommonDataObjectCodecPrototype<AugmentRuntimeType> {
    // Note: all NodeIdentifiers are expected to have the same QNameModule
    private final @NonNull ImmutableSet<NodeIdentifier> childArgs;

    AugmentationCodecPrototype(final @NonNull Class<T> cls, final AugmentRuntimeType type,
            final CodecContextFactory factory, final ImmutableSet<NodeIdentifier> childArgs) {
        super(new NodeStep<>(cls), type, factory);
        // Note: caller guarantees non-empty, and all substatements have the same namespace
        this.childArgs = requireNonNull(childArgs);
    }

    @Override
    NodeIdentifier yangArg() {
        throw new UnsupportedOperationException("Augmentation does not have PathArgument address");
    }

    @Override
    NodeIdentifier bindIdentifier(final Unqualified identifier) {
        final var first = childArgs.iterator().next();
        final var pathArg = new NodeIdentifier(identifier.bindTo(first.getNodeType().getModule()));
        if (childArgs.contains(pathArg)) {
            return pathArg;
        }
        throw new IllegalArgumentException(identifier + " does not match any of " + childArgs);
    }

    @Override
    AugmentationCodecContext<?> createInstance() {
        return new AugmentationCodecContext<>(this);
    }

    // Guaranteed to be non-empty
    @NonNull ImmutableSet<NodeIdentifier> getChildArgs() {
        return childArgs;
    }
}
