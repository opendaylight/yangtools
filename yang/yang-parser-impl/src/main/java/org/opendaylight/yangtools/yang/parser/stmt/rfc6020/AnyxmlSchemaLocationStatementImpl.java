/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.AnyxmlSchemaLocationNamespace;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyxmlSchemaLocationEffectiveStatementImpl;

@Beta
public final class AnyxmlSchemaLocationStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements
        UnknownStatement<SchemaNodeIdentifier> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
            SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION).build();

    AnyxmlSchemaLocationStatementImpl(
            final StmtContext<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>, ?> context) {
        super(context);
    }

    public static class AnyxmlSchemaLocationSupport
            extends AbstractStatementSupport<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>,
                EffectiveStatement<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>>> {

        public AnyxmlSchemaLocationSupport() {
            super(SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.nodeIdentifierFromPath(ctx, value);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>,
                EffectiveStatement<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            stmt.getParentContext().addToNs(AnyxmlSchemaLocationNamespace.class,
                    SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION, stmt);
        }

        @Override
        public UnknownStatement<SchemaNodeIdentifier> createDeclared(
                final StmtContext<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>, ?> ctx) {
            return new AnyxmlSchemaLocationStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>> createEffective(
                final StmtContext<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>,
                EffectiveStatement<SchemaNodeIdentifier, UnknownStatement<SchemaNodeIdentifier>>> ctx) {
            return new AnyxmlSchemaLocationEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public SchemaNodeIdentifier getArgument() {
        return argument();
    }
}
