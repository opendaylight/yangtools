/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.RequireInstanceRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.rfc6020.util.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public final class LeafrefTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {

    private final LeafrefTypeDefinition typeDefinition;

    public LeafrefTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final LeafrefTypeDefinition baseType) {
        super(ctx);

        final RequireInstanceRestrictedTypeBuilder<LeafrefTypeDefinition> builder =
                RestrictedTypes.newLeafrefBuilder(baseType, TypeUtils.typeEffectiveSchemaPath(ctx));

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement) stmt).argument());
            } else if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Nonnull
    @Override
    public LeafrefTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
