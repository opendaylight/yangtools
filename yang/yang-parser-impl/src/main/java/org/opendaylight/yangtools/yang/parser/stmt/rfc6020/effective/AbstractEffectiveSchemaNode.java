/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractEffectiveSchemaNode<D extends DeclaredStatement<QName>> extends
        AbstractEffectiveDocumentedNode<QName, D> implements SchemaNode {

    private final QName qname;
    private final SchemaPath path;
    private final List<UnknownSchemaNode> unknownNodes;

    AbstractEffectiveSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = ctx.getSchemaPath().get();

        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();
        ImmutableList.Builder<UnknownSchemaNode> listBuilder = new ImmutableList.Builder<>();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                listBuilder.add((UnknownSchemaNode) effectiveStatement);
            }
        }
        this.unknownNodes = listBuilder.build();
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
