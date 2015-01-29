/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedLeafref extends NormalizatedDerivedType<LeafrefTypeDefinition> implements LeafrefTypeDefinition {

    public DerivedLeafref(final ExtendedType definition) {
        super(LeafrefTypeDefinition.class, definition);
    }

    @Override
    LeafrefTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedLeafref(base);
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return getBaseType().getPathStatement();
    }
}