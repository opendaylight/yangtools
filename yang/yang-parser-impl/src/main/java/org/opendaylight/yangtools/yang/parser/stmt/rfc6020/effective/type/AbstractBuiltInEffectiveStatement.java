/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;

abstract class AbstractBuiltInEffectiveStatement<T extends TypeDefinition<T>>
        implements TypedefEffectiveStatement {
    @Override
    public final StatementDefinition statementDefinition() {
        return Rfc6020Mapping.TYPE;
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.CONTEXT;
    }

    @Override
    public TypedefStatement getDeclared() {
        return null;
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return null;
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return ImmutableMap.of();
    }

    @Override
    public final QName argument() {
        return getTypeDefinition().getQName();
    }
}
