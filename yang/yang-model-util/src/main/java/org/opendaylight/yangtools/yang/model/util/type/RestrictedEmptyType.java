/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class RestrictedEmptyType extends AbstractRestrictedType<EmptyTypeDefinition> implements EmptyTypeDefinition {
    RestrictedEmptyType(final EmptyTypeDefinition baseType, final SchemaPath path,
            final Collection<? extends UnknownSchemaNode> unknownSchemaNodes) {
        super(baseType, path, unknownSchemaNodes);
    }

    private RestrictedEmptyType(final RestrictedEmptyType original, final SchemaPath path) {
        super(original, path);
    }

    @Override
    public RestrictedEmptyType bindTo(final SchemaPath newPath) {
        return new RestrictedEmptyType(this, newPath);
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
