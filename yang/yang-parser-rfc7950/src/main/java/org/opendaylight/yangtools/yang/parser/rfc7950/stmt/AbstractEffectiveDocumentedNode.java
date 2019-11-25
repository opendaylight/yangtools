/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: 5.0.0: rename to AbstractEffectiveDocumentedNodeWithStatus
public abstract class AbstractEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveDocumentedNodeWithoutStatus<A, D> implements DocumentedNode.WithStatus {
    private final @NonNull Status status;

    /**
     * Constructor.
     *
     * @param ctx
     *            context of statement.
     */
    protected AbstractEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        status = findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    @Override
    public final Status getStatus() {
        return status;
    }
}
