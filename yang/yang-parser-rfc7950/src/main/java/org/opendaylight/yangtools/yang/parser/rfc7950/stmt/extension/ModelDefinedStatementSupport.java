/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import java.util.Optional;
import java.util.stream.Stream;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * StatementSupport for statements defined via YANG extensions. This is implemented by piggy-backing
 * to a {@link UnrecognizedStatementSupport}.
 *
 * @author Robert Varga
 */
final class ModelDefinedStatementSupport
        extends StatementSupport<String, UnrecognizedStatement, UnrecognizedEffectiveStatement> {
    private final UnrecognizedStatementSupport definition;

    ModelDefinedStatementSupport(final ModelDefinedStatementDefinition publicDefinition) {
        super(publicDefinition, CopyPolicy.DECLARED_COPY);
        this.definition = new UnrecognizedStatementSupport(publicDefinition);
    }

    @Override
    public UnrecognizedStatement createDeclared(final StmtContext<String, UnrecognizedStatement, ?> ctx) {
        return definition.createDeclared(ctx);
    }

    @Override
    public UnrecognizedEffectiveStatement createEffective(final Current<String, UnrecognizedStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
            final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        return definition.createEffective(stmt, declaredSubstatements, effectiveSubstatements);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return definition.parseArgumentValue(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return null;
    }

    @Override
    public Optional<StatementSupport<?, ?, ?>> getUnknownStatementDefinitionOf(final StatementDefinition yangStmtDef) {
        return definition.getUnknownStatementDefinitionOf(yangStmtDef);
    }
}
