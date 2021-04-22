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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;

public final class RefineEffectiveStatementImpl extends WithSubstatements<Descendant, RefineStatement>
        implements RefineEffectiveStatement, DocumentedNodeMixin<Descendant, RefineStatement> {
    private final @NonNull SchemaNode refineTargetNode;

    RefineEffectiveStatementImpl(final RefineStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final SchemaNode refineTargetNode) {
        super(declared, substatements);
        this.refineTargetNode = requireNonNull(refineTargetNode);
    }

    // FIXME: 8.0.0: discover this through namespace population
    public SchemaNode getRefineTargetNode() {
        return refineTargetNode;
    }
}
