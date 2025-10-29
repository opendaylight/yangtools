/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public final class UnionTypeBuilder extends TypeBuilder<UnionTypeDefinition> {
    private final ImmutableList.Builder<TypeDefinition<?>> builder = ImmutableList.builder();

    UnionTypeBuilder(final QName qname) {
        super(null, qname);
    }

    public UnionTypeBuilder addType(final @NonNull TypeDefinition<?> type) {
        builder.add(type);
        return this;
    }

    @Override
    public UnionTypeDefinition build() {
        return new BaseUnionType(getQName(), getUnknownSchemaNodes(), builder.build());
    }
}
