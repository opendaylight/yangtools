/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

final class EmptyDataTreeCandidateNode extends AbstractDataTreeCandidateNode {
    private final @NonNull PathArgument name;

    EmptyDataTreeCandidateNode(final PathArgument name) {
        super(ModificationType.UNMODIFIED);
        this.name = requireNonNull(name);
    }

    @Override
    public PathArgument name() {
        return name;
    }

    @Override
    public Collection<DataTreeCandidateNode> childNodes() {
        return List.of();
    }

    @Override
    public DataTreeCandidateNode modifiedChild(final PathArgument childName) {
        return null;
    }

    @Override
    public NormalizedNode dataAfter() {
        return null;
    }

    @Override
    public NormalizedNode dataBefore() {
        return null;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}