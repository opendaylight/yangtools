/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseUint16Type extends AbstractRangeRestrictedBaseType<Uint16TypeDefinition, Integer>
        implements Uint16TypeDefinition {
    static final @NonNull BaseUint16Type INSTANCE = new BaseUint16Type();

    private BaseUint16Type() {
        super(BaseTypes.UINT16_QNAME, 0, 65535);
    }

    @Override
    public int hashCode() {
        return Uint16TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint16TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Uint16TypeDefinition.toString(this);
    }
}
