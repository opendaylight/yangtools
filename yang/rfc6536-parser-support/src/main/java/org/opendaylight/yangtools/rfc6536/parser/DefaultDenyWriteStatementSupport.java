/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyWriteStatementSupport
        extends AbstractEmptyStatementSupport<DefaultDenyWriteStatement, DefaultDenyWriteEffectiveStatement> {
    private static final class Declared extends WithSubstatements implements DefaultDenyWriteStatement {
        static final @NonNull Declared EMPTY = new Declared(ImmutableList.of());

        Declared(final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(substatements);
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<Empty, DefaultDenyWriteStatement>
            implements DefaultDenyWriteEffectiveStatement, DefaultDenyWriteSchemaNode {
        private final @NonNull Immutable path;

        Effective(final Current<Empty, DefaultDenyWriteStatement> stmt,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(stmt, substatements);
            path = SchemaPathSupport.toEffectivePath(stmt.getEffectiveParent().getSchemaPath()
                    .createChild(stmt.publicDefinition().getStatementName()));
        }

        @Override
        public QName getQName() {
            return SchemaNodeDefaults.extractQName(path);
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return SchemaNodeDefaults.extractPath(this, path);
        }

        @Override
        public DefaultDenyWriteEffectiveStatement asEffectiveStatement() {
            return this;
        }
    }

    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(NACMStatements.DEFAULT_DENY_WRITE).build();

    public DefaultDenyWriteStatementSupport(final YangParserConfiguration config) {
        super(NACMStatements.DEFAULT_DENY_WRITE, StatementPolicy.contextIndependent(), config);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected DefaultDenyWriteStatement createDeclared(final StmtContext<Empty, DefaultDenyWriteStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? Declared.EMPTY : new Declared(substatements);
    }

    @Override
    protected DefaultDenyWriteEffectiveStatement createEffective(final Current<Empty, DefaultDenyWriteStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(stmt, substatements);
    }
}
