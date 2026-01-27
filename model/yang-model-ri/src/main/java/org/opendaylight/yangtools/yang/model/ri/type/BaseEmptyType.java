/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class BaseEmptyType extends AbstractBaseType<EmptyTypeDefinition> implements EmptyTypeDefinition {
    static final @NonNull BaseEmptyType INSTANCE = new BaseEmptyType();

    private BaseEmptyType() {
        super(BuiltInType.EMPTY.typeName());
    }

    @Override
    public int hashCode() {
        return EmptyTypeDefinition.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EmptyTypeDefinition.equals(this, obj);
    }

    @Override
    public String toString() {
        return EmptyTypeDefinition.toString(this);
    }
}
