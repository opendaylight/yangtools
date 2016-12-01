/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

abstract class AbstractBuiltinEffectiveStatement implements TypeEffectiveStatement<TypeStatement> {
    @Override
    public final TypeStatement getDeclared() {
        return null;
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return YangStmtMapping.TYPE;
    }

    @Override
    public final String argument() {
        return getTypeDefinition().getQName().getLocalName();
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.CONTEXT;
    }
}
