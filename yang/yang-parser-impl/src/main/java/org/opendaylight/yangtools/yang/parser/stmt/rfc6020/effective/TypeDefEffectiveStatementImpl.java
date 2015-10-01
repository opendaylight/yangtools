/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Verify;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class TypeDefEffectiveStatementImpl extends AbstractEffectiveSchemaNode<TypedefStatement> implements
        TypedefEffectiveStatement {

    private final TypeDefinition<?> typeDefinition;

    public TypeDefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        final TypeEffectiveStatement<?> innerType = firstEffective(TypeEffectiveStatement.class);
        Verify.verifyNotNull(innerType, "Type definition %s %s does not have a type statement", ctx, ctx.getStatementSourceReference());

        final DerivedTypeBuilder<?> builder = DerivedTypes.newBuilder(innerType.getTypeDefinition(), getPath());
        for (EffectiveStatement<?, ?> s : effectiveSubstatements()) {
            if (s instanceof UnitsEffectiveStatementImpl) {
                builder.setUnits(((UnitsEffectiveStatementImpl)s).argument());
            }
            if (s instanceof DefaultEffectiveStatementImpl) {
                builder.setDefaultValue(((DefaultEffectiveStatementImpl)s).argument());
            }

        }

        builder.setDescription(getDescription());
        builder.setReference(getReference());
        builder.setStatus(getStatus());

        // FIXME: this is inefficient
        for (UnknownSchemaNode n : getUnknownSchemaNodes()) {
            builder.addUnknownSchemaNode(n);
        }

        this.typeDefinition = builder.build();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return typeDefinition;
    }
}
