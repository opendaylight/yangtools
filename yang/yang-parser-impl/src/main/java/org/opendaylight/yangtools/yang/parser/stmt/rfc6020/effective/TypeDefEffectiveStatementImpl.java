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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public final class TypeDefEffectiveStatementImpl extends EffectiveStatementBase<QName, TypedefStatement>
        implements TypedefEffectiveStatement {

    private final TypeEffectiveStatement<?> innerType;
    private final TypeDefinition<?> typeDefinition;

    public TypeDefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        innerType = Verify.verifyNotNull(firstEffective(TypeEffectiveStatement.class),
            "Failed to look up effective type statement for typedef %s at %s",
            ctx.getStatementArgument(), ctx.getStatementSourceReference());

        // Now we need to instantiate a type for this typedef
        final TypeDefinitionBuilder<?> b = innerType.newTypeDefinitionBuilder();
        b.setPath(Utils.getSchemaPath(ctx));
        b.addEffectiveStatements(effectiveSubstatements());
        typeDefinition = Verify.verifyNotNull(b.build());
    }

    @Override
    public TypeDefinitionBuilder<?> newTypeDefinitionBuilder() {
        // We are not setting calling setBaseType, as that would point to our internal type, which is not the intent
        // behind TypeDefinition#getBaseType().
        return innerType.newTypeDefinitionBuilder().setPath(typeDefinition.getPath())
                .addEffectiveStatements(effectiveSubstatements());
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return typeDefinition;
    }
}
