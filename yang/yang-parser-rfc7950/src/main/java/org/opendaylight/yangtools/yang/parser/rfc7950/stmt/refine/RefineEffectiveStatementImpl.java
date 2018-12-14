/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: hide this class
public final class RefineEffectiveStatementImpl
        extends AbstractEffectiveDocumentedNode<SchemaNodeIdentifier, RefineStatement>
        implements RefineEffectiveStatement, SchemaNode {

    private final @NonNull QName qname;
    private final @NonNull SchemaPath path;
    private final @NonNull ImmutableList<UnknownSchemaNode> unknownNodes;
    private final SchemaNode refineTargetNode;

    RefineEffectiveStatementImpl(final StmtContext<SchemaNodeIdentifier, RefineStatement, ?> ctx) {
        super(ctx);
        qname = verifyNotNull(ctx.coerceStatementArgument().getLastComponent());
        path = ctx.getSchemaPath().get();
        refineTargetNode = (SchemaNode) ctx.getEffectOfStatement().iterator().next().buildEffective();

        // initSubstatementCollectionsAndFields
        this.unknownNodes = ImmutableList.copyOf(effectiveSubstatements().stream()
            .filter(UnknownSchemaNode.class::isInstance)
            .map(UnknownSchemaNode.class::cast)
            .collect(Collectors.toList()));
    }

    public SchemaNode getRefineTargetNode() {
        return refineTargetNode;
    }

    @Nonnull
    @Override
    public QName getQName() {
        return qname;
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }
}