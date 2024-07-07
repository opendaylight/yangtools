/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Collections2;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * Abstract base class for {@link DataTreeCandidateNode} implementations.
 */
public abstract class AbstractDataTreeCandidateNode implements DataTreeCandidateNode {
    private final @NonNull ModificationType modificationType;

    protected AbstractDataTreeCandidateNode(final ModificationType modificationType) {
        this.modificationType = requireNonNull(modificationType);
    }

    @Override
    public final ModificationType modificationType() {
        return modificationType;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(DataTreeCandidateNode.class)
            .add("modificationType", modificationType))
            .toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    @SuppressWarnings("null")
    protected static final @NonNull Collection<DataTreeCandidateNode> childNodes(
            final DistinctNodeContainer<?, ?> container,
            final Function<NormalizedNode, DataTreeCandidateNode> function) {
        @SuppressWarnings("unchecked")
        final var body = (Collection<NormalizedNode>) container.body();
        return Collections2.transform(body, function);
    }

    protected static final @Nullable DataTreeCandidateNode modifiedChild(final DistinctNodeContainer<?, ?> container,
            final Function<NormalizedNode, DataTreeCandidateNode> function, final PathArgument childName) {
        @SuppressWarnings("unchecked")
        final var child = ((DistinctNodeContainer<PathArgument, ?>) container).childByArg(childName);
        return child != null ? function.apply(child) : null;
    }
}
