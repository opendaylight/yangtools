/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public final class UnionBaseType extends BaseType<UnionTypeDefinition> implements UnionTypeDefinition {
    private final List<TypeDefinition<?>> types;

    UnionBaseType(final SchemaPath path, final Collection<TypeDefinition<?>> types) {
        super(path);
        this.types = ImmutableList.copyOf(types);
    }

    @Override
    public List<TypeDefinition<?>> getTypes() {
        return types;
    }

    @Override
    public UnionConstrainedTypeBuilder newConstrainedTypeBuilder(final SchemaPath path) {
        return new UnionConstrainedTypeBuilder(this, path);
    }
}
