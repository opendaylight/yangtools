/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.SchemaNodeIdentifierBuildNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml.AnyxmlStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

public final class AnyxmlStatementSupportOverride
        implements StatementSupport<QName, AnyxmlStatement, AnyxmlEffectiveStatement> {
    private static final AnyxmlStatementSupportOverride INSTANCE = new AnyxmlStatementSupportOverride();

    private final StatementSupport<QName, AnyxmlStatement, AnyxmlEffectiveStatement> delegate =
            new AnyxmlStatementSupport();

    private AnyxmlStatementSupportOverride() {

    }

    public static AnyxmlStatementSupportOverride getInstance() {
        return INSTANCE;
    }

    @Override
    public QName getStatementName() {
        return delegate.getStatementName();
    }

    @Override
    public QName getArgumentName() {
        return delegate.getArgumentName();
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return delegate.getDeclaredRepresentationClass();
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return delegate.getEffectiveRepresentationClass();
    }

    @Override
    public boolean isArgumentYinElement() {
        return delegate.isArgumentYinElement();
    }

    @Override
    public AnyxmlStatement createDeclared(final StmtContext<QName, AnyxmlStatement, ?> ctx) {
        return delegate.createDeclared(ctx);
    }

    @Override
    public AnyxmlEffectiveStatement createEffective(
            final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx) {
        final AnyxmlEffectiveStatement delegateStatement = delegate.createEffective(ctx);
        final Map<StatementDefinition, Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>>> schemaLocations =
                ctx.getAllFromCurrentStmtCtxNamespace(AnyxmlSchemaLocationNamespace.class);
        if (schemaLocations != null && !schemaLocations.isEmpty()) {
            final SchemaNodeIdentifier anyXmlSchemaNodeIdentifier = schemaLocations.values().iterator().next()
                    .getStatementArgument();
            final Optional<ContainerSchemaNode> anyXmlSchema = getAnyXmlSchema(ctx, anyXmlSchemaNodeIdentifier);
            if (anyXmlSchema.isPresent()) {
                return new YangModeledAnyxmlEffectiveStatementImpl(delegateStatement, anyXmlSchema.get());
            }
        }

        return delegateStatement;
    }

    private static Optional<ContainerSchemaNode> getAnyXmlSchema(
            final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx,
            final SchemaNodeIdentifier contentSchemaPath) {
        return SchemaNodeIdentifierBuildNamespace.findNode(ctx.getRoot(), contentSchemaPath)
                .map(StmtContext::buildEffective)
                .filter(ContainerSchemaNode.class::isInstance).map(ContainerSchemaNode.class::cast);
    }

    @Override
    public StatementDefinition getPublicView() {
        return delegate.getPublicView();
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return delegate.parseArgumentValue(ctx, value);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, AnyxmlStatement, AnyxmlEffectiveStatement> stmt) {
        delegate.onStatementAdded(stmt);
    }

    @Override
    public Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        return delegate.getImplicitParentFor(stmtDef);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<QName, AnyxmlStatement, AnyxmlEffectiveStatement> stmt) {
        delegate.onPreLinkageDeclared(stmt);
    }

    @Override
    public void onLinkageDeclared(final Mutable<QName, AnyxmlStatement, AnyxmlEffectiveStatement> stmt) {
        delegate.onLinkageDeclared(stmt);
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<QName, AnyxmlStatement, AnyxmlEffectiveStatement> stmt) {
        delegate.onStatementDefinitionDeclared(stmt);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, AnyxmlStatement, AnyxmlEffectiveStatement> stmt) {
        delegate.onFullDefinitionDeclared(stmt);
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        return delegate.hasArgumentSpecificSupports();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        return delegate.getSupportSpecificForArgument(argument);
    }
}
