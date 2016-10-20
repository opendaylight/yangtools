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
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

final class BuiltinDeclaredStatements {
    private static final class BuiltinTypeStatement implements TypeStatement {
        private final String argument;

        BuiltinTypeStatement(final String argument) {
            this.argument = Preconditions.checkNotNull(argument);
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
        public String argument() {
            return argument;
        }

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }

        @Override
        public String getName() {
            return argument;
        }
    }

    private static final Map<String, BuiltinTypeStatement> BUILTINS;
    static {
        final Builder<String, BuiltinTypeStatement> builder = ImmutableMap.builder();
        putBuiltin(builder, BaseTypes.binaryType());
        putBuiltin(builder, BaseTypes.booleanType());
        putBuiltin(builder, BaseTypes.emptyType());
        putBuiltin(builder, BaseTypes.instanceIdentifierType());
        putBuiltin(builder, BaseTypes.int8Type());
        putBuiltin(builder, BaseTypes.int16Type());
        putBuiltin(builder, BaseTypes.int32Type());
        putBuiltin(builder, BaseTypes.int64Type());
        putBuiltin(builder, BaseTypes.stringType());
        putBuiltin(builder, BaseTypes.uint8Type());
        putBuiltin(builder, BaseTypes.uint16Type());
        putBuiltin(builder, BaseTypes.uint32Type());
        putBuiltin(builder, BaseTypes.uint64Type());

        BUILTINS = builder.build();
    }

    private static void putBuiltin(final Builder<String, BuiltinTypeStatement> builder, final TypeDefinition<?> type) {
        final String argument = type.getQName().getLocalName();
        builder.put(argument, new BuiltinTypeStatement(argument));
    }

    private BuiltinDeclaredStatements() {
        throw new UnsupportedOperationException();
    }

    static TypeStatement replaceIfBuiltin(final TypeStatementImpl orig) {
        if (orig.declaredSubstatements().isEmpty() && orig.getStatementSource() == StatementSource.DECLARATION &&
                orig.statementDefinition() == Rfc6020Mapping.TYPE) {
            final BuiltinTypeStatement builtin = BUILTINS.get(orig.argument());
            if (builtin != null) {
                return builtin;
            }
        }

        return orig;
    }
}
