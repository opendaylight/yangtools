/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: 5.0.0: hide this class
public final class RefineEffectiveStatementImpl
        extends AbstractEffectiveDocumentedNode<SchemaNodeIdentifier, RefineStatement>
        implements RefineEffectiveStatement, SchemaNode {

    private final @NonNull QName qname;
    private final @NonNull SchemaPath path;
    private final SchemaNode refineTargetNode;

    RefineEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, RefineStatement, ?> ctx) {
        super(ctx);
        qname = Iterables.getLast(ctx.coerceStatementArgument().getNodeIdentifiers());
        path = ctx.getSchemaPath().get();
        refineTargetNode = (SchemaNode) ctx.getEffectOfStatement().iterator().next().buildEffective();
    }

    public SchemaNode getRefineTargetNode() {
        return refineTargetNode;
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }
}