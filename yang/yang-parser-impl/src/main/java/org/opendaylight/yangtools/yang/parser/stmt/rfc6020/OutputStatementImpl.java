/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class OutputStatementImpl extends AbstractDeclaredStatement<Void>
        implements OutputStatement {

    protected OutputStatementImpl(
            StmtContext<Void, OutputStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Void, OutputStatement, EffectiveStatement<Void, OutputStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Output);
        }

        @Override
        public Void parseArgumentValue(StmtContext<?, ?, ?> ctx, String value)
                throws SourceException {
            return null;
        }

        @Override
        public OutputStatement createDeclared(
                StmtContext<Void, OutputStatement, ?> ctx) {
            return new OutputStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Void, OutputStatement> createEffective(
                StmtContext<Void, OutputStatement, EffectiveStatement<Void, OutputStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public Collection<? extends TypedefStatement> getTypedefs() {
        return allDeclared(TypedefStatement.class);
    }

    @Override
    public Collection<? extends GroupingStatement> getGroupings() {
        return allDeclared(GroupingStatement.class);
    }

    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }
}

