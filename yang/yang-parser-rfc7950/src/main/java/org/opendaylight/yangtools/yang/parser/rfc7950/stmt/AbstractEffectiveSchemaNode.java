/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveSchemaNode<D extends DeclaredStatement<QName>> extends
        AbstractSchemaEffectiveDocumentedNode<QName, D> implements SchemaNode {

    private final @NonNull SchemaPath path;

    protected AbstractEffectiveSchemaNode(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
        this.path = ctx.getSchemaPath().get();
    }

    @Override
    public final QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public final SchemaPath getPath() {
        return path;
    }
}
