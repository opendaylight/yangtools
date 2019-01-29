/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;

abstract class NormalizedNodeContainerSupport<K extends PathArgument, T extends NormalizedNode<K, ?>> {
    static final class Single<K extends PathArgument, T extends NormalizedNode<K, ?>>
            extends NormalizedNodeContainerSupport<K, T> {
        Single(final Class<T> requiredClass,
                final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder,
                final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder) {
            super(requiredClass, copyBuilder, emptyBuilder);
        }

        @Override
        NormalizedNodeContainerBuilder<?, ?, ?, T> createBuilder(final NormalizedNode<?, ?> original) {
            return copyBuilder.apply(cast(original));
        }

        @Override
        NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
            return emptyBuilder.get().withNodeIdentifier(cast(original).getIdentifier()).build();
        }

        private T cast(final NormalizedNode<?, ?> original) {
            checkArgument(requiredClass.isInstance(original), "Require %s, got %s", requiredClass, original);
            return requiredClass.cast(original);
        }
    }

    // FIXME: MapEntry a refactor of a hack, originally introduced in
    //        Change-Id: I9dc02a1917f38e8a0d62279843974b9869c48693. DataTreeRoot needs to be fixed up to properly
    //        handle the lookup of through maps.
    static final class MapEntry<T extends NormalizedNode<NodeIdentifier, ?>>
            extends NormalizedNodeContainerSupport<NodeIdentifier, T> {
        MapEntry(final Class<T> requiredClass,
                final Function<T, NormalizedNodeContainerBuilder<NodeIdentifier, ?, ?, T>> copyBuilder,
                final Supplier<NormalizedNodeContainerBuilder<NodeIdentifier, ?, ?, T>> emptyBuilder) {
            super(requiredClass, copyBuilder, emptyBuilder);
        }

        @Override
        NormalizedNodeContainerBuilder<?, ?, ?, ?> createBuilder(final NormalizedNode<?, ?> original) {
            if (requiredClass.isInstance(original)) {
                return copyBuilder.apply(requiredClass.cast(original));
            }
            if (original instanceof MapEntryNode) {
                return ImmutableMapEntryNodeBuilder.create((MapEntryNode) original);
            }
            throw new IllegalArgumentException("Expected either MapEntryNode or " + requiredClass + ", offending node: "
                    + original);
        }

        @Override
        NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
            if (requiredClass.isInstance(original)) {
                return emptyBuilder.get().withNodeIdentifier(requiredClass.cast(original).getIdentifier()).build();
            }
            if (original instanceof MapEntryNode) {
                return ImmutableMapEntryNodeBuilder.create()
                        .withNodeIdentifier(((MapEntryNode) original).getIdentifier()).build();
            }
            throw new IllegalArgumentException("Expected either MapEntryNode or " + requiredClass + ", offending node: "
                    + original);
        }
    }

    final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder;
    final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder;
    final Class<T> requiredClass;

    NormalizedNodeContainerSupport(final Class<T> requiredClass,
            final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder) {
        this.requiredClass = requireNonNull(requiredClass);
        this.copyBuilder = requireNonNull(copyBuilder);
        this.emptyBuilder = requireNonNull(emptyBuilder);
    }

    @SuppressWarnings("rawtypes")
    abstract NormalizedNodeContainerBuilder createBuilder(NormalizedNode<?, ?> original);

    abstract NormalizedNode<?, ?> createEmptyValue(NormalizedNode<?, ?> original);

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("requiredClass", requiredClass).toString();
    }
}
