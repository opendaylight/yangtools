/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Collection;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class RefineEffectiveStatementImpl extends
        AbstractEffectiveDocumentedNode<SchemaNodeIdentifier, RefineStatement>
        implements SchemaNode {

    private final QName qname;
    private final SchemaPath path;
    ImmutableList<UnknownSchemaNode> unknownNodes;
    private final SchemaNode refineTargetNode;

    public RefineEffectiveStatementImpl(
            StmtContext<SchemaNodeIdentifier, RefineStatement, ?> ctx) {
        super(ctx);

        qname = ctx.getStatementArgument().getLastComponent();
        path = Utils.getSchemaPath(ctx);
        refineTargetNode = (SchemaNode) ctx.getEffectOfStatement().iterator().next().buildEffective();

        initSubstatementCollectionsAndFields();

    }

    public SchemaNode getRefineTargetNode() {
        return refineTargetNode;
    }

    private void initSubstatementCollectionsAndFields() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();

        for (EffectiveStatement<?, ?> effectiveSubstatement : effectiveSubstatements) {
            if (effectiveSubstatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveSubstatement;
                unknownNodesInit.add(unknownNode);
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }
}