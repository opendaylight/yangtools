/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TypeDefEffectiveStatementImpl extends AbstractEffectiveSchemaNode<TypedefStatement> implements
        TypedefEffectiveStatement {
    private static final Logger LOG = LoggerFactory.getLogger(TypeDefEffectiveStatementImpl.class);
    private final TypeDefinition<?> typeDefinition;

    public TypeDefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        final TypeEffectiveStatement<?> typeEffectiveStmt = firstSubstatementOfType(TypeEffectiveStatement.class);
        final DerivedTypeBuilder<?> builder = DerivedTypes.derivedTypeBuilder(typeEffectiveStmt.getTypeDefinition(),
            ctx.getSchemaPath().get());
        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatementImpl) {
                builder.setDefaultValue(stmt.argument());
            } else if (stmt instanceof DescriptionEffectiveStatementImpl) {
                builder.setDescription(((DescriptionEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatementImpl) {
                builder.setReference(((ReferenceEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatementImpl) {
                builder.setStatus(((StatusEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatementImpl) {
                builder.setUnits(((UnitsEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof UnknownEffectiveStatementImpl) {
                // FIXME: should not directly implement, I think
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            } else {
                LOG.debug("Ignoring statement {}", stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return typeDefinition;
    }
}
