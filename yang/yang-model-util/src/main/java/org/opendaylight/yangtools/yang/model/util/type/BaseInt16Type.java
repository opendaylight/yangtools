/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseInt16Type extends AbstractRangeRestrictedBaseType<Int16TypeDefinition, Short>
        implements Int16TypeDefinition {
    static final @NonNull BaseInt16Type INSTANCE = new BaseInt16Type();

    private BaseInt16Type() {
        super(BaseTypes.INT16_QNAME, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Override
    public int hashCode() {
        return Int16TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Int16TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Int16TypeDefinition.toString(this);
    }
}
