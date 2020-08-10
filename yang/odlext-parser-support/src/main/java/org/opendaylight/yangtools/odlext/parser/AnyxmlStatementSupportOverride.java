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
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml.AnyxmlStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.ForwardingStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

public final class AnyxmlStatementSupportOverride
        extends ForwardingStatementSupport<QName, AnyxmlStatement, AnyxmlEffectiveStatement> {
    private static final AnyxmlStatementSupportOverride INSTANCE = new AnyxmlStatementSupportOverride();

    private AnyxmlStatementSupportOverride() {

    }

    public static AnyxmlStatementSupportOverride getInstance() {
        return INSTANCE;
    }

    @Override
    protected StatementSupport<QName, AnyxmlStatement, AnyxmlEffectiveStatement> delegate() {
        return AnyxmlStatementSupport.getInstance();
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        // FIXME: this is not entirely accurate?
        return delegate().getEffectiveRepresentationClass();
    }

    @Override
    public AnyxmlEffectiveStatement createEffective(
            final StmtContext<QName, AnyxmlStatement, AnyxmlEffectiveStatement> ctx) {
        final AnyxmlEffectiveStatement delegateStatement = delegate().createEffective(ctx);
        final Map<StatementDefinition, Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            AnyxmlSchemaLocationEffectiveStatement>> schemaLocations =
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
        return ChildSchemaNodeNamespace.findNode(ctx.getRoot(), contentSchemaPath)
                .map(StmtContext::buildEffective)
                .filter(ContainerSchemaNode.class::isInstance).map(ContainerSchemaNode.class::cast);
    }
}
