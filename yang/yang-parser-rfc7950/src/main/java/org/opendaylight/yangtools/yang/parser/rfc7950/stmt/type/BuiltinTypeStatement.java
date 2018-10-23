/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

final class BuiltinTypeStatement implements TypeStatement {
    private static final Map<String, BuiltinTypeStatement> BUILTINS;

    static {
        final Builder<String, BuiltinTypeStatement> builder = ImmutableMap.builder();
        putBuiltin(builder, AbstractTypeStatementSupport.BINARY);
        putBuiltin(builder, AbstractTypeStatementSupport.BOOLEAN);
        putBuiltin(builder, AbstractTypeStatementSupport.EMPTY);
        putBuiltin(builder, AbstractTypeStatementSupport.INSTANCE_IDENTIFIER);
        putBuiltin(builder, AbstractTypeStatementSupport.INT8);
        putBuiltin(builder, AbstractTypeStatementSupport.INT16);
        putBuiltin(builder, AbstractTypeStatementSupport.INT32);
        putBuiltin(builder, AbstractTypeStatementSupport.INT64);
        putBuiltin(builder, AbstractTypeStatementSupport.STRING);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT8);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT16);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT32);
        putBuiltin(builder, AbstractTypeStatementSupport.UINT64);
        BUILTINS = builder.build();
    }

    private static void putBuiltin(final Builder<String, BuiltinTypeStatement> builder, final String argument) {
        builder.put(argument, new BuiltinTypeStatement(argument));
    }

    private final String argument;

    private BuiltinTypeStatement(final String argument) {
        this.argument = requireNonNull(argument);
    }

    static TypeStatement maybeReplace(final TypeStatementImpl orig) {
        if (orig.declaredSubstatements().isEmpty() && orig.getStatementSource() == StatementSource.DECLARATION
                && orig.statementDefinition() == YangStmtMapping.TYPE) {
            final BuiltinTypeStatement builtin = BUILTINS.get(orig.argument());
            if (builtin != null) {
                return builtin;
            }
        }

        return orig;
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
        return YangStmtMapping.TYPE;
    }

    @Override
    public StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }
}
