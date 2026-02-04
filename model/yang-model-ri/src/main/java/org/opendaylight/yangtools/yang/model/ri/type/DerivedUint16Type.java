/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;

final class DerivedUint16Type extends AbstractRangeRestrictedDerivedType<Uint16TypeDefinition, Uint16>
        implements Uint16TypeDefinition {
    DerivedUint16Type(final Uint16TypeDefinition baseType, final QName qname, final Object defaultValue,
            final String description, final String reference, final Status status, final String units) {
        super(baseType, qname, defaultValue, description, reference, status, units);
    }

    @Override
    public int hashCode() {
        return Uint16TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint16TypeDefinition.equals(this, obj);
    }
}
