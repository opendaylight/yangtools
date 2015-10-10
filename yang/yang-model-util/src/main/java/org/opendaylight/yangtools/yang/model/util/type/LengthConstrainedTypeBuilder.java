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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

abstract class LengthConstrainedTypeBuilder<T extends TypeDefinition<T>> extends ConstrainedTypeBuilder<T> {
    private Collection<LengthConstraint> lengthAlternatives;

    LengthConstrainedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public final void setLengthAlternatives(
            @Nonnull final Collection<LengthConstraint> lengthAlternatives) {
        Preconditions.checkState(this.lengthAlternatives == null, "Range alternatives already defined as %s",
                lengthAlternatives);
        this.lengthAlternatives = Preconditions.checkNotNull(lengthAlternatives);
    }

    final List<LengthConstraint> calculateLenghtConstraints(final List<LengthConstraint> baseLengthConstraints) {
        if (lengthAlternatives == null || lengthAlternatives.isEmpty()) {
            return baseLengthConstraints;
        }

        // FIXME: calculate lengths
        throw new UnsupportedOperationException();
    }
}
