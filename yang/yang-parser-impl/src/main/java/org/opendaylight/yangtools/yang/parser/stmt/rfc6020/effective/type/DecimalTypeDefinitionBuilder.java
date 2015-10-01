/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;

final class DecimalTypeDefinitionBuilder extends RangeTypeDefinitionBuilder<DecimalTypeDefinition> {
    private Integer fractionDigits;

    @Override
    protected void addEffectiveStatement(final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof FractionDigitsEffectiveStatementImpl) {
            final Integer f = ((FractionDigitsEffectiveStatementImpl) stmt).argument();
            if (fractionDigits != null) {
                Preconditions.checkArgument(fractionDigits.equals(f),
                    "Conflicting fraction digits %s, %d already specified", f, fractionDigits);
            }
            fractionDigits = f;
        } else {
            super.addEffectiveStatement(stmt);
        }
    }

    @Override
    protected void modifyBuilder(final Builder builder) {
        super.modifyBuilder(builder);
        Preconditions.checkArgument(fractionDigits != null, "Fraction digits not defined");
        builder.fractionDigits(fractionDigits);
    }
}
