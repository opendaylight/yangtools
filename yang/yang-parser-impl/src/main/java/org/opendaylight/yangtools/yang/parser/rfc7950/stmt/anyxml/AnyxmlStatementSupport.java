/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anyxml;

import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.yang.parser.stmt.anyxmlschema.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ChildSchemaNodes;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public final class AnyxmlStatementSupport extends
        AbstractQNameStatementSupport<AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .ANYXML)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .addOptional(SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION)
        .build();

    public AnyxmlStatementSupport() {
        super(YangStmtMapping.ANYXML);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, AnyxmlStatement,
            EffectiveStatement<QName, AnyxmlStatement>> stmt) {
        stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
    }

    @Override
    public AnyxmlStatement createDeclared(final StmtContext<QName, AnyxmlStatement, ?> ctx) {
        return new AnyxmlStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, AnyxmlStatement> createEffective(
            final StmtContext<QName, AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> ctx) {
        final Map<StatementDefinition, Mutable<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement,
            EffectiveStatement<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement>>> schemaLocations =
            ctx.getAllFromCurrentStmtCtxNamespace(AnyxmlSchemaLocationNamespace.class);
        if (schemaLocations != null && !schemaLocations.isEmpty()) {
            final SchemaNodeIdentifier anyXmlSchemaNodeIdentifier = schemaLocations.values().iterator().next()
                    .getStatementArgument();
            final Optional<ContainerSchemaNode> anyXmlSchema = getAnyXmlSchema(ctx, anyXmlSchemaNodeIdentifier);
            if (anyXmlSchema.isPresent()) {
                return new YangModeledAnyXmlEffectiveStatementImpl(ctx, anyXmlSchema.get());
            }
        }
        return new AnyxmlEffectiveStatementImpl(ctx);
    }

    private static Optional<ContainerSchemaNode> getAnyXmlSchema(
            final StmtContext<QName, AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> ctx,
            final SchemaNodeIdentifier contentSchemaPath) {
        final StmtContext<?, ?, ?> findNode = Utils.findNode(ctx.getRoot(), contentSchemaPath);
        if (findNode != null) {
            final EffectiveStatement<?, ?> anyXmlSchemaNode = findNode.buildEffective();
            if (anyXmlSchemaNode instanceof ContainerSchemaNode) {
                return Optional.of((ContainerSchemaNode) anyXmlSchemaNode);
            }
        }
        return Optional.empty();
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}