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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;

/**
 * A declared {@link AbstractDeclaredEffectiveStatement} with {@code UnknownSchemaNode}.
 */
@Beta
// FIXME: remove
public abstract class AbstractEffectiveUnknownSchmemaNode<A, D extends UnknownStatement<A>>
        extends AbstractDeclaredEffectiveStatement<A, D>
        implements DocumentedNodeMixin<A, D> {
    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;
    private final @NonNull D declared;
    private final @NonNull A argument;

    protected AbstractEffectiveUnknownSchmemaNode(final @NonNull D declared, final A argument,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        this.argument = requireNonNull(argument);
        this.declared = requireNonNull(declared);
        this.substatements = requireNonNull(substatements);
    }

    @Override
    public final A argument() {
        return argument;
    }

    @Override
    public final @NonNull D declared() {
        return declared;
    }

    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(effectiveSubstatements(), type::isInstance));
    }
}
