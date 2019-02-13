/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

abstract class NormalizedNodeContainerSupport<K extends PathArgument, T extends NormalizedNode<K, ?>> {
    static final class Augmentation extends NormalizedNodeContainerSupport<AugmentationIdentifier, AugmentationNode> {
        Augmentation() {
            super(AugmentationNode.class, ImmutableAugmentationNodeBuilder::create,
                ImmutableAugmentationNodeBuilder::create);
        }

        @Override
        AugmentationNode defaultEmptyValue(final WithStatus schemaNode) {
            verify(schemaNode instanceof AugmentationSchemaNode, "Unexpected schema %s", schemaNode);
            final AugmentationIdentifier identifier = DataSchemaContextNode.augmentationIdentifierFrom(
                (AugmentationSchemaNode) schemaNode);
            return ImmutableAugmentationNodeBuilder.create().withNodeIdentifier(identifier).build();
        }
    }

    static final class Automatic<T extends NormalizedNode<NodeIdentifier, ?>, S extends SchemaNode>
            extends NormalizedNodeContainerSupport<NodeIdentifier, T> {

        private final Class<S> schemaClass;

        Automatic(final Class<T> requiredClass, final Class<S> schemaClass,
            final Function<T, NormalizedNodeContainerBuilder<NodeIdentifier, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<NodeIdentifier, ?, ?, T>> emptyBuilder) {
            super(requiredClass, copyBuilder, emptyBuilder);
            this.schemaClass = requireNonNull(schemaClass);
        }

        Automatic(final Class<T> requiredClass, final Class<S> schemaClass, final ChildTrackingPolicy childPolicy,
            final Function<T, NormalizedNodeContainerBuilder<NodeIdentifier, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<NodeIdentifier, ?, ?, T>> emptyBuilder) {
            super(requiredClass, childPolicy, copyBuilder, emptyBuilder);
            this.schemaClass = requireNonNull(schemaClass);
        }

        @Override
        T defaultEmptyValue(final WithStatus schemaNode) {
            verify(schemaClass.isInstance(schemaNode), "Unexpected schema %s while expecting %s", schemaNode,
                schemaClass);
            final QName qname = schemaClass.cast(schemaNode).getQName();
            return emptyBuilder.get().withNodeIdentifier(NodeIdentifier.create(qname)).build();
        }
    }

    static final class Manual<K extends PathArgument, T extends NormalizedNode<K, ?>>
        extends NormalizedNodeContainerSupport<K, T> {

        Manual(final Class<T> requiredClass, final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder) {
            super(requiredClass, copyBuilder, emptyBuilder);
        }

        Manual(final Class<T> requiredClass, final ChildTrackingPolicy childPolicy,
            final Function<T, NormalizedNodeContainerBuilder<K, ?, ?, T>> copyBuilder,
            final Supplier<NormalizedNodeContainerBuilder<K, ?, ?, T>> emptyBuilder) {
            super(requiredClass, childPolicy, copyBuilder, emptyBuilder);
        }

        @Override
        T defaultEmptyValue(final WithStatus schemaNode) {
            return null;
        }
    }

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

    final NormalizedNodeContainerBuilder<?, ?, ?, T> createBuilder(final NormalizedNode<?, ?> original) {
        return copyBuilder.apply(cast(original));
    }

    final T createEmptyValue(final NormalizedNode<?, ?> original) {
        return emptyBuilder.get().withNodeIdentifier(cast(original).getIdentifier()).build();
    }

    abstract @Nullable T defaultEmptyValue(WithStatus schemaNode);

    private T cast(final NormalizedNode<?, ?> original) {
        checkArgument(requiredClass.isInstance(original), "Require %s, got %s", requiredClass, original);
        return requiredClass.cast(original);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("requiredClass", requiredClass).toString();
    }
}
