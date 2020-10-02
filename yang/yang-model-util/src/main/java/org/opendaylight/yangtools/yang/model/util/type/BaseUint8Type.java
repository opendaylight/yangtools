/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseUint8Type extends AbstractRangeRestrictedBaseType<Uint8TypeDefinition, Uint8>
        implements Uint8TypeDefinition {
    static final @NonNull BaseUint8Type INSTANCE = new BaseUint8Type();

    private BaseUint8Type() {
        super(BaseTypes.UINT8_QNAME, Uint8.ZERO, Uint8.MAX_VALUE);
    }

    @Override
    public int hashCode() {
        return Uint8TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint8TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Uint8TypeDefinition.toString(this);
    }
}
