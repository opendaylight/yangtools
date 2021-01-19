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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractVoidStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyAllStatementSupport
        extends AbstractVoidStatementSupport<DefaultDenyAllStatement, DefaultDenyAllEffectiveStatement> {
    private static final class Declared extends WithSubstatements implements DefaultDenyAllStatement {
        static final @NonNull Declared EMPTY = new Declared(ImmutableList.of());

        Declared(final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(substatements);
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<Void, DefaultDenyAllStatement>
            implements DefaultDenyAllEffectiveStatement, DefaultDenyAllSchemaNode {
        private final @Nullable SchemaPath path;

        Effective(final Current<Void, DefaultDenyAllStatement> stmt,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(stmt, substatements);
            path = SchemaPathSupport.wrap(stmt.getEffectiveParent().getSchemaPath()
                    .createChild(stmt.publicDefinition().getStatementName()));
        }

        @Override
        public QName getQName() {
            return path.getLastComponent();
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return SchemaNodeDefaults.throwUnsupportedIfNull(this, path);
        }

        @Override
        public DefaultDenyAllEffectiveStatement asEffectiveStatement() {
            return this;
        }
    }

    private static final DefaultDenyAllStatementSupport INSTANCE =
            new DefaultDenyAllStatementSupport(NACMStatements.DEFAULT_DENY_ALL);

    private final SubstatementValidator validator;

    private DefaultDenyAllStatementSupport(final StatementDefinition definition) {
        super(definition, StatementPolicy.contextIndependent());
        this.validator = SubstatementValidator.builder(definition).build();
    }

    public static DefaultDenyAllStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected DefaultDenyAllStatement createDeclared(final StmtContext<Void, DefaultDenyAllStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(substatements);
    }

    @Override
    protected DefaultDenyAllStatement createEmptyDeclared(final StmtContext<Void, DefaultDenyAllStatement, ?> ctx) {
        return Declared.EMPTY;
    }

    @Override
    protected DefaultDenyAllEffectiveStatement createEffective(final Current<Void, DefaultDenyAllStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(stmt, substatements);
    }
}
