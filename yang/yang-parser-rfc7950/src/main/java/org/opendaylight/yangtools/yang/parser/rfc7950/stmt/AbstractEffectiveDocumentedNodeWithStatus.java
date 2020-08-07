/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A declared {@link AbstractEffectiveStatement} with DocumentedNode.WithStatus.
 */
//FIXME: 6.0.0: use DocumentedNodeMixin.WithStatus instead of keeping any state
@Beta
public abstract class AbstractEffectiveDocumentedNodeWithStatus<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D> implements DocumentedNode.WithStatus {
    private static final VarHandle UNKNOWN_NODES;

    static {
        try {
            UNKNOWN_NODES = MethodHandles.lookup().findVarHandle(AbstractEffectiveDocumentedNodeWithStatus.class,
                "unknownNodes", ImmutableList.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;
    private final @NonNull StatementSource statementSource;
    private final A argument;
    private final @NonNull D declaredInstance;
    private final @Nullable String description;
    private final @Nullable String reference;
    private final @NonNull Status status;

    @SuppressWarnings("unused")
    private volatile ImmutableList<UnknownSchemaNode> unknownNodes;

    /**
     * Constructor.
     *
     * @param ctx context of statement.
     */
    protected AbstractEffectiveDocumentedNodeWithStatus(final StmtContext<A, D, ?> ctx) {
        argument = ctx.getStatementArgument();
        statementSource = ctx.getStatementSource();
        declaredInstance = ctx.buildDeclared();
        substatements = ImmutableList.copyOf(
            Collections2.transform(Collections2.filter(BaseStatementSupport.declaredSubstatements(ctx),
                StmtContext::isSupportedToBuildEffective), StmtContext::buildEffective));

        description = findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class).orElse(null);
        reference = findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class).orElse(null);
        status = findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return declaredInstance.statementDefinition();
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public final StatementSource getStatementSource() {
        return statementSource;
    }

    @Override
    public final D getDeclared() {
        return declaredInstance;
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    @Override
    public final Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public final Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(effectiveSubstatements(), type::isInstance));
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

    protected final <T> @Nullable T firstSubstatementOfType(final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast).orElse(null);
    }

    protected final <R> R firstSubstatementOfType(final Class<?> type, final Class<R> returnType) {
        return effectiveSubstatements().stream()
                .filter(((Predicate<Object>)type::isInstance).and(returnType::isInstance))
                .findFirst().map(returnType::cast).orElse(null);
    }

    protected final EffectiveStatement<?, ?> firstEffectiveSubstatementOfType(final Class<?> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().orElse(null);
    }

    // FIXME: rename to 'getFirstEffectiveStatement()'
    protected final <S extends SchemaNode> S firstSchemaNode(final Class<S> type) {
        return findFirstEffectiveSubstatement(type).orElse(null);
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
