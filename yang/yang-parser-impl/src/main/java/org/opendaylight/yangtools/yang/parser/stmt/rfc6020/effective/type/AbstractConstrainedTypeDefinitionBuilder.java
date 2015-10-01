/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DerivedType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType.Builder;
import org.opendaylight.yangtools.yang.parser.util.TypeConstraints;

abstract class AbstractConstrainedTypeDefinitionBuilder<T extends TypeDefinition<T>>
        extends AbstractTypeDefinitionBuilder<T> {
    // FIXME: get source reference
    private final TypeConstraints constraints = new TypeConstraints("foo", 4);

    final TypeConstraints getConstraints() {
        return constraints;
    }

    protected final TypeConstraints validConstraints() {
        constraints.validateConstraints();
        return constraints;
    }

    protected void modifyBuilder(final Builder builder) {
        // Default is no-op
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T build() {
        final Builder builder = ExtendedType.builder(getPath().getLastComponent(), getBaseType(),
            Optional.fromNullable(getDescription()), Optional.fromNullable(getReference()), getPath());

        modifyBuilder(builder);

        return (T)DerivedType.from(builder.build());
    }
}
