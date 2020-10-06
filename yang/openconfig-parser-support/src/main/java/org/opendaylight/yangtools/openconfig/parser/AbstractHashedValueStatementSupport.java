/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseVoidStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

abstract class AbstractHashedValueStatementSupport
        extends BaseVoidStatementSupport<OpenConfigHashedValueStatement, OpenConfigHashedValueEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements OpenConfigHashedValueStatement {
        private final @NonNull StatementDefinition definition;

        Declared(final StatementDefinition definition,
                final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(substatements);
            this.definition = requireNonNull(definition);
        }

        @Override
        public StatementDefinition statementDefinition() {
            return definition;
        }
    }

    private static final class Effective
            extends UnknownEffectiveStatementBase<Void, OpenConfigHashedValueStatement>
            implements OpenConfigHashedValueEffectiveStatement {
        private final @NonNull StatementDefinition definition;
        private final SchemaPath path;

        Effective(final OpenConfigHashedValueStatement declared,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
                final StmtContext<Void, OpenConfigHashedValueStatement, ?> ctx) {
            super(declared.argument(), declared, substatements, ctx);
            definition = ctx.getPublicDefinition();
            path = ctx.coerceParentContext().getSchemaPath().get().createChild(
                ctx.getPublicDefinition().getStatementName());
        }

        @Override
        public QName getQName() {
            return path.getLastComponent();
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public StatementDefinition statementDefinition() {
            return definition;
        }
    }

    private final SubstatementValidator validator;

    AbstractHashedValueStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    @Override
    protected final SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected final OpenConfigHashedValueStatement createDeclared(
            final StmtContext<Void, OpenConfigHashedValueStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(getPublicView(), substatements);
    }

    @Override
    protected final OpenConfigHashedValueStatement createEmptyDeclared(
            final StmtContext<Void, OpenConfigHashedValueStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected final OpenConfigHashedValueEffectiveStatement createEffective(
            final StmtContext<Void, OpenConfigHashedValueStatement, OpenConfigHashedValueEffectiveStatement> ctx,
            final OpenConfigHashedValueStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(declared, substatements, ctx);
    }

    @Override
    protected final OpenConfigHashedValueEffectiveStatement createEmptyEffective(
            final StmtContext<Void, OpenConfigHashedValueStatement, OpenConfigHashedValueEffectiveStatement> ctx,
            final OpenConfigHashedValueStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
