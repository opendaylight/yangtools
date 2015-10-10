/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

abstract class LengthDerivedType<T extends TypeDefinition<T>> extends DerivedType<T> {
    private final List<LengthConstraint> lengthConstraints;

    public LengthDerivedType(final T baseType, final SchemaPath path,
            final Object defaultValue, final String description, final String reference, final Status status,
            final String units, final List<LengthConstraint> lengthConstraints) {
        super(baseType, path, defaultValue, description, reference, status, units);
        this.lengthConstraints = Preconditions.checkNotNull(lengthConstraints);
    }

    public final List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }
}
