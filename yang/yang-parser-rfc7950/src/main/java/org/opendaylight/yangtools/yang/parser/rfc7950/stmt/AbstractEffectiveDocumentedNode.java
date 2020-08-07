/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A declared {@link AbstractEffectiveStatement}.
 *
 * @deprecated This class has a number of design problems. Use {@link AbstractDeclaredEffectiveStatement} and its
 *             subclasses instead.
 */
// TODO: This class is problematic in that it interacts with its subclasses via methods which are guaranteed to allows
//       atrocities like RecursiveObjectLeaker tricks. That should be avoided and pushed to caller in a way where
//       this class is a pure holder taking {@code ImmutableList<? extends EffectiveStatement<?, ?>>} in the
//       constructor.
//
// From memory efficiency perspective, it is very common to have effective statements without any substatements,
// in which case 'substatements' field is redundant.
@Beta
@Deprecated(forRemoval = true)
// FIXME: 6.0.0: fold this into AbstractEffectiveDocumentedNodeWithStatus
public abstract class AbstractEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D> implements DocumentedNode {
    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;
    private final @NonNull StatementSource statementSource;
    private final A argument;
    private final @NonNull D declaredInstance;
    private final @Nullable String description;
    private final @Nullable String reference;

    protected AbstractEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        description = findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class).orElse(null);
        reference = findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class).orElse(null);
        argument = ctx.getStatementArgument();
        statementSource = ctx.getStatementSource();
        declaredInstance = ctx.buildDeclared();

        substatements = ImmutableList.copyOf(initSubstatements(BaseStatementSupport.declaredSubstatements(ctx)));
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


    protected final @Nullable String nullableDescription() {
        return description;
    }

    protected final @Nullable String nullableReference() {
        return reference;
    }

    /**
     * Create a set of substatements. This method is split out so it can be overridden in
     * ExtensionEffectiveStatementImpl to leak a not-fully-initialized instance.
     *
     * @param substatementsInit proposed substatements
     * @return Filtered substatements
     */
    // FIXME: 6.0.0: this facility is only overridden by ExtensionEffectiveStatementImpl
    protected Collection<? extends EffectiveStatement<?, ?>> initSubstatements(
            final Collection<? extends StmtContext<?, ?, ?>> substatementsInit) {
        return Collections2.transform(Collections2.filter(substatementsInit,
            StmtContext::isSupportedToBuildEffective), StmtContext::buildEffective);
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


}
