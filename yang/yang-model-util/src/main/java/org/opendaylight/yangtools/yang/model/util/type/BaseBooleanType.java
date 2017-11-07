/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class BaseBooleanType extends AbstractBaseType<BooleanTypeDefinition, Boolean> implements BooleanTypeDefinition {
    static final BaseBooleanType INSTANCE = new BaseBooleanType();

    private BaseBooleanType() {
        super(BaseTypes.BOOLEAN_QNAME);
    }

    @Override
    public int hashCode() {
        return BooleanTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return BooleanTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return BooleanTypeDefinition.toString(this);
    }
}
