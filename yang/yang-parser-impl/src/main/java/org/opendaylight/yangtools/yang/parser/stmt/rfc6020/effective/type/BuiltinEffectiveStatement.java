/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Common shared effective statements for built-in types.
 */
public enum BuiltinEffectiveStatement implements TypeEffectiveStatement<TypeStatement> {
    BINARY(BaseTypes.binaryType()),
    BOOLEAN(BaseTypes.booleanType()),
    EMPTY(BaseTypes.emptyType()),
    INSTANCE_IDENTIFIER(BaseTypes.instanceIdentifierType()),
    INT8(BaseTypes.int8Type()),
    INT16(BaseTypes.int16Type()),
    INT32(BaseTypes.int32Type()),
    INT64(BaseTypes.int64Type()),
    STRING(BaseTypes.stringType()),
    UINT8(BaseTypes.uint8Type()),
    UINT16(BaseTypes.uint16Type()),
    UINT32(BaseTypes.uint32Type()),
    UINT64(BaseTypes.uint64Type());

    private final TypeDefinition<?> typedef;

    BuiltinEffectiveStatement(final TypeDefinition<?> typedef) {
        this.typedef = requireNonNull(typedef);
    }

    @Override
    public @Nonnull TypeDefinition<?> getTypeDefinition() {
        return typedef;
    }

    @Override
    public final TypeStatement getDeclared() {
        return null;
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(@Nonnull final Class<N> namespace,
            @Nonnull final K identifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(@Nonnull final Class<N> namespace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Nonnull
    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public final StatementDefinition statementDefinition() {
        return YangStmtMapping.TYPE;
    }

    @Override
    public final String argument() {
        return getTypeDefinition().getQName().getLocalName();
    }

    @Nonnull
    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.CONTEXT;
    }
}
