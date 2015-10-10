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
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

abstract class LengthDerivedTypeBuilder<T extends TypeDefinition<T>> extends DerivedTypeBuilder<T> {
    private final List<LengthConstraint> lengthConstraints;

    LengthDerivedTypeBuilder(final T baseType, final SchemaPath path, final List<LengthConstraint> lengthConstraints) {
        super(baseType, path);
        this.lengthConstraints = Preconditions.checkNotNull(lengthConstraints);
    }

    final List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }
}
