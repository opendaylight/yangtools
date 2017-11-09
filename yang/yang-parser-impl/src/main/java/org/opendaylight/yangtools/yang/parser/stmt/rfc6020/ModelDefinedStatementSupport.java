/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * StatementSupport for statements defined via YANG extensions. This is implemented by piggy-backing
 * to a {@link UnrecognizedStatementImpl.Definition}.
 *
 * @author Robert Varga
 */
public final class ModelDefinedStatementSupport extends AbstractStatementSupport<String,
        UnrecognizedStatement, EffectiveStatement<String, UnrecognizedStatement>> {
    private final UnrecognizedStatementImpl.Definition definition;

    ModelDefinedStatementSupport(final ModelDefinedStatementDefinition publicDefinition) {
        super(publicDefinition);
        this.definition = new UnrecognizedStatementImpl.Definition(publicDefinition);
    }

    @Override
    public UnrecognizedStatement createDeclared(final StmtContext<String, UnrecognizedStatement, ?> ctx) {
        return definition.createDeclared(ctx);
    }

    @Override
    public EffectiveStatement<String, UnrecognizedStatement> createEffective(
            final StmtContext<String, UnrecognizedStatement, EffectiveStatement<String, UnrecognizedStatement>> ctx) {
        return definition.createEffective(ctx);
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
