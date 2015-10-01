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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class TypeDefEffectiveStatementImpl extends EffectiveStatementBase<QName, TypedefStatement>
        implements TypedefEffectiveStatement {

    private final TypeEffectiveStatement<?> effectiveTypeStatement;

    public TypeDefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        final TypeEffectiveStatement<?> baseEffectiveType = firstEffective(TypeEffectiveStatement.class);
        Verify.verifyNotNull(baseEffectiveType, "Failed to look up effective type statement for typedef %s at %s",
            ctx.getStatementArgument(), ctx.getStatementSourceReference());

        // Now we need to instantiate a type for this typedef

        effectiveTypeStatement = baseEffectiveType.derive(new DefinedTypeEffectiveStatement(ctx),
            Utils.getSchemaPath(ctx));
    }

    @Override
    public TypeEffectiveStatement<?> getEffectiveTypeStatement() {
        return effectiveTypeStatement;
    }
}
