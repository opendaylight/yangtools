/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseInt32Type extends AbstractRangeRestrictedBaseType<Int32TypeDefinition, Integer>
        implements Int32TypeDefinition {
    static final @NonNull BaseInt32Type INSTANCE = new BaseInt32Type();

    private BaseInt32Type() {
        super(BaseTypes.INT32_QNAME, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public int hashCode() {
        return Int32TypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return Int32TypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return Int32TypeDefinition.toString(this);
    }

}
