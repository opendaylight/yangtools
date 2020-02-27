/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.type;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

public final class DecimalTypeBuilder extends RangeRestrictedTypeBuilder<DecimalTypeDefinition, BigDecimal> {
    private Integer fractionDigits;

    DecimalTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    public DecimalTypeBuilder setFractionDigits(final int fractionDigits) {
        Preconditions.checkState(this.fractionDigits == null, "Fraction digits already defined to %s",
                this.fractionDigits);
        this.fractionDigits = fractionDigits;
        return this;
    }

    @Override
    DecimalTypeDefinition buildType() {
        Preconditions.checkState(fractionDigits != null, "Fraction digits not defined");

        return new BaseDecimalType(getPath(), getUnknownSchemaNodes(), fractionDigits,
            calculateRangeConstraint(BaseDecimalType.constraintsForDigits(fractionDigits)));
    }
}
