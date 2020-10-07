/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigRegexpPosixEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigRegexpPosixStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
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

public class RegexpPosixStatementSupport extends
        BaseVoidStatementSupport<OpenConfigRegexpPosixStatement, OpenConfigRegexpPosixEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements OpenConfigRegexpPosixStatement {

        private final  StatementDefinition definition;

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
            extends UnknownEffectiveStatementBase<Void, OpenConfigRegexpPosixStatement>
            implements OpenConfigRegexpPosixEffectiveStatement {

        private final SchemaPath path;

        Effective(final StmtContext<Void, OpenConfigRegexpPosixStatement, ?> ctx,
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
    private static final OpenConfigStatements CURRENT_STATEMENT = OpenConfigStatements.OPENCONFIG_REGEXP_POSIX;
    private static final RegexpPosixStatementSupport INSTANCE = new RegexpPosixStatementSupport();

    RegexpPosixStatementSupport() {
        super(CURRENT_STATEMENT);
        this.validator = SubstatementValidator.builder(CURRENT_STATEMENT).build();
    }

    public static RegexpPosixStatementSupport getInstance() {
        return INSTANCE;
    }


    @Override
    protected  OpenConfigRegexpPosixStatement createDeclared(
             StmtContext<Void, OpenConfigRegexpPosixStatement, ?> ctx,
             ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegexpPosixStatementSupport.Declared(getPublicView(), substatements);
    }

    @Override
    protected  OpenConfigRegexpPosixStatement createEmptyDeclared(
             StmtContext<Void, OpenConfigRegexpPosixStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected  OpenConfigRegexpPosixEffectiveStatement createEffective(
             StmtContext<Void, OpenConfigRegexpPosixStatement, OpenConfigRegexpPosixEffectiveStatement> ctx,
             OpenConfigRegexpPosixStatement declared,
             ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegexpPosixStatementSupport.Effective(ctx, substatements);
    }

    @Override
    protected  OpenConfigRegexpPosixEffectiveStatement createEmptyEffective(
             StmtContext<Void, OpenConfigRegexpPosixStatement, OpenConfigRegexpPosixEffectiveStatement> ctx,
             OpenConfigRegexpPosixStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    @Override
    protected @Nullable SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
