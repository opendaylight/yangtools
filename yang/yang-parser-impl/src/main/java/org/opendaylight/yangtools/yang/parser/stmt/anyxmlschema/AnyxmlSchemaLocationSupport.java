/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public final class AnyxmlSchemaLocationSupport
        extends AbstractStatementSupport<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION).build();
    private static final AnyxmlSchemaLocationSupport INSTANCE = new AnyxmlSchemaLocationSupport();

    private AnyxmlSchemaLocationSupport() {
        super(SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION);
    }

    public static AnyxmlSchemaLocationSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return Utils.nodeIdentifierFromPath(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.getParentContext().addToNs(AnyxmlSchemaLocationNamespace.class,
                SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION, stmt);
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