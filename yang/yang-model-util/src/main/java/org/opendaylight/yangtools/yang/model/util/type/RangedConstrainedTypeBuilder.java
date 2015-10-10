/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

abstract class RangedConstrainedTypeBuilder<T extends TypeDefinition<T>> extends ConstrainedTypeBuilder<T> {
    private Collection<RangeConstraint> rangeAlternatives;

    RangedConstrainedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public void setRangeAlternatives(@Nonnull final Collection<RangeConstraint> rangeAlternatives) {
        Preconditions.checkState(this.rangeAlternatives == null, "Range alternatives already defined as %s",
                this.rangeAlternatives);
        this.rangeAlternatives = Preconditions.checkNotNull(rangeAlternatives);
    }

    final List<RangeConstraint> calculateRangeConstraints(final List<RangeConstraint> baseRangeConstraints) {
        if (rangeAlternatives == null || rangeAlternatives.isEmpty()) {
            return baseRangeConstraints;
        }

        // FIXME: calculate ranges
        throw new UnsupportedOperationException();
    }
}
