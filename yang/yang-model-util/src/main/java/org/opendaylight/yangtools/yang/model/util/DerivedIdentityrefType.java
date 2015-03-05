/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

class DerivedIdentityrefType extends DerivedType<IdentityrefTypeDefinition> implements
        IdentityrefTypeDefinition {

    public DerivedIdentityrefType(final ExtendedType definition) {
        super(IdentityrefTypeDefinition.class, definition);
    }

    @Override
    IdentityrefTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedIdentityrefType(base);
    }

    @Override
    public IdentitySchemaNode getIdentity() {
        // FIXME: Is this really correct?
        return getBaseType().getIdentity();
    }
}