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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

final class BuiltinTypeStatement implements TypeStatement {
    private static final Map<String, BuiltinTypeStatement> BUILTINS;

    static {
        final Builder<String, BuiltinTypeStatement> builder = ImmutableMap.builder();
        putBuiltin(builder, TypeUtils.BINARY);
        putBuiltin(builder, TypeUtils.BOOLEAN);
        putBuiltin(builder, TypeUtils.EMPTY);
        putBuiltin(builder, TypeUtils.INSTANCE_IDENTIFIER);
        putBuiltin(builder, TypeUtils.INT8);
        putBuiltin(builder, TypeUtils.INT16);
        putBuiltin(builder, TypeUtils.INT32);
        putBuiltin(builder, TypeUtils.INT64);
        putBuiltin(builder, TypeUtils.STRING);
        putBuiltin(builder, TypeUtils.UINT8);
        putBuiltin(builder, TypeUtils.UINT16);
        putBuiltin(builder, TypeUtils.UINT32);
        putBuiltin(builder, TypeUtils.UINT64);
        BUILTINS = builder.build();
    }

    private static void putBuiltin(final Builder<String, BuiltinTypeStatement> builder, final String argument) {
        builder.put(argument, new BuiltinTypeStatement(argument));
    }

    private final String argument;

    private BuiltinTypeStatement(final String argument) {
        this.argument = Preconditions.checkNotNull(argument);
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

    @Nonnull
    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public StatementDefinition statementDefinition() {
        return YangStmtMapping.TYPE;
    }

    @Nonnull
    @Override
    public String getName() {
        return argument;
    }

    @Nonnull
    @Override
    public StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }
}
