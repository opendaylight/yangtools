/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

class DerivedInstanceIdentifier extends NormalizatedDerivedType<InstanceIdentifierTypeDefinition> implements
        InstanceIdentifierTypeDefinition {

    public DerivedInstanceIdentifier(final ExtendedType definition) {
        super(InstanceIdentifierTypeDefinition.class, definition);
    }

    @Override
    InstanceIdentifierTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedInstanceIdentifier(base);
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