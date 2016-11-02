/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends DeclaredEffectiveStatementBase<A, D> implements DocumentedNode.WithStatus {

    private final String description;
    private final String reference;
    private final Status status;

    /**
     * Constructor.
     *
     * @param ctx
     *            context of statement.
     */
    protected AbstractEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);

        final DescriptionEffectiveStatementImpl descStmt = firstEffective(DescriptionEffectiveStatementImpl.class);
        if (descStmt != null) {
            description = descStmt.argument();
        } else {
            description = null;
        }

        final ReferenceEffectiveStatementImpl refStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        if (refStmt != null) {
            reference = refStmt.argument();
        } else {
            reference = null;
        }

        final StatusEffectiveStatementImpl statusStmt = firstEffective(StatusEffectiveStatementImpl.class);
        if (statusStmt != null) {
            status = statusStmt.argument();
        } else {
            status = Status.CURRENT;
        }
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getReference() {
        return reference;
    }

    @Nonnull
    @Override
    public final Status getStatus() {
        return status;
    }
}
