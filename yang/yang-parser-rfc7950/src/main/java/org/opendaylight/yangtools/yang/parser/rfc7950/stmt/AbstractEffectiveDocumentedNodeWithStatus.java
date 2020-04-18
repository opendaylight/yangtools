/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveDocumentedNodeWithStatus<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveDocumentedNode<A, D> implements DocumentedNode.WithStatus {
    private static final VarHandle UNKNOWN_NODES;

    static {
        try {
            UNKNOWN_NODES = MethodHandles.lookup().findVarHandle(AbstractEffectiveDocumentedNodeWithStatus.class,
                "unknownNodes", ImmutableList.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Status status;

    @SuppressWarnings("unused")
    private volatile ImmutableList<UnknownSchemaNode> unknownNodes;

    /**
     * Constructor.
     *
     * @param ctx
     *            context of statement.
     */
    protected AbstractEffectiveDocumentedNodeWithStatus(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        status = findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    @Override
    public final Collection<? extends UnknownSchemaNode> getUnknownSchemaNodes() {
        final ImmutableList<UnknownSchemaNode> existing =
                (ImmutableList<UnknownSchemaNode>) UNKNOWN_NODES.getAcquire(this);
        return existing != null ? existing : loadUnknownSchemaNodes();
    }

    @SuppressWarnings("unchecked")
    private @NonNull ImmutableList<UnknownSchemaNode> loadUnknownSchemaNodes() {
        final List<UnknownSchemaNode> init = new ArrayList<>();
        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof UnknownSchemaNode) {
                init.add((UnknownSchemaNode) stmt);
            }
        }

        final ImmutableList<UnknownSchemaNode> computed = ImmutableList.copyOf(init);
        final Object witness = UNKNOWN_NODES.compareAndExchangeRelease(this, null, computed);
        return witness == null ? computed : (ImmutableList<UnknownSchemaNode>) witness;
    }
}
