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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

abstract class AbstractHashedValueStatementSupport
        extends AbstractEmptyStatementSupport<OpenConfigHashedValueStatement, OpenConfigHashedValueEffectiveStatement> {

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
            extends UnknownEffectiveStatementBase<Empty, OpenConfigHashedValueStatement>
            implements OpenConfigHashedValueEffectiveStatement {
        private final @NonNull StatementDefinition definition;
        private final @NonNull Immutable path;

        Effective(final Current<Empty, OpenConfigHashedValueStatement> stmt,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(stmt, substatements);
            definition = stmt.publicDefinition();
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
        public StatementDefinition statementDefinition() {
            return definition;
        }

        @Override
        public OpenConfigHashedValueEffectiveStatement asEffectiveStatement() {
            return this;
        }
    }

    private final SubstatementValidator validator;

    AbstractHashedValueStatementSupport(final StatementDefinition definition, final YangParserConfiguration config) {
        super(definition, StatementPolicy.contextIndependent(), config);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    @Override
    protected final SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected final OpenConfigHashedValueStatement createDeclared(
            final StmtContext<Empty, OpenConfigHashedValueStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(getPublicView(), substatements);
    }

    @Override
    protected OpenConfigHashedValueEffectiveStatement createEffective(
            final Current<Empty, OpenConfigHashedValueStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(stmt, substatements);
    }
}
