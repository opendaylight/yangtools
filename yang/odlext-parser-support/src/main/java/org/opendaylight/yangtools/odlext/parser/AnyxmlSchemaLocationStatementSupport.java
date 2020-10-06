/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnyxmlSchemaLocationStatementSupport
        extends BaseStatementSupport<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            AnyxmlSchemaLocationEffectiveStatement> {
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
            AnyxmlSchemaLocationEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.coerceParentContext().addToNs(AnyxmlSchemaLocationNamespace.class, ANYXML_SCHEMA_LOCATION, stmt);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected AnyxmlSchemaLocationStatement createDeclared(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new AnyxmlSchemaLocationStatementImpl(ctx, substatements);
    }

    @Override
    protected AnyxmlSchemaLocationStatement createEmptyDeclared(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected AnyxmlSchemaLocationEffectiveStatement createEffective(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
                AnyxmlSchemaLocationEffectiveStatement> ctx, final AnyxmlSchemaLocationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AnyxmlSchemaLocationEffectiveStatementImpl(declared, substatements, ctx);
    }

    @Override
    protected AnyxmlSchemaLocationEffectiveStatement createEmptyEffective(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
                AnyxmlSchemaLocationEffectiveStatement> ctx, final AnyxmlSchemaLocationStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}