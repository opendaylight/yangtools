/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedBinary extends NormalizatedDerivedType<BinaryTypeDefinition> implements BinaryTypeDefinition {

    public DerivedBinary(final ExtendedType definition) {
        super(BinaryTypeDefinition.class, definition);
    }

    @Override
    BinaryTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedBinary(base);
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return delegate().getLengthConstraints();
    }
}