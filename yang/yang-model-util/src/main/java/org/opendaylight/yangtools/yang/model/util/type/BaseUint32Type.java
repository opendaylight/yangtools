/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseUint32Type extends AbstractRangeRestrictedBaseType<Uint32TypeDefinition, Uint32>
        implements Uint32TypeDefinition {
    static final @NonNull BaseUint32Type INSTANCE = new BaseUint32Type();

    private BaseUint32Type() {
        super(BaseTypes.UINT32_QNAME, Uint32.ZERO, Uint32.MAX_VALUE);
    }

    @Override
    public int hashCode() {
        return Uint32TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Uint32TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Uint32TypeDefinition.toString(this);
    }
}
