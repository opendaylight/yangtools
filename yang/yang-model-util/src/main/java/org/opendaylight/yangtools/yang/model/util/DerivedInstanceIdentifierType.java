/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

class DerivedInstanceIdentifierType extends DerivedType<InstanceIdentifierTypeDefinition> implements
        InstanceIdentifierTypeDefinition {

    public DerivedInstanceIdentifierType(final ExtendedType definition) {
        super(InstanceIdentifierTypeDefinition.class, definition);
    }

    @Override
    InstanceIdentifierTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedInstanceIdentifierType(base);
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        throw new UnsupportedOperationException("Path statement is not part of instance-identifier type");
    }

    @Override
    public boolean requireInstance() {
        return getBaseType().requireInstance();
    }
}