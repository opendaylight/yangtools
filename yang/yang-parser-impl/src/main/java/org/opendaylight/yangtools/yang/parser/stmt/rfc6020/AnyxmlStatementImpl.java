/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyXmlEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.YangModeledAnyXmlEffectiveStatementImpl;

public class AnyxmlStatementImpl extends AbstractDeclaredStatement<QName> implements AnyxmlStatement {
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

    protected AnyxmlStatementImpl(final StmtContext<QName, AnyxmlStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractQNameStatementSupport<AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> {

        public Definition() {
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
            final Map<StatementDefinition, Mutable<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>,
                EffectiveStatement<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>>>> schemaLocations =
                ctx.getAllFromCurrentStmtCtxNamespace(AnyxmlSchemaLocationNamespace.class);
            if (schemaLocations != null && !schemaLocations.isEmpty()) {
                final SchemaNodeIdentifier anyXmlSchemaNodeIdentifier = schemaLocations.values().iterator().next()
                        .getStatementArgument();
                final Optional<ContainerSchemaNode> anyXmlSchema = getAnyXmlSchema(ctx, anyXmlSchemaNodeIdentifier);
                if (anyXmlSchema.isPresent()) {
                    return new YangModeledAnyXmlEffectiveStatementImpl(ctx, anyXmlSchema.get());
                }
            }
            return new AnyXmlEffectiveStatementImpl(ctx);
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
            return Optional.absent();
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

    @Override
    public WhenStatement getWhenStatement() {
        return firstDeclared(WhenStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends MustStatement> getMusts() {
        return allDeclared(MustStatement.class);
    }

    @Override
    public ConfigStatement getConfig() {
        return firstDeclared(ConfigStatement.class);
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public MandatoryStatement getMandatory() {
        return firstDeclared(MandatoryStatement.class);
    }
}
