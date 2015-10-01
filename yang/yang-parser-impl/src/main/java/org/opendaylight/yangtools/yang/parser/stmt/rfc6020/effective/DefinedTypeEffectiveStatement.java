/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

/**
 * Effective type derived from another base type.
 */
final class DefinedTypeEffectiveStatement implements TypeEffectiveStatement<TypeStatement>,
        TypeDefinition<TypeDefinition<?>> {
    private final TypeDefEffectiveStatementImpl typedef;
    private final TypeEffectiveStatement<?> type;
    private final String argument;

    DefinedTypeEffectiveStatement(final TypeDefEffectiveStatementImpl typedef, final String argument,
        final TypeEffectiveStatement<?> type) {
        this.argument = Preconditions.checkNotNull(argument);
        this.typedef = Preconditions.checkNotNull(typedef);
        this.type = Preconditions.checkNotNull(type);
    }

    @Override
    public TypeStatement getDeclared() {
        return type.getDeclared();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return typedef.get(namespace, identifier);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return typedef.getAll(namespace);
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return typedef.effectiveSubstatements();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return typedef.statementDefinition();
    }

    @Override
    public String argument() {
        return argument;
    }

    @Override
    public StatementSource getStatementSource() {
        return typedef.getStatementSource();
    }

    @Override
    public QName getQName() {
        return typedef.argument();
    }

    @Override
    public SchemaPath getPath() {
        return typedef.getPath();
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return typedef.getUnknownSchemaNodes();
    }

    @Override
    public String getDescription() {
        return typedef.getDescription();
    }

    @Override
    public String getReference() {
        return typedef.getReference();
    }

    @Override
    public Status getStatus() {
        return typedef.getStatus();
    }

    @Override
    public TypeDefinition<?> getBaseType() {
        return typedef.getBaseType();
    }

    @Override
    public String getUnits() {
        return typedef.getUnits();
    }

    @Override
    public Object getDefaultValue() {
        return typedef.getDefaultValue();
    }
}
