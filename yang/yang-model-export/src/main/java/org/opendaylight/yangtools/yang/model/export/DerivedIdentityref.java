/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedIdentityref extends NormalizatedDerivedType<IdentityrefTypeDefinition> implements
        IdentityrefTypeDefinition {

    public DerivedIdentityref(final ExtendedType definition) {
        super(IdentityrefTypeDefinition.class, definition);
    }

    @Override
    IdentityrefTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedIdentityref(base);
    }

    @Override
    public IdentitySchemaNode getIdentity() {
        // FIXME: Is this really correct?
        return getBaseType().getIdentity();
    }
}