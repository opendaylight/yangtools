/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Common shared effective statements for built-in types.
 */
enum BuiltinEffectiveStatement implements TypeEffectiveStatement<TypeStatement> {
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

    private final @NonNull TypeDefinition<?> typedef;

    BuiltinEffectiveStatement(final TypeDefinition<?> typedef) {
        this.typedef = requireNonNull(typedef);
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return typedef;
    }

    @Override
    public final TypeStatement getDeclared() {
        return null;
    }

    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public final QName argument() {
        return getTypeDefinition().getQName();
    }
}
