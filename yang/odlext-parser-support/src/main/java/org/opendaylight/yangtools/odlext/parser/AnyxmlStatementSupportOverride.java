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
import java.util.stream.Stream;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml.AnyxmlStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ForwardingStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

public final class AnyxmlStatementSupportOverride
        extends ForwardingStatementSupport<QName, AnyxmlStatement, AnyxmlEffectiveStatement> {
    private static final AnyxmlStatementSupportOverride INSTANCE = new AnyxmlStatementSupportOverride();

    private AnyxmlStatementSupportOverride() {
        super(AnyxmlStatementSupport.getInstance());
    }

    public static AnyxmlStatementSupportOverride getInstance() {
        return INSTANCE;
    }

    @Override
    public AnyxmlEffectiveStatement createEffective(final Current<QName, AnyxmlStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
                final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        final AnyxmlEffectiveStatement delegateStatement = super.createEffective(stmt, declaredSubstatements,
            effectiveSubstatements);

        final Map<StatementDefinition, Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
                AnyxmlSchemaLocationEffectiveStatement>> schemaLocations =
                stmt.localNamespacePortion(AnyxmlSchemaLocationNamespace.class);
        if (schemaLocations != null && !schemaLocations.isEmpty()) {
            final SchemaNodeIdentifier anyXmlSchemaNodeIdentifier = schemaLocations.values().iterator().next()
                    .argument();
            final Optional<ContainerSchemaNode> anyXmlSchema = getAnyXmlSchema(stmt, anyXmlSchemaNodeIdentifier);
            if (anyXmlSchema.isPresent()) {
                return new YangModeledAnyxmlEffectiveStatementImpl(delegateStatement, anyXmlSchema.get());
            }
        }

        return delegateStatement;
    }

    private static Optional<ContainerSchemaNode> getAnyXmlSchema(final Current<QName, AnyxmlStatement> stmt,
            final SchemaNodeIdentifier contentSchemaPath) {
        return SchemaTreeNamespace.findNode(stmt.caerbannog().getRoot(), contentSchemaPath)
                .map(StmtContext::buildEffective)
                .filter(ContainerSchemaNode.class::isInstance).map(ContainerSchemaNode.class::cast);
    }
}
