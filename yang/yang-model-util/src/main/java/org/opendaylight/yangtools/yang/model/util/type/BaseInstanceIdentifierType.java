/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseInstanceIdentifierType extends AbstractBaseType<InstanceIdentifierTypeDefinition>
        implements InstanceIdentifierTypeDefinition {
    static final @NonNull BaseInstanceIdentifierType INSTANCE = new BaseInstanceIdentifierType();

    private BaseInstanceIdentifierType() {
        super(BaseTypes.INSTANCE_IDENTIFIER_QNAME);
    }

    @Override
    public boolean requireInstance() {
        return false;
    }

    @Override
    public int hashCode() {
        return InstanceIdentifierTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return InstanceIdentifierTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return InstanceIdentifierTypeDefinition.toString(this);
    }
}
