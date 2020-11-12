/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.ItemOrder.Unordered;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.LeafsetEntryInterner;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

@NonNullByDefault
final class InterningLeafSetNodeBuilder<T> extends ImmutableLeafSetNodeBuilder<T> {
    private final LeafsetEntryInterner interner;

    private InterningLeafSetNodeBuilder(final LeafsetEntryInterner interner) {
        this.interner = requireNonNull(interner);
    }

    private InterningLeafSetNodeBuilder(final LeafsetEntryInterner interner, final int sizeHint) {
        super(sizeHint);
        this.interner = requireNonNull(interner);
    }

    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Non-grok of type annotations")
    private static @Nullable LeafsetEntryInterner getInterner(final @Nullable DataSchemaNode schema) {
        return schema instanceof LeafListSchemaNode ? LeafsetEntryInterner.forSchema((LeafListSchemaNode) schema)
                : null;
    }

    static <T> ListNodeBuilder<Unordered, T, LeafSetEntryNode<T>> create(final @Nullable DataSchemaNode schema) {
        final LeafsetEntryInterner interner = getInterner(schema);
        if (interner != null) {
            return new InterningLeafSetNodeBuilder<>(interner);
        }

        return ImmutableLeafSetNodeBuilder.create();
    }

    static <T> ListNodeBuilder<Unordered, T, LeafSetEntryNode<T>> create(final @Nullable DataSchemaNode schema,
            final int sizeHint) {
        final LeafsetEntryInterner interner = getInterner(schema);
        if (interner != null) {
            return new InterningLeafSetNodeBuilder<>(interner, sizeHint);
        }

        return ImmutableLeafSetNodeBuilder.create(sizeHint);
    }

    @Override
    public ImmutableLeafSetNodeBuilder<T> withChild(final LeafSetEntryNode<T> child) {
        return super.withChild(interner.intern(child));
    }
}
