/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema;

import static org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION;

import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnyxmlSchemaLocationSupport
        extends AbstractStatementSupport<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        ANYXML_SCHEMA_LOCATION).build();
    private static final AnyxmlSchemaLocationSupport INSTANCE = new AnyxmlSchemaLocationSupport();

    private AnyxmlSchemaLocationSupport() {
        super(ANYXML_SCHEMA_LOCATION);
    }

    public static AnyxmlSchemaLocationSupport getInstance() {
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
        stmt.getParentContext().addToNs(AnyxmlSchemaLocationNamespace.class, ANYXML_SCHEMA_LOCATION, stmt);
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
        return SUBSTATEMENT_VALIDATOR;
    }
}