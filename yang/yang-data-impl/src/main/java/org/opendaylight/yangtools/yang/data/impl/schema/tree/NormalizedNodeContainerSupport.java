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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;

final class NormalizedNodeContainerSupport<K extends PathArgument, T extends NormalizedNode<K, ?>> {
    final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder;
    final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder;
    final ChildTrackingPolicy childPolicy;
    final Class<T> requiredClass;

    NormalizedNodeContainerSupport(final Class<T> requiredClass, final ChildTrackingPolicy childPolicy,
            final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder) {
        this.requiredClass = requireNonNull(requiredClass);
        this.childPolicy = requireNonNull(childPolicy);
        this.copyBuilder = requireNonNull(copyBuilder);
        this.emptyBuilder = requireNonNull(emptyBuilder);
    }

    NormalizedNodeContainerSupport(final Class<T> requiredClass,
            final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder) {
        this(requiredClass, ChildTrackingPolicy.UNORDERED, copyBuilder, emptyBuilder);
    }

    NormalizedNodeContainerBuilder<?, ?, ?, T> createBuilder(final NormalizedNode<?, ?> original) {
        return copyBuilder.apply(cast(original));
    }

    NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        return emptyBuilder.get().withNodeIdentifier(cast(original).getIdentifier()).build();
    }

    private T cast(final NormalizedNode<?, ?> original) {
        checkArgument(requiredClass.isInstance(original), "Require %s, got %s", requiredClass, original);
        return requiredClass.cast(original);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("requiredClass", requiredClass).toString();
    }
}
