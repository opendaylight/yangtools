/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION;

import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnyxmlSchemaLocationStatementSupport
        extends AbstractStatementSupport<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>> {
    private static final AnyxmlSchemaLocationStatementSupport INSTANCE =
            new AnyxmlSchemaLocationStatementSupport(ANYXML_SCHEMA_LOCATION);

    private final SubstatementValidator validator;

    private AnyxmlSchemaLocationStatementSupport(final StatementDefinition definition) {
        super(definition);
        validator = SubstatementValidator.builder(definition).build();
    }

    public static AnyxmlSchemaLocationStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.nodeIdentifierFromPath(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.coerceParentContext().addToNs(AnyxmlSchemaLocationNamespace.class, ANYXML_SCHEMA_LOCATION, stmt);
    }

    @Override
    public AnyxmlSchemaLocationStatement createDeclared(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> ctx) {
        return new AnyxmlSchemaLocationStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement> createEffective(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>> ctx) {
        return new AnyxmlSchemaLocationEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}