/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public abstract class TypeBuilder<T extends TypeDefinition<T>> implements Mutable {
    private final @NonNull QName qname;
    private final T baseType;

    TypeBuilder(final T baseType, final QName qname) {
        this.qname = requireNonNull(qname);
        this.baseType = baseType;
    }

    /**
     * Build a {@link TypeDefinition} corresponding to this builder.
     *
     * @return A TypeDefinition
     */
    public abstract @NonNull T build();

    final T getBaseType() {
        return baseType;
    }

    final @NonNull QName getQName() {
        return qname;
    }
}
