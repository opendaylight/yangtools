/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

/**
 * A declared {@link AbstractEffectiveStatement} with DocumentedNode.WithStatus.
 */
@Beta
public abstract class AbstractEffectiveDocumentedNodeWithStatus<A, D extends DeclaredStatement<A>>
        extends AbstractDeclaredEffectiveStatement<A, D>
        implements DocumentedNodeMixin<A, D>, DocumentedNode.WithStatus {
    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;
    private final @NonNull D declared;
    private final A argument;

    protected AbstractEffectiveDocumentedNodeWithStatus(final A argument, final @NonNull D declared,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        this.argument = argument;
        this.declared = requireNonNull(declared);
        this.substatements = requireNonNull(substatements);
    }

    @Override
    public final A argument() {
        return argument;
    }

    @Override
    public final @NonNull D getDeclared() {
        return declared;
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(effectiveSubstatements(), type::isInstance));
    }

    @Override
    public final Status getStatus() {
        return findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    protected final <T> @Nullable T firstSubstatementOfType(final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast).orElse(null);
    }

    protected final <R> R firstSubstatementOfType(final Class<?> type, final Class<R> returnType) {
        return effectiveSubstatements().stream()
                .filter(((Predicate<Object>)type::isInstance).and(returnType::isInstance))
                .findFirst().map(returnType::cast).orElse(null);
    }
}
