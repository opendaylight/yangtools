/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

abstract class AbstractBuiltInTypeEffectiveStatement<T extends TypeDefinition<T>>
        extends ForwardingObject implements TypeEffectiveStatement<TypeStatement>, TypeDefinition<T> {

    @Override
    protected abstract T delegate();

    @Override
    public final QName getQName() {
        return delegate().getQName();
    }

    @Override
    public final SchemaPath getPath() {
        return delegate().getPath();
    }

    @Override
    public final List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return delegate().getUnknownSchemaNodes();
    }

    @Override
    public final String getDescription() {
        return delegate().getDescription();
    }

    @Override
    public final String getReference() {
        return delegate().getReference();
    }

    @Override
    public final Status getStatus() {
        return delegate().getStatus();
    }

    @Override
    public final T getBaseType() {
        return delegate().getBaseType();
    }

    @Override
    public final String getUnits() {
        return delegate().getUnits();
    }

    @Override
    public final Object getDefaultValue() {
        return delegate().getDefaultValue();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return Rfc6020Mapping.TYPE;
    }

    @Override
    public final String argument() {
        return getQName().getLocalName();
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.CONTEXT;
    }

    @Override
    public final TypeStatement getDeclared() {
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
}
