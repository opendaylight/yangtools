/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

enum BuiltinTypeStatement implements TypeStatement {
    BINARY(TypeUtils.BINARY),
    BOOLEAN(TypeUtils.BOOLEAN),
    EMPTY(TypeUtils.EMPTY),
    INSTANCE_IDENTIFIER(TypeUtils.INSTANCE_IDENTIFIER),
    INT8(TypeUtils.INT8),
    INT16(TypeUtils.INT16),
    INT32(TypeUtils.INT32),
    INT64(TypeUtils.INT64),
    STRING(TypeUtils.STRING),
    UINT8(TypeUtils.UINT8),
    UINT16(TypeUtils.UINT16),
    UINT32(TypeUtils.UINT32),
    UINT64(TypeUtils.UINT64);

    private final String argument;

    private BuiltinTypeStatement(final String argument) {
        this.argument = Preconditions.checkNotNull(argument);
    }

    @Override
    public String argument() {
        return argument;
    }

    @Override
    public String rawArgument() {
        return argument;
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return Rfc6020Mapping.TYPE;
    }

    @Override
    public String getName() {
        return argument;
    }

    @Override
    public StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }
}
