/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

abstract class AbstractRestrictedType<T extends TypeDefinition<T>> extends AbstractTypeDefinition<T> {
    private final @NonNull T baseType;

    AbstractRestrictedType(final T baseType, final SchemaPath path,
            final Collection<UnknownSchemaNode> unknownSchemaNodes) {
        super(path, unknownSchemaNodes);
        this.baseType = requireNonNull(baseType);
    }

    @Override
    public final T getBaseType() {
        return baseType;
    }

    @Override
    public final Optional<String> getUnits() {
        return baseType.getUnits();
    }

    @Override
    public final Optional<? extends Object> getDefaultValue() {
        return baseType.getDefaultValue();
    }

    @Override
    public final Optional<String> getDescription() {
        return baseType.getDescription();
    }

    @Override
    public final Optional<String> getReference() {
        return baseType.getReference();
    }

    @Override
    public final Status getStatus() {
        return baseType.getStatus();
    }
}
