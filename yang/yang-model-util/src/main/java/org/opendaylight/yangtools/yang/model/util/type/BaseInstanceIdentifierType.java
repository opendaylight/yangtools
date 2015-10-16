/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseInstanceIdentifierType extends AbstractBaseType<InstanceIdentifierTypeDefinition>
        implements InstanceIdentifierTypeDefinition {
    static final BaseInstanceIdentifierType INSTANCE = new BaseInstanceIdentifierType();

    private BaseInstanceIdentifierType() {
        super(BaseTypes.INSTANCE_IDENTIFIER_QNAME);
    }

    @Deprecated
    @Override
    public RevisionAwareXPath getPathStatement() {
        throw new UnsupportedOperationException("API design error");
    }

    @Override
    public boolean requireInstance() {
        return false;
    }

    @Override
    public int hashCode() {
        return TypeDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return TypeDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeDefinitions.toString(this);
    }
}
