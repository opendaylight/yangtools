/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public final class TypeEffectiveStatementImpl extends EffectiveStatementBase<String, TypeStatement> implements TypeEffectiveStatement<TypeStatement> {
    private final TypedefEffectiveStatement typedef;
    private final SchemaPath path;

    public TypeEffectiveStatementImpl(final StmtContext<String, TypeStatement, ?> ctx, final TypedefEffectiveStatement typedef) {
        super(ctx);
        this.path = Utils.getSchemaPath(ctx);
        this.typedef = Preconditions.checkNotNull(typedef);
    }

    @Override
    public TypeDefinitionBuilder<?> newTypeDefinitionBuilder() {
        return typedef.newTypeDefinitionBuilder().setPath(path).addEffectiveStatements(effectiveSubstatements())
                .setBaseType(typedef.getTypeDefinition());
    }

    // Used by union type, which does not contribute to type definition, so we can reuse the typedef'd type
    TypeDefinition<?> buildType() {
        if (effectiveSubstatements().isEmpty()) {
            return typedef.getTypeDefinition();
        } else {
            return newTypeDefinitionBuilder().build();
        }
    }
}
