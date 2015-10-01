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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.FractionDigitsEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

final class DecimalTypeDefinitionBuilder extends AbstractRangeTypeDefinitionBuilder<DecimalTypeDefinition> {

    @Override
    protected void addEffectiveStatement(final EffectiveStatement<?, ?> stmt) {

        if (stmt instanceof FractionDigitsEffectiveStatementImpl) {
            getConstraints().addFractionDigits(((FractionDigitsEffectiveStatementImpl) stmt).argument());
        } else {
            super.addEffectiveStatement(stmt);
        }
    }

    @Override
    public DecimalTypeDefinition build() {
        final TypeConstraints c = validConstraints();

        c.getRange();
        final Integer f = c.getFractionDigits();
        Preconditions.checkArgument(f != null, "Fraction digits not defined");


        // TODO Auto-generated method stub
        return null;
    }
}
