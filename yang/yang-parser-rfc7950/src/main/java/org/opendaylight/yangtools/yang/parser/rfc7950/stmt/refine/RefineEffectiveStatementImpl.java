/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

// FIXME: 7.0.0: hide this class
// FIXME: 6.0.0: do not implement SchemaNode
public final class RefineEffectiveStatementImpl extends WithSubstatements<Descendant, RefineStatement>
        implements RefineEffectiveStatement, SchemaNode, DocumentedNodeMixin<Descendant, RefineStatement> {
    private final @NonNull SchemaPath path;
    private final SchemaNode refineTargetNode;

    RefineEffectiveStatementImpl(final RefineStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final SchemaPath path,
            final SchemaNode refineTargetNode) {
        super(declared, substatements);
        this.path = requireNonNull(path);
        this.refineTargetNode = requireNonNull(refineTargetNode);
    }

    public SchemaNode getRefineTargetNode() {
        return refineTargetNode;
    }

    @Override
    @Deprecated
    public QName getQName() {
        return Iterables.getLast(argument().getNodeIdentifiers());
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public Status getStatus() {
        return findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }
}
