/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

class DerivedUnionType extends DerivedType<UnionTypeDefinition> implements UnionTypeDefinition {

    public DerivedUnionType(final ExtendedType definition) {
        super(UnionTypeDefinition.class, definition);
    }

    @Override
    UnionTypeDefinition createDerived(final ExtendedType base) {
        return new DerivedUnionType(base);
    }

    @Override
    public List<TypeDefinition<?>> getTypes() {
        return getBaseType().getTypes();
    }

}