/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

abstract class AbstractTypeDefinition<T extends TypeDefinition<T>> extends AbstractSchemaNode
        implements Immutable, TypeDefinition<T> {
    private final Object defaultValue;
    private final String units;
    private final T baseType;

    @SuppressWarnings("unchecked")
    protected AbstractTypeDefinition(final SchemaPath path, final String description, final String reference,
            final Status status, final List<UnknownSchemaNode> unknownSchemaNodes, final TypeDefinition<?> baseType,
            final String units, final Object defaultValue) {
        super(path, description, reference, status, unknownSchemaNodes);

        this.baseType = (T) baseType;
        this.defaultValue = defaultValue;
        this.units = units;
    }

    @Override
    public final T getBaseType() {
        return baseType;
    }

    @Override
    public final Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public final String getUnits() {
        return units;
    }

}
