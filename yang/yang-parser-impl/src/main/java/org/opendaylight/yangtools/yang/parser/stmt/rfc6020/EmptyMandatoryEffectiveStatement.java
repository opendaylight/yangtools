/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.MandatoryEffectiveStatement;

abstract class EmptyMandatoryEffectiveStatement implements MandatoryEffectiveStatement {
    static final EmptyMandatoryEffectiveStatement FALSE = new EmptyMandatoryEffectiveStatement() {
        @Override
        public MandatoryStatement getDeclared() {
            return EmptyMandatoryStatement.FALSE;
        }
    };

    static final EmptyMandatoryEffectiveStatement TRUE = new EmptyMandatoryEffectiveStatement() {
        @Override
        public MandatoryStatement getDeclared() {
            return EmptyMandatoryStatement.TRUE;
        }
    };

    @Nonnull
    @Override
    public final StatementDefinition statementDefinition() {
        return getDeclared().statementDefinition();
    }

    @Override
    public final Boolean argument() {
        return getDeclared().argument();
    }

    @Nonnull
    @Override
    public final StatementSource getStatementSource() {
        return getDeclared().getStatementSource();
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(@Nonnull final Class<N> namespace,
            @Nonnull final K identifier) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(@Nonnull final Class<N> namespace) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Nonnull
    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }
}
