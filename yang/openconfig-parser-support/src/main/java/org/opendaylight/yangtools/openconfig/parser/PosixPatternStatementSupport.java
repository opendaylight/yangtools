/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigPosixPatternEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigPosixPatternStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public class PosixPatternStatementSupport extends
        BaseStringStatementSupport<OpenConfigPosixPatternStatement, OpenConfigPosixPatternEffectiveStatement> {

    private static final class Declared extends WithSubstatements<String> implements OpenConfigPosixPatternStatement {

        private final @NonNull StatementDefinition definition;

        Declared(final StatementDefinition definition,
                final StmtContext statementContext,
                final ImmutableList<? extends DeclaredStatement<?>> substatements) {

            super(statementContext, substatements);
            this.definition = requireNonNull(definition);
        }

        @Override
        public StatementDefinition statementDefinition() {
            return definition;
        }
    }

    private static final class Effective
            extends UnknownEffectiveStatementBase<String, OpenConfigPosixPatternStatement>
            implements OpenConfigPosixPatternEffectiveStatement {
        private final SchemaPath path;

        Effective(final StmtContext<String, OpenConfigPosixPatternStatement, ?> ctx,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(ctx, substatements);
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

    }

    private final SubstatementValidator validator;
    private static final OpenConfigStatements CURRENT_STATEMENT = OpenConfigStatements.OPENCONFIG_POSIX_PATTERN;
    private static final PosixPatternStatementSupport INSTANCE = new PosixPatternStatementSupport();

    protected PosixPatternStatementSupport() {
        super(CURRENT_STATEMENT);
        this.validator = SubstatementValidator.builder(CURRENT_STATEMENT).build();
    }

    public static PosixPatternStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected @NonNull OpenConfigPosixPatternStatement createDeclared(
            @NonNull StmtContext<String, OpenConfigPosixPatternStatement, ?> ctx,
            @NonNull ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new PosixPatternStatementSupport.Declared(getPublicView(), ctx, substatements);
    }

    @Override
    protected @NonNull OpenConfigPosixPatternStatement createEmptyDeclared(
            @NonNull StmtContext<String, OpenConfigPosixPatternStatement, ?> ctx) {
        return new PosixPatternStatementSupport.Declared(getPublicView(), ctx, ImmutableList.of());
    }

    @Override
    protected @NonNull OpenConfigPosixPatternEffectiveStatement createEffective(
            @NonNull StmtContext<String, OpenConfigPosixPatternStatement, OpenConfigPosixPatternEffectiveStatement> ctx,
            @NonNull OpenConfigPosixPatternStatement declared,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new PosixPatternStatementSupport.Effective(ctx, substatements);
    }

    @Override
    protected @NonNull OpenConfigPosixPatternEffectiveStatement createEmptyEffective(
            @NonNull StmtContext<String, OpenConfigPosixPatternStatement, OpenConfigPosixPatternEffectiveStatement> ctx,
            @NonNull OpenConfigPosixPatternStatement declared) {
        return createEffective(ctx, declared,ImmutableList.of());
    }

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    @Override
    protected @Nullable SubstatementValidator getSubstatementValidator() {
        return this.validator;
    }
}
