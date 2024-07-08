/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Collections2;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.WithChildren;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

/**
 * An {@link AbstractDataTreeCandidateNode} backed by a CandidateNode.WithChildren.
 */
final class CompatDataTreeCandidateNode<T extends CandidateNode & WithChildren> extends AbstractDataTreeCandidateNode {
    private final @NonNull T node;
    private final NormalizedNode dataBefore;
    private final NormalizedNode dataAfter;

    CompatDataTreeCandidateNode(final ModificationType modificationType, final T node,
            final NormalizedNode dataBefore, final NormalizedNode dataAfter) {
        super(modificationType);
        this.node = requireNonNull(node);
        this.dataBefore = dataBefore;
        this.dataAfter = dataAfter;
    }

    @Override
    public PathArgument name() {
        return node.name();
    }

    @Override
    public Collection<DataTreeCandidateNode> childNodes() {
        return Collections2.transform(node.children(), CandidateNode::toLegacy);
    }

    @Override
    public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        final var child = node.modifiedChild(childName);
        return child != null ? child.toLegacy() : null;
    }

    @Override
    public NormalizedNode dataBefore() {
        return dataBefore;
    }

    @Override
    public NormalizedNode dataAfter() {
        return dataAfter;
    }

    @Override
    public CandidateNode toModern() {
        return node;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", node);
    }
}
