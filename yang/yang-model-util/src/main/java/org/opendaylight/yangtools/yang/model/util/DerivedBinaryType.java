/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

class DerivedBinaryType extends DerivedType<BinaryTypeDefinition> implements BinaryTypeDefinition {

    public DerivedBinaryType(final ExtendedType definition) {
        super(BinaryTypeDefinition.class, definition);
    }

    @Override
    BinaryTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedBinaryType(base);
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return delegate().getLengthConstraints();
    }
}