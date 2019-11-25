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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends DeclaredEffectiveStatementBase<A, D> implements DocumentedNode.WithStatus {

    private static final VarHandle UNKNOWN_NODES;

    static {
        try {
            UNKNOWN_NODES = MethodHandles.lookup().findVarHandle(AbstractEffectiveDocumentedNode.class, "unknownNodes",
                ImmutableList.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final String description;
    private final String reference;
    private final @NonNull Status status;

    @SuppressWarnings("unused")
    private volatile ImmutableList<UnknownSchemaNode> unknownNodes;

    /**
     * Constructor.
     *
     * @param ctx
     *            context of statement.
     */
    protected AbstractEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        description = findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class).orElse(null);
        reference = findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class).orElse(null);
        status = findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    @Override
    public final Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public final Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    @Override
    public final Status getStatus() {
        return status;
    }

    @Override
    public final ImmutableList<UnknownSchemaNode> getUnknownSchemaNodes() {
        return derivedList(UNKNOWN_NODES, UnknownSchemaNode.class);
    }

    protected final <T> @NonNull ImmutableList<T> derivedList(final VarHandle vh, final @NonNull Class<T> clazz) {
        final ImmutableList<T> existing = (ImmutableList<T>) vh.getAcquire(this);
        return existing != null ? existing : calculateList(vh, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> @NonNull ImmutableList<T> calculateList(final VarHandle vh, final @NonNull Class<T> clazz) {
        final ImmutableList<T> computed = ImmutableList.copyOf(allSubstatementsOfType(clazz));
        final Object witness = vh.compareAndExchangeRelease(this, null, computed);
        return witness == null ? computed : (ImmutableList<T>) witness;
    }
}
