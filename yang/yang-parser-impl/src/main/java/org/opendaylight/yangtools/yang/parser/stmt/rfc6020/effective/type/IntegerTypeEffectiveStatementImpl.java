/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.type.InvalidRangeConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class IntegerTypeEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<String,TypeStatement> implements TypeEffectiveStatement<TypeStatement> {

    private final IntegerTypeDefinition typeDefinition;

    public IntegerTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final IntegerTypeDefinition baseType) {
        super(ctx);

        final RangeRestrictedTypeBuilder<IntegerTypeDefinition> builder =
                RestrictedTypes.newIntegerBuilder(baseType, TypeUtils.typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl range = (RangeEffectiveStatementImpl) stmt;
                builder.setRangeConstraint(range, range.argument());
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            }
        }

        try {
            typeDefinition = builder.build();
        } catch (InvalidRangeConstraintException e) {
            final RangeConstraint c = e.getOffendingConstraint();
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid range constraint: <%s, %s>",
                c.getMin(), c.getMax());
        }
    }

    @Nonnull
    @Override
    public IntegerTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
