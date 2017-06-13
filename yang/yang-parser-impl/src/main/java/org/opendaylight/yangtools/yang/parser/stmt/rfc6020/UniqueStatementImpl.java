/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UniqueEffectiveStatementImpl;

public class UniqueStatementImpl extends AbstractDeclaredStatement<Collection<SchemaNodeIdentifier.Relative>>
        implements UniqueStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .UNIQUE)
            .build();

    protected UniqueStatementImpl(final StmtContext<Collection<SchemaNodeIdentifier.Relative>, UniqueStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Collection<SchemaNodeIdentifier.Relative>, UniqueStatement,
                    EffectiveStatement<Collection<SchemaNodeIdentifier.Relative>, UniqueStatement>> {

        public Definition() {
            super(YangStmtMapping.UNIQUE);
        }

        @Override
        public Collection<SchemaNodeIdentifier.Relative> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            final Collection<Relative> uniqueConstraints = Utils.parseUniqueConstraintArgument(ctx, value);
            SourceException.throwIf(uniqueConstraints.isEmpty(), ctx.getStatementSourceReference(),
                    "Invalid argument value '%s' of unique statement. The value must contains at least "
                            + "one descendant schema node identifier.", value);
            return uniqueConstraints;
        }

        @Override
        public UniqueStatement createDeclared(final StmtContext<Collection<Relative>, UniqueStatement, ?> ctx) {
            return new UniqueStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Collection<Relative>, UniqueStatement> createEffective
                (final StmtContext<Collection<Relative>, UniqueStatement, EffectiveStatement<Collection<Relative>,
                        UniqueStatement>> ctx) {
            return new UniqueEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public Collection<Relative> getTag() {
        return argument();
    }
}
