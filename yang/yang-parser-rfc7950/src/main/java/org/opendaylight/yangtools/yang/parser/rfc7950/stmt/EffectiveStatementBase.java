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
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Stateful version of {@link AbstractEffectiveStatement}, which holds substatements in an {@link ImmutableList}.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
// TODO: This class is problematic in that it interacts with its subclasses via methods which are guaranteed to allows
//       atrocities like RecursiveObjectLeaker tricks. That should be avoided and pushed to caller in a way where
//       this class is a pure holder taking {@code ImmutableList<? extends EffectiveStatement<?, ?>>} in the
//       constructor.
//
//       From memory efficiency perspective, it is very common to
public abstract class EffectiveStatementBase<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D> {
    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;

    /**
     * Constructor.
     *
     * @param ctx context of statement.
     */
    protected EffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        this.substatements = ImmutableList.copyOf(initSubstatements(ctx, declaredSubstatements(ctx)));
    }

    @Beta
    protected Collection<? extends EffectiveStatement<?, ?>> initSubstatements(final StmtContext<A, D, ?> ctx,
            final Collection<? extends StmtContext<?, ?, ?>> substatementsInit) {
        return initSubstatements(substatementsInit);
    }

    /**
     * Create a set of substatements. This method is split out so it can be overridden in
     * ExtensionEffectiveStatementImpl to leak a not-fully-initialized instance.
     *
     * @param substatementsInit proposed substatements
     * @return Filtered substatements
     */
    protected Collection<? extends EffectiveStatement<?, ?>> initSubstatements(
            final Collection<? extends StmtContext<?, ?, ?>> substatementsInit) {
        return Collections2.transform(Collections2.filter(substatementsInit,
            StmtContext::isSupportedToBuildEffective), StmtContext::buildEffective);
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    // FIXME: rename to 'getFirstEffectiveStatement()'
    protected final <S extends SchemaNode> S firstSchemaNode(final Class<S> type) {
        return findFirstEffectiveSubstatement(type).orElse(null);
    }
}
