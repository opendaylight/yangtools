/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;

abstract class EmptyConfigEffectiveStatement implements ConfigEffectiveStatement {
    static final EmptyConfigEffectiveStatement FALSE = new EmptyConfigEffectiveStatement() {
        @Override
        public ConfigStatement getDeclared() {
            return EmptyConfigStatement.FALSE;
        }
    };

    static final EmptyConfigEffectiveStatement TRUE = new EmptyConfigEffectiveStatement() {
        @Override
        public ConfigStatement getDeclared() {
            return EmptyConfigStatement.TRUE;
        }
    };

    private EmptyConfigEffectiveStatement() {
        // Hidden
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return getDeclared().statementDefinition();
    }

    @Override
    public final Boolean argument() {
        return getDeclared().argument();
    }

    @Override
    public final StatementSource getStatementSource() {
        return getDeclared().getStatementSource();
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public abstract @NonNull ConfigStatement getDeclared();
}
