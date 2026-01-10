/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;

/**
 * A declared {@link AbstractDeclaredEffectiveStatement} with {@link UnknownSchemaNode}.
 */
@Beta
public abstract class AbstractEffectiveUnknownSchmemaNode<A, D extends UnknownStatement<A>>
        extends AbstractDeclaredEffectiveStatement<A, D>
        implements DocumentedNodeMixin<A, D>, UnknownSchemaNode {
    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;
    private final @NonNull D declared;
    private final @NonNull A argument;
    @Deprecated(since = "7.0.9")
    private final boolean addedByUses;
    private final boolean augmenting;

    protected AbstractEffectiveUnknownSchmemaNode(final @NonNull D declared, final A argument,
            final CopyableNode history,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        this.argument = requireNonNull(argument);
        this.declared = requireNonNull(declared);
        this.substatements = requireNonNull(substatements);
        augmenting = history.isAugmenting();
        addedByUses = history.isAddedByUses();
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
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    @Override
    public final QName getNodeType() {
        return statementDefinition().statementName();
    }

    @Override
    public final String getNodeParameter() {
        final String rawArgument = getDeclared().rawArgument();
        return rawArgument == null ? "" : rawArgument;
    }

    @Deprecated(forRemoval = true)
    @Override
    public final boolean isAddedByUses() {
        return addedByUses;
    }

    @Deprecated
    @Override
    public final boolean isAugmenting() {
        return augmenting;
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(effectiveSubstatements(), type::isInstance));
    }

    @Override
    public final Status getStatus() {
        return findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }
}
